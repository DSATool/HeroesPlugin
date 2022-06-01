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
package heroes.fight;

import dsa41basis.fight.CloseCombatWeapon;
import dsa41basis.fight.DefensiveWeapon;
import dsa41basis.fight.RangedWeapon;
import dsa41basis.ui.hero.SingleRollDialog;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import heroes.ui.HeroTabController;
import heroes.util.UiUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class FightController extends HeroTabController {

	@FXML
	private TitledPane closeCombatPane;
	@FXML
	private TableColumn<CloseCombatWeapon, Integer> closeCombatBFColumn;
	@FXML
	private TableColumn<CloseCombatWeapon, Integer> closeCombatEBEColumn;
	@FXML
	private TableColumn<CloseCombatWeapon, Integer> closeCombatPAColumn;
	@FXML
	private TableColumn<CloseCombatWeapon, Integer> closeCombatIniColumn;
	@FXML
	private TableColumn<CloseCombatWeapon, String> closeCombatNameColumn;
	@FXML
	private TableView<CloseCombatWeapon> closeCombatTable;
	@FXML
	private TableColumn<CloseCombatWeapon, String> closeCombatTypeColumn;
	@FXML
	private TitledPane defensiveWeaponsPane;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> defensiveWeaponsBFColumn;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> defensiveWeaponsIniColumn;
	@FXML
	private TableColumn<DefensiveWeapon, String> defensiveWeaponsNameColumn;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> defensiveWeaponsPAColumn;
	@FXML
	private TableView<DefensiveWeapon> defensiveWeaponsTable;
	@FXML
	private ScrollPane pane;
	@FXML
	private VBox stack;
	@FXML
	private TitledPane rangedCombatPane;
	@FXML
	private TableColumn<RangedWeapon, String> rangedCombatAmmunitionColumn;
	@FXML
	private TableColumn<RangedWeapon, Integer> rangedCombatEBEColumn;
	@FXML
	private TableColumn<RangedWeapon, String> rangedCombatNameColumn;
	@FXML
	private TableView<RangedWeapon> rangedCombatTable;
	@FXML
	private TableColumn<RangedWeapon, String> rangedCombatTPColumn;
	@FXML
	private TableColumn<RangedWeapon, String> rangedCombatTypeColumn;
	@FXML
	private TitledPane shieldsPane;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> shieldsBFColumn;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> shieldsIniColumn;
	@FXML
	private TableColumn<DefensiveWeapon, String> shieldsNameColumn;
	@FXML
	private TableView<DefensiveWeapon> shieldsTable;

	private final JSONListener listener = o -> fillTables();

	public FightController(final TabPane tabPane) {
		super(tabPane);
	}

	@FXML
	private void addArmorSet() {
		new ArmorSetDialog(pane.getScene().getWindow(), hero, hero.getObj("Kampf").getArr("Rüstungskombinationen"), null);
	}

	@SuppressWarnings("unchecked")
	private void fillTables() {
		closeCombatTable.getItems().clear();
		rangedCombatTable.getItems().clear();
		shieldsTable.getItems().clear();
		defensiveWeaponsTable.getItems().clear();

		final JSONObject talents = ResourceManager.getResource("data/Talente");
		final JSONObject closeCombatTalents = talents.getObj("Nahkampftalente");
		final JSONObject actualCloseCombatTalents = hero.getObj("Talente").getObj("Nahkampftalente");
		final JSONObject rangedCombatTalents = talents.getObj("Fernkampftalente");
		final JSONObject actualRangedCombatTalents = hero.getObj("Talente").getObj("Fernkampftalente");

		closeCombatTable.getItems().add(new CloseCombatWeapon(hero, HeroUtil.infight, HeroUtil.infight, closeCombatTalents, actualCloseCombatTalents));

		stack.getChildren().remove(4, stack.getChildren().size() - 1);
		final JSONArray armorSets = hero.getObj("Kampf").getArrOrDefault("Rüstungskombinationen", new JSONArray(null));
		for (int i = 0; i < armorSets.size(); ++i) {
			final Control armor = new ArmorList(hero, armorSets.getObj(i)).getControl();
			stack.getChildren().add(stack.getChildren().size() - 1, armor);
			GUIUtil.dragDropReorder(armor, moved -> {
				final int index = stack.getChildren().indexOf(moved) - 4;
				final JSONArray current = (JSONArray) moved.getUserData();
				armorSets.remove(current);
				armorSets.add(index, current);
				armorSets.notifyListeners(null);
			}, stack);
		}
		final ArmorList armorList = new ArmorList(hero, null);
		final Control armorControl = armorList.getControl();
		stack.getChildren().add(stack.getChildren().size() - 1, armorControl);
		final BooleanBinding hasArmor = Bindings.isNotEmpty(armorList.getArmor());
		armorControl.visibleProperty().bind(hasArmor);
		armorControl.managedProperty().bind(hasArmor);

		HeroUtil.foreachInventoryItem(hero, item -> item.containsKey("Kategorien"), (item, extraInventory) -> {
			final JSONArray categories = item.getArr("Kategorien");

			if (categories.contains("Nahkampfwaffe")) {
				if (item.containsKey("Nahkampfwaffe")) {
					closeCombatTable.getItems()
							.add(new CloseCombatWeapon(hero, item.getObj("Nahkampfwaffe"), item, closeCombatTalents, actualCloseCombatTalents));
				} else {
					closeCombatTable.getItems().add(new CloseCombatWeapon(hero, item, item, closeCombatTalents, actualCloseCombatTalents));
				}
			}
			if (categories.contains("Fernkampfwaffe")) {
				if (item.containsKey("Fernkampfwaffe")) {
					rangedCombatTable.getItems()
							.add(new RangedWeapon(hero, item.getObj("Fernkampfwaffe"), item, rangedCombatTalents, actualRangedCombatTalents));
				} else {
					rangedCombatTable.getItems().add(new RangedWeapon(hero, item, item, rangedCombatTalents, actualRangedCombatTalents));
				}
			}
			if (categories.contains("Schild")) {
				if (item.containsKey("Schild")) {
					shieldsTable.getItems().add(new DefensiveWeapon(true, hero, item.getObj("Schild"), item));
				} else {
					shieldsTable.getItems().add(new DefensiveWeapon(true, hero, item, item));
				}
			}
			if (categories.contains("Parierwaffe")) {
				if (item.containsKey("Parierwaffe")) {
					defensiveWeaponsTable.getItems().add(new DefensiveWeapon(false, hero, item.getObj("Parierwaffe"), item));
				} else {
					defensiveWeaponsTable.getItems().add(new DefensiveWeapon(false, hero, item, item));
				}
			}
		});

		if (HeroUtil.getMainWeapon(hero) == null) {
			defensiveWeaponsPAColumn.setCellFactory(UiUtil.signedIntegerCellFactory);
		} else {
			defensiveWeaponsPAColumn.setCellFactory(UiUtil.integerCellFactory);
		}
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Kampf";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Fight.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		super.init();

		final BooleanBinding hasCloseCombat = Bindings.isNotEmpty(closeCombatTable.getItems());
		closeCombatPane.visibleProperty().bind(hasCloseCombat);
		closeCombatPane.managedProperty().bind(hasCloseCombat);

		closeCombatTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(closeCombatTable);
		GUIUtil.cellValueFactories(closeCombatTable, "name", "type", "ebe", "tp", "at", "pa", "ini", "dk", "bf");

		closeCombatTable.setRowFactory(table -> {
			final TableRow<CloseCombatWeapon> row = new TableRow<>() {
				@Override
				public void updateItem(final CloseCombatWeapon weapon, final boolean empty) {
					super.updateItem(weapon, empty);
					if (empty) {
						setTooltip(null);
					} else {
						final String notes = HeroUtil.getWeaponNotes(weapon.getItem(), weapon.getBaseItem(), weapon.getType(), hero);
						if (notes.isBlank()) {
							setTooltip(null);
						} else {
							setTooltip(new Tooltip(notes));
						}
					}
				}
			};

			final ContextMenu rowMenu = new ContextMenu();

			final MenuItem atItem = new MenuItem("Attacke");
			atItem.setOnAction(e -> {
				final CloseCombatWeapon item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.ATTACK, null, item);
			});

			final MenuItem paItem = new MenuItem("Parade");
			paItem.setOnAction(e -> {
				final CloseCombatWeapon item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.DEFENSE, hero, item);
			});

			final MenuItem changeHandItem = new MenuItem();
			changeHandItem.textProperty().bind(Bindings.createStringBinding(() -> {
				final CloseCombatWeapon item = row.getItem();
				if (item == null) return "";
				return "In die " + (row.getItem().isSecondHand() != hero.getObj("Vorteile").containsKey("Linkshänder") ? "rechte Hand" : "linke Hand");
			}, row.itemProperty(), Bindings.createBooleanBinding(() -> row.getItem() != null && row.getItem().isSecondHand(), row.itemProperty())));
			changeHandItem.setOnAction(e -> {
				final CloseCombatWeapon item = row.getItem();
				item.setSecondHand(!item.isSecondHand());
			});
			changeHandItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
				final CloseCombatWeapon item = row.getItem();
				if (item == null) return false;
				return !item.getSpecial().contains("z");
			}, row.itemProperty(), Bindings.createBooleanBinding(() -> row.getItem() != null && row.getItem().getSpecial().contains("z"), row.itemProperty())));

			final CheckMenuItem mainWeaponItem = new CheckMenuItem();
			mainWeaponItem.textProperty().bind(Bindings.createStringBinding(() -> {
				final CloseCombatWeapon item = row.getItem();
				if (item == null) return "";
				return row.getItem().isSecondHand() ? "Seitenwaffe" : "Hauptwaffe";
			}, row.itemProperty(), Bindings.createBooleanBinding(() -> row.getItem() != null && row.getItem().isSecondHand(), row.itemProperty())));
			mainWeaponItem.setOnAction(e -> {
				final CloseCombatWeapon item = row.getItem();
				if (!item.isMainWeapon()) {
					unsetMainWeapon(item.isSecondHand());
				}
				item.setMainWeapon(!item.isMainWeapon());
			});
			mainWeaponItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
				final CloseCombatWeapon item = row.getItem();
				if (item == null) return false;
				return !item.getSpecial().contains("z");
			}, row.itemProperty(), Bindings.createBooleanBinding(() -> row.getItem() != null && row.getItem().getSpecial().contains("z"), row.itemProperty())));

			rowMenu.getItems().addAll(atItem, paItem, changeHandItem, mainWeaponItem);
			rowMenu.setOnShowing(e -> mainWeaponItem.setSelected(row.getItem() != null && row.getItem().isMainWeapon()));
			row.setContextMenu(rowMenu);

			return row;
		});

		closeCombatNameColumn.setCellFactory(p -> {
			final TextFieldTableCell<CloseCombatWeapon, String> cell = new TextFieldTableCell<>() {
				@Override
				public void updateItem(final String item, final boolean empty) {
					super.updateItem(item, empty);
					final CloseCombatWeapon weapon = getTableRow().getItem();
					if (weapon != null) {
						if (weapon.isMainWeapon()) {
							getStyleClass().add("bold");
						} else {
							getStyleClass().remove("bold");
						}
						if (weapon.isSecondHand()) {
							getStyleClass().add("italic");
						} else {
							getStyleClass().remove("italic");
						}
					}
				}
			};
			return cell;
		});

		closeCombatTypeColumn.setCellFactory(p -> {
			final ComboBoxTableCell<CloseCombatWeapon, String> cell = new ComboBoxTableCell<>() {
				@Override
				public void updateItem(final String item, final boolean empty) {
					super.updateItem(item, empty);
					if (!empty) {
						setPadding(Insets.EMPTY);
						final ComboBox<String> comboBox = new ComboBox<>(getItems());
						comboBox.itemsProperty().bind(getTableView().getItems().get(getIndex()).talentsProperty());
						comboBox.setMaxWidth(Double.MAX_VALUE);
						comboBox.getSelectionModel().select(item);
						comboBox.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
							if (newValue != null) {
								getTableView().getItems().get(getIndex()).setType(newValue);
							}
						});
						setGraphic(comboBox);
						setText(null);
					}
				}
			};
			return cell;
		});
		closeCombatEBEColumn.setCellFactory(UiUtil.signedIntegerCellFactory);
		closeCombatPAColumn.setCellFactory(UiUtil.integerCellFactory);
		closeCombatIniColumn.setCellFactory(UiUtil.signedIntegerCellFactory);
		closeCombatBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		closeCombatBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});

		final BooleanBinding hasRanged = Bindings.isNotEmpty(rangedCombatTable.getItems());
		rangedCombatPane.visibleProperty().bind(hasRanged);
		rangedCombatPane.managedProperty().bind(hasRanged);

		rangedCombatTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(rangedCombatTable);
		GUIUtil.cellValueFactories(rangedCombatTable, "name", "type", "ebe", "tp", "at", "load", "distance", "distancetp", "ammunition");

		rangedCombatTable.setRowFactory(table -> {
			final TableRow<RangedWeapon> row = new TableRow<>() {
				@Override
				public void updateItem(final RangedWeapon weapon, final boolean empty) {
					super.updateItem(weapon, empty);
					if (weapon == null) {
						setTooltip(null);
					} else {
						final String notes = HeroUtil.getWeaponNotes(weapon.getItem(), weapon.getBaseItem(), weapon.getType(), hero);
						if (notes.isBlank()) {
							setTooltip(null);
						} else {
							setTooltip(new Tooltip(notes));
						}
					}
				}
			};

			final ContextMenu rowMenu = new ContextMenu();

			final MenuItem atItem = new MenuItem("Attacke");
			atItem.setOnAction(e -> {
				final RangedWeapon item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.ATTACK, null, item);
			});

			rowMenu.getItems().add(atItem);
			row.setContextMenu(rowMenu);

			return row;
		});

		rangedCombatTypeColumn.setCellFactory(p -> {
			final ComboBoxTableCell<RangedWeapon, String> cell = new ComboBoxTableCell<>() {

				@Override
				public void updateItem(final String item, final boolean empty) {
					super.updateItem(item, empty);
					if (!empty) {
						setPadding(Insets.EMPTY);
						final ComboBox<String> comboBox = new ComboBox<>(getItems());
						comboBox.itemsProperty().bindBidirectional(getTableView().getItems().get(getIndex()).talentsProperty());
						comboBox.setMaxWidth(Double.MAX_VALUE);
						comboBox.getSelectionModel().select(item);
						comboBox.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
							if (newValue != null) {
								getTableView().getItems().get(getIndex()).setType(newValue);
							}
						});
						setGraphic(comboBox);
						setText(null);
					}
				}
			};
			return cell;
		});
		rangedCombatAmmunitionColumn.setCellFactory(o -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				if (!"Pfeile".equals(getItem()) && !"Bolzen".equals(getItem())) {
					final ReactiveSpinner<Integer> spinner = new ReactiveSpinner<>(0, getTableView().getItems().get(getIndex()).getAmmunitionMax());
					spinner.setEditable(true);
					createGraphic(spinner, () -> spinner.getValue().toString(), t -> spinner.getValueFactory().setValue(Integer.valueOf(t)));
				}
			}

			@Override
			public void updateItem(final String item, final boolean empty) {
				if ("Pfeile".equals(item) || "Bolzen".equals(item)) {
					final Button button = new Button(item);
					button.setOnAction(o -> {
						final int row = getTableRow().getIndex();
						final CellEditEvent<RangedWeapon, String> editEvent = new CellEditEvent<>(rangedCombatTable,
								new TablePosition<>(rangedCombatTable, row, rangedCombatAmmunitionColumn), TableColumn.editCommitEvent(), item);
						Event.fireEvent(rangedCombatAmmunitionColumn, editEvent);
					});
					setText(null);
					setGraphic(button);
				} else {
					super.updateItem(item, empty);
				}
			}
		});
		rangedCombatAmmunitionColumn.setOnEditCommit((final CellEditEvent<RangedWeapon, String> t) -> {
			if (t.getRowValue() != null)
				if ("Pfeile".equals(t.getNewValue()) || "Bolzen".equals(t.getNewValue())) {
					new AmmunitionDialog(pane.getScene().getWindow(), t.getRowValue());
				} else {
					t.getRowValue().setAmmunition(Integer.parseInt(t.getNewValue()));
				}
		});

		final BooleanBinding hasShields = Bindings.isNotEmpty(shieldsTable.getItems());
		shieldsPane.visibleProperty().bind(hasShields);
		shieldsPane.managedProperty().bind(hasShields);

		shieldsTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(shieldsTable);
		GUIUtil.cellValueFactories(shieldsTable, "name", "at", "pa", "ini", "bf");

		shieldsTable.setRowFactory(table -> {
			final TableRow<DefensiveWeapon> row = new TableRow<>() {
				@Override
				public void updateItem(final DefensiveWeapon weapon, final boolean empty) {
					super.updateItem(weapon, empty);
					if (weapon == null) {
						setTooltip(null);
					} else {
						final String notes = HeroUtil.getItemNotes(weapon.getItem(), weapon.getBaseItem());
						if (notes.isBlank()) {
							setTooltip(null);
						} else {
							setTooltip(new Tooltip(notes));
						}
					}
				}
			};

			final ContextMenu rowMenu = new ContextMenu();

			final MenuItem paItem = new MenuItem("Parade");
			paItem.setOnAction(e -> {
				final DefensiveWeapon item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.DEFENSE, hero, item);
			});

			final CheckMenuItem mainWeaponItem = new CheckMenuItem("Seitenwaffe");
			mainWeaponItem.setOnAction(e -> {
				final DefensiveWeapon item = row.getItem();
				if (!item.isMainWeapon()) {
					unsetMainWeapon(true);
				}
				item.setMainWeapon(!item.isMainWeapon());
			});

			rowMenu.getItems().addAll(paItem, mainWeaponItem);
			rowMenu.setOnShowing(e -> mainWeaponItem.setSelected(row.getItem() != null && row.getItem().isMainWeapon()));
			row.setContextMenu(rowMenu);

			return row;
		});

		shieldsNameColumn.setCellFactory(p -> {
			final TextFieldTableCell<DefensiveWeapon, String> cell = new TextFieldTableCell<>() {
				@Override
				public void updateItem(final String item, final boolean empty) {
					super.updateItem(item, empty);
					final DefensiveWeapon weapon = getTableRow().getItem();
					if (weapon != null) {
						if (weapon.isMainWeapon()) {
							getStyleClass().add("bold");
						} else {
							getStyleClass().remove("bold");
						}
					}
				}
			};
			return cell;
		});

		shieldsIniColumn.setCellFactory(UiUtil.signedIntegerCellFactory);
		shieldsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		shieldsBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});

		final BooleanBinding hasDefensiveWeapons = Bindings.isNotEmpty(defensiveWeaponsTable.getItems());
		defensiveWeaponsPane.visibleProperty().bind(hasDefensiveWeapons);
		defensiveWeaponsPane.managedProperty().bind(hasDefensiveWeapons);

		defensiveWeaponsTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(defensiveWeaponsTable);
		GUIUtil.cellValueFactories(defensiveWeaponsTable, "name", "at", "pa", "ini", "bf");

		defensiveWeaponsTable.setRowFactory(table -> {
			final TableRow<DefensiveWeapon> row = new TableRow<>() {
				@Override
				public void updateItem(final DefensiveWeapon weapon, final boolean empty) {
					super.updateItem(weapon, empty);
					if (weapon == null) {
						setTooltip(null);
					} else {
						final String notes = HeroUtil.getItemNotes(weapon.getItem(), weapon.getBaseItem());
						if (notes.isBlank()) {
							setTooltip(null);
						} else {
							setTooltip(new Tooltip(notes));
						}
					}
				}
			};

			final ContextMenu rowMenu = new ContextMenu();

			final MenuItem paItem = new MenuItem("Parade");
			paItem.setOnAction(e -> {
				final DefensiveWeapon item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.DEFENSE, hero, item);
			});

			final CheckMenuItem mainWeaponItem = new CheckMenuItem("Seitenwaffe");
			mainWeaponItem.setOnAction(e -> {
				final DefensiveWeapon item = row.getItem();
				if (!item.isMainWeapon()) {
					unsetMainWeapon(true);
				}
				item.setMainWeapon(!item.isMainWeapon());
			});

			rowMenu.getItems().addAll(paItem, mainWeaponItem);
			rowMenu.setOnShowing(e -> {
				paItem.setVisible(HeroUtil.getMainWeapon(hero) != null);
				mainWeaponItem.setSelected(row.getItem() != null && row.getItem().isMainWeapon());
			});
			row.setContextMenu(rowMenu);

			return row;
		});

		defensiveWeaponsNameColumn.setCellFactory(p -> {
			final TextFieldTableCell<DefensiveWeapon, String> cell = new TextFieldTableCell<>() {
				@Override
				public void updateItem(final String item, final boolean empty) {
					super.updateItem(item, empty);
					final DefensiveWeapon weapon = getTableRow().getItem();
					if (weapon != null) {
						if (weapon.isMainWeapon()) {
							getStyleClass().add("bold");
						} else {
							getStyleClass().remove("bold");
						}
					}
				}
			};
			return cell;
		});

		defensiveWeaponsIniColumn.setCellFactory(UiUtil.signedIntegerCellFactory);
		defensiveWeaponsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		defensiveWeaponsBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});
	}

	@Override
	protected void registerListeners() {
		hero.addListener(listener);
	}

	@Override
	protected void unregisterListeners() {
		hero.removeListener(listener);
		closeCombatTable.getItems().clear();
		rangedCombatTable.getItems().clear();
		shieldsTable.getItems().clear();
		defensiveWeaponsTable.getItems().clear();
	}

	private void unsetMainWeapon(final boolean secondary) {
		HeroUtil.foreachInventoryItem(hero,
				otherItem -> otherItem.containsKey("Kategorien"),
				(otherItem, extraInventory) -> {
					if (otherItem.getArr("Kategorien").contains("Nahkampfwaffe")) {
						final JSONObject baseWeapon = otherItem;
						if (otherItem != null && otherItem.containsKey("Nahkampfwaffe")) {
							otherItem = otherItem.getObj("Nahkampfwaffe");
						}

						if (otherItem.getBoolOrDefault("Hauptwaffe", baseWeapon.getBoolOrDefault("Hauptwaffe", false))
								&& otherItem.getBoolOrDefault("Zweithand", baseWeapon.getBoolOrDefault("Zweithand", false)) == secondary) {
							otherItem.removeKey("Hauptwaffe");
							otherItem.notifyListeners(listener);
						}
					}

					if (secondary) {
						if (otherItem.getArr("Kategorien").contains("Schild")) {
							final JSONObject baseWeapon = otherItem;
							if (otherItem != null && otherItem.containsKey("Schild")) {
								otherItem = otherItem.getObj("Schild");
							}

							if (otherItem.getBoolOrDefault("Seitenwaffe", baseWeapon.getBoolOrDefault("Seitenwaffe", false))) {
								otherItem.removeKey("Seitenwaffe");
								otherItem.notifyListeners(listener);
							}
						}
						if (otherItem.getArr("Kategorien").contains("Parierwaffe")) {
							final JSONObject baseWeapon = otherItem;
							if (otherItem != null && otherItem.containsKey("Parierwaffe")) {
								otherItem = otherItem.getObj("Parierwaffe");
							}

							if (otherItem.getBoolOrDefault("Seitenwaffe", baseWeapon.getBoolOrDefault("Seitenwaffe", false))) {
								otherItem.removeKey("Seitenwaffe");
								otherItem.notifyListeners(listener);
							}
						}
					}
				});
	}

	@Override
	protected void update() {
		fillTables();
	}
}
