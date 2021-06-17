/*
 * Copyright 2017 DSATool team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package heroes.pros_cons_skills;

import dsa41basis.hero.ProOrCon;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsa41basis.util.RequirementsUtil;
import dsatool.gui.GUIUtil;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveComboBox;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import heroes.ui.HeroTabController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class ProsOrConsController {
	@FXML
	private Button addButton;
	@FXML
	private TableColumn<ProOrCon, String> descColumn;
	@FXML
	protected ComboBox<String> list;
	@FXML
	private TableColumn<ProOrCon, String> nameColumn;
	@FXML
	private TitledPane pane;
	@FXML
	protected TableView<ProOrCon> table;
	@FXML
	protected TableColumn<ProOrCon, Integer> valueColumn;
	@FXML
	private TableColumn<ProOrCon, String> variantColumn;
	@FXML
	private TableColumn<ProOrCon, Boolean> validColumn;

	protected final JSONObject prosOrCons;
	protected JSONObject hero;
	protected final String category;
	protected final BooleanProperty showAll;
	private final JSONListener listener = o -> {
		fillTable();
		setVisibility();
	};

	public ProsOrConsController(final ScrollPane parent, final String name, final String singular, final boolean needsVariant, final JSONObject prosOrCons,
			final String category, final BooleanProperty showAll) {
		this.prosOrCons = prosOrCons;
		this.category = category;
		this.showAll = showAll;

		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ProsOrCons.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		pane.setText(name);
		nameColumn.setText(singular);

		table.prefWidthProperty().bind(parent.widthProperty().subtract(17));
		table.getSortOrder().add(nameColumn);

		GUIUtil.autosizeTable(table);

		if (!needsVariant) {
			descColumn.setResizable(false);
			variantColumn.setVisible(false);
		}

		nameColumn.setCellValueFactory(new PropertyValueFactory<ProOrCon, String>("displayName"));
		nameColumn.setCellFactory(c -> new TextFieldTableCell<>() {
			@Override
			public void updateItem(final String item, final boolean empty) {
				super.updateItem(item, empty);
				final TableRow<ProOrCon> row = getTableRow();
				final ProOrCon proOrCon = row != null ? row.getItem() : null;
				if (proOrCon != null) {
					Util.addReference(this, proOrCon.getProOrCon(), 15, nameColumn.widthProperty());
				}
			}
		});

		descColumn.setCellValueFactory(new PropertyValueFactory<ProOrCon, String>("description"));
		descColumn.setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final ObservableList<String> items = FXCollections
						.<String> observableArrayList(getTableView().getItems().get(getIndex()).getFirstChoiceItems(false));
				switch (getTableView().getItems().get(getIndex()).firstChoiceOrText()) {
					case TEXT:
						if (items.size() > 0) {
							final ComboBox<String> c = new ReactiveComboBox<>(items);
							c.setEditable(true);
							createGraphic(c, c.getSelectionModel()::getSelectedItem, c.getSelectionModel()::select);
						} else {
							final TextField t = new TextField();
							createGraphic(t, t::getText, t::setText);
						}
						break;
					case CHOICE:
						final ComboBox<String> c = new ReactiveComboBox<>(items);
						createGraphic(c, c.getSelectionModel()::getSelectedItem, c.getSelectionModel()::select);
						break;
					case NONE:
						final Label l = new Label();
						createGraphic(l, () -> "", s -> {});
						break;
				}
			}
		});
		descColumn.setOnEditCommit(t -> t.getRowValue().setDescription(t.getNewValue(), true));

		variantColumn.setCellValueFactory(new PropertyValueFactory<ProOrCon, String>("variant"));
		variantColumn.setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final ObservableList<String> items = FXCollections
						.<String> observableArrayList(getTableView().getItems().get(getIndex()).getSecondChoiceItems(false));
				switch (getTableView().getItems().get(getIndex()).secondChoiceOrText()) {
					case TEXT:
						if (items.size() > 0) {
							final ComboBox<String> c = new ReactiveComboBox<>(items);
							c.setEditable(true);
							createGraphic(c, c.getSelectionModel()::getSelectedItem, c.getSelectionModel()::select);
						} else {
							final TextField t = new TextField();
							createGraphic(t, t::getText, t::setText);
						}
						break;
					case CHOICE:
						final ComboBox<String> c = new ReactiveComboBox<>(items);
						createGraphic(c, c.getSelectionModel()::getSelectedItem, c.getSelectionModel()::select);
						break;
					case NONE:
						final Label l = new Label();
						createGraphic(l, () -> "", s -> {});
						break;
				}
			}
		});
		variantColumn.setOnEditCommit(t -> t.getRowValue().setVariant(t.getNewValue(), true));

		valueColumn.setCellValueFactory(new PropertyValueFactory<ProOrCon, Integer>("value"));
		valueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 9999) {
			@Override
			public void startEdit() {
				if (!HeroTabController.isEditable.get()) {
					final ProOrCon item = o.getTableView().getItems().get(getIndex());
					if (!item.getProOrCon().getBoolOrDefault("Schlechte Eigenschaft", false)) return;
					min = 0;
					max = item.getValue();
				}
				super.startEdit();
			}
		});
		valueColumn.setOnEditCommit(t -> {
			final ProOrCon quirk = t.getRowValue();
			final int newV = t.getNewValue();
			if (HeroTabController.isEditable.get()) {
				if (newV == 0) {
					quirk.remove();
				} else {
					quirk.setValue(newV);
				}
			} else {
				new QuirkReductionDialog(pane.getScene().getWindow(), quirk, hero, newV);
			}
		});

		validColumn.setCellValueFactory(new PropertyValueFactory<ProOrCon, Boolean>("valid"));
		validColumn.setCellFactory(tableColumn -> new TableCell<>() {
			@Override
			public void updateItem(final Boolean valid, final boolean empty) {
				super.updateItem(valid, empty);
				@SuppressWarnings("all")
				final TableRow<ProOrCon> row = getTableRow();
				row.getStyleClass().remove("invalid");
				if (!empty && !valid) {
					row.getStyleClass().add("invalid");
				}
			}
		});

		table.setRowFactory(t -> {
			final TableRow<ProOrCon> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();
			if ("Verbilligte Sonderfertigkeiten".equals(name)) {
				final MenuItem acquisitionItem = new MenuItem("Erwerben");
				contextMenu.getItems().add(acquisitionItem);
				acquisitionItem.setOnAction(o -> {
					final ProOrCon skill = row.getItem();
					final ProOrCon dummy = new ProOrCon(skill.getName(), hero, skill.getProOrCon(), skill.getActual().clone(null));
					new SkillAcquisitionDialog(pane.getScene().getWindow(), dummy, hero);
				});
			}
			if ("Nachteile".equals(name)) {
				final MenuItem reductionItem = new MenuItem("Senken");
				reductionItem.setOnAction(o -> {
					final ProOrCon con = row.getItem();
					new QuirkReductionDialog(pane.getScene().getWindow(), con, hero, con.getValue() - 1);
				});
				reductionItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
					final ProOrCon con = row.getItem();
					return con != null && con.getProOrCon().getBoolOrDefault("Schlechte Eigenschaft", false);
				}, row.itemProperty()));
				contextMenu.getItems().add(reductionItem);
			}
			if ("Kampf-Sonderfertigkeiten".equals(name)) {
				final MenuItem weaponMasteryItem = new MenuItem("Bearbeiten");
				weaponMasteryItem.setOnAction(o -> new WeaponMasteryDialog(pane.getScene().getWindow(), row.getItem()));
				weaponMasteryItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
					final ProOrCon skill = row.getItem();
					return skill != null && "Waffenmeister".equals(skill.getName());
				}, row.itemProperty()));
				contextMenu.getItems().add(weaponMasteryItem);
			}
			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(deleteAction(row));
			contextMenu.getItems().add(deleteItem);

			row.contextMenuProperty()
					.bind(Bindings.when(HeroTabController.isEditable.and(row.itemProperty().isNotNull())).then(contextMenu).otherwise((ContextMenu) null));

			return row;
		});

		descColumn.editableProperty().bind(HeroTabController.isEditable);
		variantColumn.editableProperty().bind(HeroTabController.isEditable);
		if (!"Sonderfertigkeiten".equals(category)) {
			list.disableProperty().bind(HeroTabController.isEditable.not());
			addButton.disableProperty().bind(HeroTabController.isEditable.not().or(Bindings.size(list.getItems()).isEqualTo(0)));
		} else {
			addButton.disableProperty().bind(Bindings.size(list.getItems()).isEqualTo(0));
		}

		showAll.addListener((o, oldV, newV) -> setVisibility());
	}

	@FXML
	protected void add() {
		final String proOrConName = list.getSelectionModel().getSelectedItem();
		final JSONObject proOrCon = prosOrCons.getObj(proOrConName);

		final JSONObject newProOrCon = newProOrCon(proOrConName, proOrCon);

		if (!HeroTabController.isEditable.get()) {
			final ProOrCon dummy = new ProOrCon(proOrConName, hero, proOrCon, newProOrCon);
			new SkillAcquisitionDialog(pane.getScene().getWindow(), dummy, hero);
		} else {
			final JSONObject actual = hero.getObj(category);
			JSONObject actualProOrCon;
			if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
				JSONArray proOrConList = actual.getArr(proOrConName);
				if (proOrConList == null) {
					proOrConList = new JSONArray(actual);
					actual.put(proOrConName, proOrConList);
				}
				actualProOrCon = newProOrCon.clone(proOrConList);
				proOrConList.add(actualProOrCon);
			} else {
				actualProOrCon = newProOrCon.clone(actual);
				actual.put(proOrConName, actualProOrCon);
			}
			HeroUtil.applyEffect(hero, proOrConName, proOrCon, actualProOrCon);
			actual.notifyListeners(null);
		}
	}

	protected EventHandler<ActionEvent> deleteAction(final TableRow<ProOrCon> row) {
		return o -> {
			final ProOrCon item = row.getItem();
			item.remove();
			fillList();
		};
	}

	protected void fillList() {
		list.getItems().clear();

		final JSONObject actual = hero.getObj(category);

		DSAUtil.foreach(proOrCon -> true, (proOrConName, proOrCon) -> {
			if (!actual.containsKey(proOrConName) || proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
				if (showAll.get() || RequirementsUtil.isRequirementFulfilled(hero, proOrCon.getObjOrDefault("Voraussetzungen", null), null, null, false)) {
					list.getItems().add(new ProOrCon(proOrConName, hero, proOrCon, new JSONObject(null)).getDisplayName());
				}
			}
		}, prosOrCons);

		if (list.getItems().size() > 0) {
			list.getSelectionModel().select(0);
		}
	}

	protected void fillTable() {
		table.getItems().clear();

		final JSONObject actual = hero.getObj(category);

		for (final String proOrConName : actual.keySet()) {
			if (prosOrCons.containsKey(proOrConName)) {
				final JSONObject proOrCon = prosOrCons.getObj(proOrConName);
				if (!"Breitgefächerte Bildung".equals(proOrConName) && !"Veteran".equals(proOrConName)
						&& (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext"))) {
					final JSONArray list = actual.getArr(proOrConName);
					for (int i = 0; i < list.size(); ++i) {
						table.getItems().add(new ProOrCon(proOrConName, hero, proOrCon, list.getObj(i)));
					}
				} else {
					table.getItems().add(new ProOrCon(proOrConName, hero, proOrCon, actual.getObj(proOrConName)));
				}
			}
		}

		table.sort();
	}

	public Node getControl() {
		return pane;
	}

	private JSONObject newProOrCon(final String name, final JSONObject proOrCon) {
		final JSONObject actualProOrCon = new JSONObject(null);
		if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
			if (proOrCon.containsKey("Auswahl")) {
				actualProOrCon.put("Auswahl", new ProOrCon(name, hero, proOrCon, actualProOrCon).getFirstChoiceItems(true).iterator().next());
			}
			if (proOrCon.containsKey("Freitext")) {
				actualProOrCon.put("Freitext", new ProOrCon(name, hero, proOrCon, actualProOrCon).getSecondChoiceItems(true).iterator().next());
			}
		} else if ("Breitgefächerte Bildung".equals(name)) {
			actualProOrCon.put("Profession", "");
		} else if ("Auswahl".equals(proOrCon.getString("Spezialisierung"))) {
			actualProOrCon.put("Auswahl", "Raufen");
		}
		return actualProOrCon;
	}

	public void registerListeners() {
		hero.addListener(listener);
	}

	public void setHero(final JSONObject hero) {
		this.hero = hero;
		fillTable();
		setVisibility();
	}

	private void setVisibility() {
		fillList();
		final boolean visible = showAll.get() || !table.getItems().isEmpty() || !list.getItems().isEmpty();
		pane.setVisible(visible);
		pane.setManaged(visible);
	}

	public void unregisterListeners() {
		hero.removeListener(listener);
		table.getItems().clear();
	}
}
