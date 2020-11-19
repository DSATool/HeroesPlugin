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
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicListCell;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class WeaponMasteryDialog {

	public class ManeuverOrPro {
		public StringProperty name;
		public IntegerProperty value;

		public ManeuverOrPro(final String maneuver, final Integer value) {
			name = new SimpleStringProperty(maneuver);
			this.value = new SimpleIntegerProperty(value);
		}

		public StringProperty nameProperty() {
			return name;
		}

		public IntegerProperty valueProperty() {
			return value;
		}
	}

	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private ComboBox<String> talent;
	@FXML
	private ComboBox<String> weapon;
	@FXML
	private Node iniBox;
	@FXML
	private ReactiveSpinner<Integer> ini;
	@FXML
	private Node tpkkBox;
	@FXML
	private ReactiveSpinner<Integer> tpkkThreshold;
	@FXML
	private ReactiveSpinner<Integer> tpkkStep;
	@FXML
	private ReactiveSpinner<Integer> at;
	@FXML
	private Node pawmBox;
	@FXML
	private ReactiveSpinner<Integer> pa;
	@FXML
	private Node rangeBox;
	@FXML
	private ReactiveSpinner<Integer> range;
	@FXML
	private Node loadTimeBox;
	@FXML
	private CheckBox loadTime;
	@FXML
	private TableView<ManeuverOrPro> easierManeuverTable;
	@FXML
	private Node additionalManeuverBox;
	@FXML
	private ListView<String> additionalManeuverList;
	@FXML
	private TableView<ManeuverOrPro> prosTable;
	@FXML
	private ListView<String> weaponsList;

	public WeaponMasteryDialog(final Window window, final ProOrCon skill) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("WeaponMasteryDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject actual = skill.getActual();
		final ProOrCon skillCopy = new ProOrCon("Waffenmeister", null, skill.getProOrCon(), actual.clone(null));

		final JSONObject rangedCombatTalents = ResourceManager.getResource("data/Talente").getObj("Fernkampftalente");

		final Stage stage = new Stage();
		stage.setTitle("Waffenmeister bearbeiten");
		stage.setScene(new Scene(root, 300, 575));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		talent.setItems(FXCollections.observableArrayList(skill.getFirstChoiceItems(false)));

		talent.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
			if (rangedCombatTalents.containsKey(talent.getValue())) {
				initRangedCombat(actual);
				stage.setHeight(510);
			} else {
				initCloseCombat(actual);
				stage.setHeight(613);
			}
			skillCopy.setDescription(newV, false);
			weapon.setItems(FXCollections.observableArrayList(skillCopy.getSecondChoiceItems(false)));
		});

		okButton.setOnAction(event -> {
			final boolean ranged = rangedCombatTalents.containsKey(talent.getValue());

			skill.setDescription(talent.getValue(), true);
			skill.setVariant(weapon.getValue(), true);

			if (ranged || ini.getValue() == 0) {
				actual.removeKey("Initiative:Modifikator");
			} else {
				actual.put("Initiative:Modifikator", ini.getValue());
			}

			if (ranged || tpkkThreshold.getValue() == 0 && tpkkStep.getValue() == 0) {
				actual.removeKey("Trefferpunkte/Körperkraft");
			} else {
				final JSONObject tpkk = new JSONObject(actual);
				if (tpkkThreshold.getValue() != 0) {
					tpkk.put("Schwellenwert", tpkkThreshold.getValue());
				}
				if (tpkkStep.getValue() != 0) {
					tpkk.put("Schadensschritte", tpkkStep.getValue());
				}
				actual.put("Trefferpunkte/Körperkraft", tpkk);
			}

			if (at.getValue() == 0 && pa.getValue() == 0) {
				actual.removeKey("Waffenmodifikatoren");
			} else {
				final JSONObject wm = new JSONObject(actual);
				if (at.getValue() != 0) {
					wm.put("Attackemodifikator", at.getValue());
				}
				if (!ranged && pa.getValue() != 0) {
					wm.put("Parademodifikator", pa.getValue());
				}
				actual.put("Waffenmodifikatoren", wm);
			}

			if (!ranged || range.getValue() == 0) {
				actual.removeKey("Reichweite");
			} else {
				actual.put("Reichweite", range.getValue() / 10);
			}

			if (!ranged || !loadTime.isSelected()) {
				actual.removeKey("Ladezeit");
			} else {
				actual.put("Ladezeit", true);
			}

			if (easierManeuverTable.getItems().isEmpty()) {
				actual.removeKey("Manöver:Erleichterung");
			} else {
				final JSONObject maneuvers = new JSONObject(actual);
				for (final ManeuverOrPro maneuver : easierManeuverTable.getItems()) {
					maneuvers.put(maneuver.name.get(), maneuver.value.get());
				}
				actual.put("Manöver:Erleichterung", maneuvers);
			}

			if (ranged || additionalManeuverList.getItems().isEmpty()) {
				actual.removeKey("Manöver:Zusätzlich");
			} else {
				final JSONArray maneuvers = new JSONArray(actual);
				for (final String maneuver : additionalManeuverList.getItems()) {
					maneuvers.add(maneuver);
				}
				actual.put("Manöver:Zusätzlich", maneuvers);
			}

			if (prosTable.getItems().isEmpty()) {
				actual.removeKey("Vorteile");
			} else {
				final JSONObject pros = new JSONObject(actual);
				for (final ManeuverOrPro pro : prosTable.getItems()) {
					pros.put(pro.name.get(), pro.value.get());
				}
				actual.put("Vorteile", pros);
			}

			if (weaponsList.getItems().isEmpty()) {
				actual.removeKey("Waffen");
			} else {
				final JSONArray weapons = new JSONArray(actual);
				for (final String weapon : weaponsList.getItems()) {
					weapons.add(weapon);
				}
				actual.put("Waffen", weapons);
			}

			stage.close();
		});

		cancelButton.setOnAction(e -> stage.close());

		talent.setValue(skill.getDescription());
		weapon.setValue(skill.getVariant());

		initTable(easierManeuverTable, "Manöver:Erleichterung", actual);
		initTable(prosTable, "Vorteile", actual);

		initList(weaponsList, 51, "Waffen", actual);

		stage.show();
	}

	private void initCloseCombat(final JSONObject skill) {
		iniBox.setManaged(true);
		iniBox.setVisible(true);
		tpkkBox.setManaged(true);
		tpkkBox.setVisible(true);
		pawmBox.setManaged(true);
		pawmBox.setVisible(true);
		rangeBox.setManaged(false);
		rangeBox.setVisible(false);
		loadTimeBox.setManaged(false);
		loadTimeBox.setVisible(false);
		additionalManeuverBox.setManaged(true);
		additionalManeuverBox.setVisible(true);

		ini.getValueFactory().setValue(skill.getIntOrDefault("Initiative:Modifikator", 0));

		final JSONObject tpkk = skill.getObj("Trefferpunkte/Körperkraft");
		if (tpkk != null) {
			tpkkThreshold.getValueFactory().setValue(tpkk.getIntOrDefault("Schwellenwert", 0));
			tpkkStep.getValueFactory().setValue(tpkk.getIntOrDefault("Schadensschritte", 0));
		}

		final JSONObject wm = skill.getObj("Waffenmodifikatoren");
		if (wm != null) {
			at.getValueFactory().setValue(wm.getIntOrDefault("Attackemodifikator", 0));
			pa.getValueFactory().setValue(wm.getIntOrDefault("Parademodifikator", 0));
		}

		initList(additionalManeuverList, 76, "Manöver:Zusätzlich", skill);
	}

	private void initList(final ListView<String> list, final int height, final String key, final JSONObject actual) {
		list.setPrefHeight(height);
		list.setMinHeight(height);
		list.setMaxHeight(height);

		list.setCellFactory(c -> {
			final ListCell<String> cell = new GraphicListCell<>(false) {

				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};

			final ContextMenu contextMenu = new ContextMenu();

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(e -> {
				final int index = cell.getIndex();
				list.getItems().remove(index);
			});
			deleteItem.visibleProperty().bind(cell.itemProperty().isNotNull());
			contextMenu.getItems().add(deleteItem);

			final MenuItem addItem = new MenuItem("Hinzufügen");
			addItem.setOnAction(e -> {
				list.getItems().add("Beschreibung");
			});
			contextMenu.getItems().add(addItem);

			cell.setContextMenu(contextMenu);

			return cell;
		});

		list.getItems().clear();

		if (actual.containsKey(key)) {
			final JSONArray items = actual.getArr(key);
			for (final String item : items.getStrings()) {
				list.getItems().add(item);
			}
		}
	}

	private void initRangedCombat(final JSONObject skill) {
		iniBox.setManaged(false);
		iniBox.setVisible(false);
		tpkkBox.setManaged(false);
		tpkkBox.setVisible(false);
		pawmBox.setManaged(false);
		pawmBox.setVisible(false);
		rangeBox.setManaged(true);
		rangeBox.setVisible(true);
		loadTimeBox.setManaged(true);
		loadTimeBox.setVisible(true);
		additionalManeuverBox.setManaged(false);
		additionalManeuverBox.setVisible(false);

		final JSONObject wm = skill.getObj("Waffenmodifikatoren");
		if (wm != null) {
			at.getValueFactory().setValue(wm.getIntOrDefault("Attackemodifikator", 0));
		}

		range.getValueFactory().setValue(skill.getIntOrDefault("Reichweite", 0) * 10);

		loadTime.setSelected(skill.getBoolOrDefault("Ladezeit", false));
	}

	@SuppressWarnings("unchecked")
	private void initTable(final TableView<ManeuverOrPro> table, final String key, final JSONObject actual) {
		table.setPrefHeight(101);
		table.setMinHeight(101);
		table.setMaxHeight(101);

		((TableColumn<ManeuverOrPro, String>) table.getColumns().get(0)).setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final TextField t = new TextField();
				createGraphic(t, t::getText, t::setText);
			}
		});
		((TableColumn<ManeuverOrPro, Integer>) table.getColumns().get(1))
				.setCellFactory(IntegerSpinnerTableCell.forTableColumn(0, 9, 1, false, (i, e) -> new Tuple<>(0, 9)));

		GUIUtil.cellValueFactories(table, "name", "value");

		table.setRowFactory(t -> {
			final TableRow<ManeuverOrPro> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(e -> {
				final int index = row.getIndex();
				table.getItems().remove(index);
			});
			deleteItem.visibleProperty().bind(row.itemProperty().isNotNull());
			contextMenu.getItems().add(deleteItem);

			final MenuItem addItem = new MenuItem("Hinzufügen");
			addItem.setOnAction(e -> {
				table.getItems().add(new ManeuverOrPro("Beschreibung", 1));
			});
			contextMenu.getItems().add(addItem);

			row.setContextMenu(contextMenu);

			return row;
		});

		if (actual.containsKey(key)) {
			final JSONObject items = actual.getObj(key);
			for (final String item : items.keySet()) {
				table.getItems().add(new ManeuverOrPro(item, items.getInt(item)));
			}
		}
	}
}
