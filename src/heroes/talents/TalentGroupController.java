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
package heroes.talents;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dsa41basis.hero.FightTalent;
import dsa41basis.hero.LanguageTalent;
import dsa41basis.hero.PhysicalTalent;
import dsa41basis.hero.Spell;
import dsa41basis.hero.Talent;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple;
import dsatool.util.Util;
import heroes.ui.HeroTabController;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class TalentGroupController {
	@FXML
	private TableColumn<Talent, String> nameColumn;
	@FXML
	private TableView<Talent> table;
	@FXML
	private ComboBox<String> talentsList;
	@FXML
	private Node pane;
	@FXML
	private ComboBox<String> representationsList;
	@FXML
	private Button addButton;

	private String name;
	private JSONObject talentGroup;
	private JSONObject talents;
	private JSONObject hero;

	private final JSONListener listener = o -> refreshTable();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TalentGroupController(final ScrollPane parent, final String name, final JSONObject talentGroup, final JSONObject talents) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		final URL gui = switch (name) {
			case "Nahkampftalente", "Fernkampftalente" -> getClass().getResource("FightTalents.fxml");
			case "Körperliche Talente" -> getClass().getResource("PhysicalTalents.fxml");
			case "Sprachen und Schriften" -> getClass().getResource("LanguageTalents.fxml");
			case "Ritualkenntnis" -> getClass().getResource("SimpleTalents.fxml");
			case "Zauber" -> getClass().getResource("Spells.fxml");
			default -> getClass().getResource("RegularTalents.fxml");
		};

		try {
			fxmlLoader.load(gui.openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		if (!"Zauber".equals(name)) {
			((TitledPane) pane).setText(name);
		}

		table.prefWidthProperty().bind(parent.widthProperty().subtract(17));

		GUIUtil.autosizeTable(table);

		nameColumn.setCellValueFactory(new PropertyValueFactory<Talent, String>("displayName"));
		nameColumn.setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final Talent item = getTableRow().getItem();
				final JSONObject talent = item.getTalent();
				final ComboBox<String> t = new ComboBox();
				t.setItems(FXCollections.observableArrayList(
						HeroUtil.getChoices(null, talent.getStringOrDefault("Auswahl", talent.getStringOrDefault("Freitext", null)), null)));
				t.setEditable(talent.containsKey("Freitext"));
				createGraphic(t, t::getValue, s -> t.getSelectionModel().select(item.getVariant()));
			}

			@Override
			public void startEdit() {
				if (isEmpty()) return;
				final JSONObject talent = getTableRow().getItem().getTalent();
				if (!talent.containsKey("Auswahl") && !talent.containsKey("Freitext")) return;
				super.startEdit();
			}

			@Override
			public void updateItem(final String item, final boolean empty) {
				super.updateItem(item, empty);
				final Talent talent = getTableRow().getItem();
				if (talent != null) {
					Util.addReference(this, talent.getTalent(), 15, nameColumn.widthProperty());
				}
			}
		});
		nameColumn.setOnEditCommit((final CellEditEvent<Talent, String> t) -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setVariant(t.getNewValue());
			}
		});
		nameColumn.editableProperty().bind(HeroTabController.isEditable);

		this.name = name;
		this.talentGroup = talentGroup;
		this.talents = talents;

		int i = 1;

		table.setRowFactory(t -> {
			final TableRow<Talent> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();

			final MenuItem editItem = new MenuItem("Bearbeiten");
			editItem.setOnAction(o -> {
				final Talent item = row.getItem();
				new TalentEditDialog(pane.getScene().getWindow(), item);
			});
			editItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
				final Talent talent = row.getItem();
				return talent != null && (talent.getTalent().containsKey("Auswahl") || talent.getTalent().containsKey("Freitext"));
			}, row.itemProperty()));

			final MenuItem enhanceItem = new MenuItem("Steigern");
			enhanceItem.setOnAction(o -> {
				final Talent item = row.getItem();
				new TalentEnhancementDialog(pane.getScene().getWindow(), item, hero, item.getValue() == Integer.MIN_VALUE ? 0 : item.getValue() + 1);
			});

			contextMenu.getItems().addAll(editItem, enhanceItem);

			if (!Arrays.asList("Nahkampftalente", "Fernkampftalente").contains(name)) {
				final MenuItem rollItem = new MenuItem("Talentprobe");
				rollItem.setOnAction(o -> {
					final Talent item = row.getItem();
					new TalentRollDialog(pane.getScene().getWindow(), item.getName(), item instanceof Spell ? ((Spell) item).getRepresentation() : null,
							new JSONObject[] { hero });
				});
				final MenuItem rollGroupItem = new MenuItem("Gruppenprobe");
				rollGroupItem.setOnAction(o -> {
					final Talent item = row.getItem();
					final List<JSONObject> characters = ResourceManager.getAllResources("characters/");
					new TalentRollDialog(pane.getScene().getWindow(), item.getName(), item instanceof Spell ? ((Spell) item).getRepresentation() : null,
							characters.toArray(new JSONObject[characters.size()]));
				});
				contextMenu.getItems().addAll(rollItem, rollGroupItem);
			}

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(o -> {
				final Talent item = row.getItem();
				item.removeTalent();
			});
			deleteItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
				final Talent talent = row.getItem();
				return talent != null && !talent.getTalent().getBoolOrDefault("Basis", false)
						&& (HeroTabController.isEditable.get() || talent.getValue() == Integer.MIN_VALUE && talent.getSes() == 0);
			}, row.itemProperty(), HeroTabController.isEditable));
			contextMenu.getItems().add(deleteItem);

			row.setContextMenu(contextMenu);

			return row;
		});

		switch (name) {
			case "Nahkampftalente":
				final TableColumn<Talent, Integer> atColumn = (TableColumn<Talent, Integer>) table.getColumns().get(i);
				atColumn.editableProperty().bind(HeroTabController.isEditable);
				final TableColumn<Talent, Integer> paColumn = (TableColumn<Talent, Integer>) table.getColumns().get(i + 1);
				paColumn.setCellValueFactory(new PropertyValueFactory<Talent, Integer>("pa"));
				paColumn.setCellFactory(
						IntegerSpinnerTableCell.<Talent> forTableColumn(0, 0, 1, false, (final IntegerSpinnerTableCell<Talent> cell, final Boolean empty) -> {
							if (empty) return new Tuple<>(0, 0);
							final int val = cell.getTableView().getItems().get(cell.getIndex()).getValue();
							if (val == Integer.MIN_VALUE) return new Tuple<>(0, 0);
							final int min = Math.max(0, (int) Math.ceil((val - 5) / 2.0));
							return new Tuple<>(min, val - min);
						}));
				paColumn.setCellFactory(
						IntegerSpinnerTableCell.<Talent> forTableColumn(0, 0, 1, false, (final IntegerSpinnerTableCell<Talent> cell, final Boolean empty) -> {
							if (empty) return new Tuple<>(0, 0);
							final int val = cell.getTableView().getItems().get(cell.getIndex()).getValue();
							if (val == Integer.MIN_VALUE) return new Tuple<>(0, 0);
							final int min = Math.max(0, (int) Math.ceil((val - 5) / 2.0));
							return new Tuple<>(min, val - min);
						}));
				paColumn.setOnEditCommit((final CellEditEvent<Talent, Integer> t) -> {
					((FightTalent) t.getRowValue()).setPa(t.getNewValue());
				});
				paColumn.setVisible(true);
				paColumn.editableProperty().bind(HeroTabController.isEditable);
			case "Fernkampftalente":
				final TableColumn<Talent, Integer> rangedAtColumn = (TableColumn<Talent, Integer>) table.getColumns().get(i);
				rangedAtColumn.setCellValueFactory(new PropertyValueFactory<Talent, Integer>("at"));
				rangedAtColumn.setCellFactory(
						IntegerSpinnerTableCell.<Talent> forTableColumn(0, 0, 1, false, (final IntegerSpinnerTableCell<Talent> cell, final Boolean empty) -> {
							if (empty) return new Tuple<>(0, 0);
							final FightTalent talent = (FightTalent) cell.getTableView().getItems().get(cell.getIndex());
							final int val = talent.getValue();
							if (val == Integer.MIN_VALUE) return new Tuple<>(0, 0);
							if (talent.getAttackOnly()) return new Tuple<>(val, val);
							final int min = Math.max(0, (int) Math.ceil((val - 5) / 2.0));
							return new Tuple<>(min, val - min);
						}));
				rangedAtColumn.setOnEditCommit((final CellEditEvent<Talent, Integer> t) -> {
					((FightTalent) t.getRowValue()).setAt(t.getNewValue());
				});
				rangedAtColumn.editableProperty().bind(HeroTabController.isEditable);
				++i;
				++i;
				final TableColumn fightBeColumn = table.getColumns().get(i);
				fightBeColumn.setCellValueFactory(new PropertyValueFactory<FightTalent, String>("be"));
				++i;
				break;
			case "Körperliche Talente":
				final TableColumn beColumn = table.getColumns().get(i);
				beColumn.setCellValueFactory(new PropertyValueFactory<PhysicalTalent, String>("be"));
				++i;
			case "Gesellschaftliche Talente":
			case "Natur-Talente":
			case "Wissenstalente":
			case "Handwerkstalente":
			case "Gaben":
			case "Liturgiekenntnis":
				final TableColumn attributesColumn = table.getColumns().get(i);
				attributesColumn.setCellValueFactory(new PropertyValueFactory<Talent, String>("attributes"));
				++i;
				break;
			case "Sprachen und Schriften":
				final TableColumn<Talent, String> mlsltlColumn = (TableColumn<Talent, String>) table.getColumns().get(i);
				mlsltlColumn.setCellValueFactory(new PropertyValueFactory<Talent, String>("mlsltl"));
				mlsltlColumn.setCellFactory(ComboBoxTableCell.forTableColumn("", "MS", "ZS", "LS"));
				mlsltlColumn.setOnEditCommit((final CellEditEvent<Talent, String> t) -> {
					((LanguageTalent) t.getRowValue()).setMlsltl(t.getNewValue());
				});
				mlsltlColumn.editableProperty().bind(HeroTabController.isEditable);
				++i;
				final TableColumn complexityColumn = table.getColumns().get(i);
				complexityColumn.setCellValueFactory(new PropertyValueFactory<LanguageTalent, Integer>("complexity"));
				++i;
				final TableColumn langAttributesColumn = table.getColumns().get(i);
				langAttributesColumn.setCellValueFactory(new PropertyValueFactory<LanguageTalent, String>("attributes"));
				++i;
				break;
			case "Zauber":
				final TableColumn representationColumn = table.getColumns().get(i);
				representationColumn.setCellValueFactory(new PropertyValueFactory<Spell, String>("representation"));
				++i;
				final TableColumn spellComplexityColumn = table.getColumns().get(i);
				spellComplexityColumn.setCellValueFactory(new PropertyValueFactory<Spell, String>("complexity"));
				++i;
				final TableColumn spellAttributesColumn = table.getColumns().get(i);
				spellAttributesColumn.setCellValueFactory(new PropertyValueFactory<Spell, String>("attributes"));
				++i;
				final TableColumn<Talent, Boolean> spellPrimaryColumn = (TableColumn<Talent, Boolean>) table.getColumns().get(i);
				spellPrimaryColumn.setCellValueFactory(new PropertyValueFactory<Talent, Boolean>("primarySpell"));
				spellPrimaryColumn.setCellFactory(CheckBoxTableCell.forTableColumn(spellPrimaryColumn));
				spellPrimaryColumn.setOnEditCommit((final CellEditEvent<Talent, Boolean> t) -> {
					((Spell) t.getRowValue()).setPrimarySpell(t.getNewValue());
				});
				spellPrimaryColumn.editableProperty().bind(HeroTabController.isEditable);
				++i;
				break;
			default:
				break;
		}

		final TableColumn<Talent, Boolean> primaryColumn = (TableColumn<Talent, Boolean>) table.getColumns().get(i);
		primaryColumn.setCellValueFactory(new PropertyValueFactory<Talent, Boolean>("primaryTalent"));
		primaryColumn.setCellFactory(CheckBoxTableCell.forTableColumn(primaryColumn));
		primaryColumn.setOnEditCommit((final CellEditEvent<Talent, Boolean> t) -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setPrimaryTalent(t.getNewValue());
			}
		});
		primaryColumn.editableProperty().bind(HeroTabController.isEditable);
		++i;

		final TableColumn<Talent, Integer> sesColumn = (TableColumn<Talent, Integer>) table.getColumns().get(i);
		sesColumn.setCellValueFactory(new PropertyValueFactory<Talent, Integer>("ses"));
		sesColumn.setCellFactory(o -> new IntegerSpinnerTableCell(0, 9));
		sesColumn.setOnEditCommit((final CellEditEvent<Talent, Integer> t) -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setSes(t.getNewValue());
			}
		});
		++i;

		final TableColumn<Talent, Integer> valueColumn = (TableColumn<Talent, Integer>) table.getColumns().get(i);
		valueColumn.setCellValueFactory(new PropertyValueFactory<Talent, Integer>("value"));
		valueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99) {
			@Override
			public void updateItem(final Integer item, final boolean empty) {
				if (empty || item.equals(Integer.MIN_VALUE)) {
					final Button button = new Button("Akt.");
					button.setOnAction(o -> {
						final int row = getTableRow().getIndex();
						final CellEditEvent<Talent, Integer> editEvent = new CellEditEvent<>(table, new TablePosition<>(table, row, valueColumn),
								TableColumn.editCommitEvent(), 0);
						Event.fireEvent(valueColumn, editEvent);
					});
					setText(null);
					setGraphic(button);
				} else {
					super.updateItem(item, empty);
				}
			}
		});
		valueColumn.setOnEditCommit((final CellEditEvent<Talent, Integer> t) -> {
			if (t.getRowValue() != null)
				if (HeroTabController.isEditable.get()) {
					t.getRowValue().setValue(t.getNewValue());
				} else {
					new TalentEnhancementDialog(pane.getScene().getWindow(), t.getRowValue(), hero, t.getNewValue());
				}
		});

		if (representationsList != null) {
			talentsList.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
				if (newValue != null) {
					representationsList.getItems().clear();
					final JSONObject spell = talents.getObj(newValue);
					final Set<String> representations = spell.getObj("Repräsentationen").keySet();
					final JSONObject actual = hero.getObj("Zauber");
					final JSONObject actualSpell = actual.getObjOrDefault(newValue, null);
					if (actualSpell == null) {
						representationsList.getItems().setAll(representations);
					} else {
						for (final String representation : representations) {
							if (!actualSpell.containsKey(representation) || spell.containsKey("Auswahl") || spell.containsKey("Freitext")) {
								representationsList.getItems().add(representation);
							}
						}
					}
					representationsList.getSelectionModel().select(0);
				}
			});
		}
	}

	@FXML
	private void addTalent() {
		final JSONObject actualGroups = "Zauber".equals(name) ? hero : hero.getObj("Talente");
		final String talentName = talentsList.getSelectionModel().getSelectedItem();
		JSONObject actual = actualGroups.getObj(name);
		if (actual == null) {
			actual = new JSONObject(actualGroups);
			actualGroups.put(name, actual);
		}
		final JSONObject talent = HeroUtil.findTalent(talentName)._1;
		final Talent newTalent;
		if ("Zauber".equals(name)) {
			newTalent = Spell.getSpell(talentName, talent, null, actual.getObjOrDefault(talentName, null), actual,
					representationsList.getSelectionModel().getSelectedItem());
		} else {
			JSONObject group = talentGroup;
			if ("Sprachen und Schriften".equals(name)) {
				group = group.getObj(talents.getObj(talentName).getBoolOrDefault("Schrift", false) ? "Schriften" : "Sprachen");
			}
			newTalent = Talent.getTalent(talentName, group, talent, null, actual);
		}
		newTalent.insertTalent(false);
	}

	public Node getControl() {
		return pane;
	}

	private void refreshTable() {
		talentsList.getItems().clear();
		table.getItems().clear();

		final JSONObject actualGroup = "Zauber".equals(name) ? hero.getObjOrDefault("Zauber", null) : hero.getObj("Talente").getObjOrDefault(name, null);

		DSAUtil.foreach(talent -> true, (talentName, talent) -> {
			final List<JSONObject> actualTalents;
			if (actualGroup == null) {
				actualTalents = null;
			} else if (!"Zauber".equals(name) && actualGroup.containsKey(talentName) && (talent.containsKey("Auswahl") || talent.containsKey("Freitext"))) {
				actualTalents = new LinkedList<>();
				final JSONArray choiceTalent = actualGroup.getArrOrDefault(talentName, null);
				if (choiceTalent != null) {
					for (int i = 0; i < choiceTalent.size(); ++i) {
						actualTalents.add(choiceTalent.getObj(i));
					}
				}
				talentsList.getItems().add(talentName);
			} else if (actualGroup.containsKey(talentName)) {
				actualTalents = Collections.singletonList(actualGroup.getObj(talentName));
			} else {
				actualTalents = null;
			}
			if (actualTalents == null) {
				talentsList.getItems().add(talentName);
				return;
			}

			for (final JSONObject actualTalent : actualTalents) {
				switch (name) {
					case "Nahkampftalente":
					case "Fernkampftalente":
					case "Körperliche Talente":
					case "Gesellschaftliche Talente":
					case "Natur-Talente":
					case "Wissenstalente":
					case "Handwerkstalente":
					case "Gaben":
					case "Liturgiekenntnis":
					case "Ritualkenntnis":
						table.getItems().add(Talent.getTalent(talentName, talentGroup, talent, actualTalent, actualGroup));
						break;
					case "Sprachen und Schriften":
						table.getItems()
								.add(Talent.getTalent(talentName, talentGroup.getObj(talent.getBoolOrDefault("Schrift", false) ? "Schriften" : "Sprachen"),
										talent, actualTalent, actualGroup));
						break;
					case "Zauber":
						boolean notFound = false;
						for (final String rep : talent.getObj("Repräsentationen").keySet()) {
							if (actualTalent.containsKey(rep)) {
								if (talent.containsKey("Auswahl") || talent.containsKey("Freitext")) {
									final JSONArray choiceTalent = actualTalent.getArrOrDefault(rep, null);
									if (choiceTalent != null) {
										for (int i = 0; i < choiceTalent.size(); ++i) {
											table.getItems().add(Spell.getSpell(talentName, talent, choiceTalent.getObj(i), actualTalent, actualGroup, rep));
										}
									}
								} else {
									table.getItems().add(Spell.getSpell(talentName, talent, actualTalent.getObj(rep), actualTalent, actualGroup, rep));
								}
							} else {
								notFound = true;
							}
						}
						if (notFound) {
							talentsList.getItems().add(talentName);
						}
						break;
				}
			}
		}, talents);

		if (talentsList.getItems().size() > 0) {
			talentsList.setDisable(false);
			talentsList.getSelectionModel().select(0);
			addButton.setDisable(false);
		} else {
			talentsList.setDisable(true);
			addButton.setDisable(true);
		}
	}

	public void registerListeners() {
		final JSONObject actual = "Zauber".equals(name) ? hero.getObj("Zauber") : hero.getObj("Talente").getObj(name);
		actual.addListener(listener);
	}

	@SuppressWarnings("unchecked")
	public void setHero(final JSONObject hero) {
		this.hero = hero;

		final JSONObject actualGroups = "Zauber".equals(name) ? hero : hero.getObj("Talente");
		JSONObject actualGroup = actualGroups.getObj(name);

		if (actualGroup == null) {
			actualGroup = new JSONObject(actualGroups);
			actualGroups.put(name, actualGroup);
		}

		final TableColumn<Talent, Boolean> primaryColumn = (TableColumn<Talent, Boolean>) table.getColumns().get(table.getColumns().size() - 3);
		final boolean needsPrimary = hero.getObj("Nachteile").containsKey("Elfische Weltsicht");
		primaryColumn.setVisible(needsPrimary);
		GUIUtil.autosizeTable(table);

		refreshTable();
	}

	public void unregisterListeners() {
		final JSONObject actual = "Zauber".equals(name) ? hero.getObj("Zauber") : hero.getObj("Talente").getObj(name);
		if (actual != null) {
			actual.removeListener(listener);
		}
		talentsList.getItems().clear();
		table.getItems().clear();
	}
}
