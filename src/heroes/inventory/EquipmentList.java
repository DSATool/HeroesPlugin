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
package heroes.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dsa41basis.inventory.InventoryItem;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import heroes.animals.HorseArmorEditor;
import heroes.ui.HeroTabController;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;
import jsonant.value.JSONValue;

public class EquipmentList {

	private static void initLocationItem(final JSONObject item, final JSONValue possessor, final JSONObject inventory, final RadioMenuItem location,
			final Menu menu, final ToggleGroup locationGroup) {
		location.setToggleGroup(locationGroup);
		if (possessor == inventory) {
			location.setSelected(true);
		} else {
			location.setOnAction(e -> {
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
				final JSONArray equipment = inventory.getArr("Ausrüstung");
				equipment.add(item.clone(equipment));
				equipment.notifyListeners(null);
				menu.getParentPopup().hide();
			});
		}
	}

	public static void updateLocationMenu(final JSONObject item, final Menu location, final JSONObject hero) {
		location.getItems().clear();

		final ToggleGroup locationGroup = new ToggleGroup();

		final JSONValue possessor = item.getParent() != null ? item.getParent().getParent() : null;

		final JSONArray inventories = hero.getObj("Besitz").getArrOrDefault("Inventare", null);
		if (inventories != null && inventories.size() > 0) {
			final Menu heroLocationItem = new Menu(hero.getObj("Biografie").getString("Vorname"));

			final RadioMenuItem defaultInventoryItem = new RadioMenuItem(hero.getObj("Biografie").getString("Vorname"));
			initLocationItem(item, possessor, hero.getObj("Besitz"), defaultInventoryItem, location, locationGroup);
			heroLocationItem.getItems().add(defaultInventoryItem);

			DSAUtil.foreach(o -> true, inventory -> {
				final RadioMenuItem inventoryItem = new RadioMenuItem(inventory.getStringOrDefault("Name", "Unbenanntes Inventar"));
				initLocationItem(item, possessor, inventory, inventoryItem, location, locationGroup);
				heroLocationItem.getItems().add(inventoryItem);
			}, inventories);

			location.getItems().add(heroLocationItem);
		} else {
			final RadioMenuItem heroLocationItem = new RadioMenuItem(hero.getObj("Biografie").getString("Vorname"));
			initLocationItem(item, possessor, hero.getObj("Besitz"), heroLocationItem, location, locationGroup);
			location.getItems().add(heroLocationItem);
		}

		final JSONArray animals = hero.getArr("Tiere");
		for (int i = 0; i < animals.size(); ++i) {
			final JSONObject animal = animals.getObj(i);

			final JSONArray animalInventories = animal.getArrOrDefault("Inventare", null);
			if (animalInventories != null && animalInventories.size() > 0) {
				final Menu heroLocationItem = new Menu(animal.getObj("Biografie").getString("Name"));

				final RadioMenuItem defaultInventoryItem = new RadioMenuItem(animal.getObj("Biografie").getString("Name"));
				initLocationItem(item, possessor, animal, defaultInventoryItem, location, locationGroup);
				heroLocationItem.getItems().add(defaultInventoryItem);

				DSAUtil.foreach(o -> true, inventory -> {
					final RadioMenuItem inventoryItem = new RadioMenuItem(inventory.getStringOrDefault("Name", "Unbenanntes Inventar"));
					initLocationItem(item, possessor, inventory, inventoryItem, location, locationGroup);
					heroLocationItem.getItems().add(inventoryItem);
				}, animalInventories);

				location.getItems().add(heroLocationItem);
			} else {
				final RadioMenuItem animalLocationItem = new RadioMenuItem(animal.getObj("Biografie").getString("Name"));
				initLocationItem(item, possessor, animal, animalLocationItem, location, locationGroup);
				location.getItems().add(animalLocationItem);
			}
		}
	}

	@FXML
	private TitledPane pane;
	@FXML
	private TableColumn<InventoryItem, String> equipmentNameColumn;
	@FXML
	private TableColumn<InventoryItem, String> equipmentNotesColumn;
	@FXML
	private TableView<InventoryItem> equipmentTable;

	@FXML
	private ComboBox<String> equipmentList;

	private final List<String> ritualObjectGroups = new ArrayList<>();
	private final String[] categoryNames = { "Nahkampfwaffe", "Fernkampfwaffe", "Schild", "Parierwaffe", "Rüstung", "Ritualobjekt", "Wertgegenstand",
			"Alchemikum", "Artefakt", "Kleidung" };

	private final String[] categoryLongNames = { "Nahkampfwaffen", "Fernkampfwaffen", "Schilde", "Parierwaffen", "Rüstung", "Ritualobjekte", "Wertgegenstände",
			"Alchemika", "Artefakte", "Kleidung" };

	private final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");

	private final JSONListener itemListener = o -> updateEquipment();
	private JSONObject hero;
	private JSONArray actualEquipment;
	private final JSONObject equipment;

	private final boolean isBaseInventory;

	public EquipmentList(final boolean isBaseInventory) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("EquipmentList.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		this.isBaseInventory = isBaseInventory;

		final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");
		DSAUtil.foreach(group -> group.getString("Ritualobjekt") != null, (name, group) -> {
			ritualObjectGroups.add(name);
		}, ritualGroups);

		equipment = ResourceManager.getResource("data/Ausruestung");

		initEquipment();
	}

	@FXML
	public void addItem() {
		String itemName = equipmentList.getSelectionModel().getSelectedItem();
		if (itemName == null) {
			itemName = equipmentList.getEditor().getText();
		}
		final JSONObject item = equipment.getObj(itemName).clone(actualEquipment);
		if (!item.containsKey("Name")) {
			item.put("Name", itemName);
		}
		equipmentList.setValue("");
		if (HeroTabController.isEditable.get()) {
			actualEquipment.add(item);
			actualEquipment.notifyListeners(null);
		} else if (item.getArr("Kategorien").getStrings().contains("Alchemikum")) {
			new PotionPurchaseDialog(pane.getScene().getWindow(), hero, actualEquipment, item);
		} else {
			new ItemPurchaseDialog(pane.getScene().getWindow(), hero, actualEquipment, item);
		}
	}

	private int findIndex(final InventoryItem item) {
		final JSONObject actual = item.getBaseItem();
		int index = actualEquipment.indexOf(actual); // Can't just indexOf(item) because there may be several which are equals but not ==
		while (actualEquipment.getObj(index) != actual) {
			index = actualEquipment.indexOf(actual, index + 1);
		}
		return index;
	}

	public Control getControl() {
		return pane;
	}

	private void initEquipment() {
		GUIUtil.autosizeTable(equipmentTable);
		GUIUtil.cellValueFactories(equipmentTable, "name", "notes");

		equipmentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		equipmentNameColumn.setCellFactory(o -> {
			final TableCell<InventoryItem, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}

				@Override
				public void updateItem(final String name, final boolean empty) {
					super.updateItem(name, empty);
					final InventoryItem item = getTableRow().getItem();
					if (item != null) {
						JSONObject referencedObject;
						if (item.getItem().containsKey("Regelwerke")) {
							referencedObject = item.getItem();
						} else if (item.getBaseItem().containsKey("Regelwerke")) {
							referencedObject = item.getBaseItem();
						} else {
							final String type = item.getItemType();
							referencedObject = equipment.getObj(type.isEmpty() ? name : type);
						}
						Util.addReference(this, referencedObject, 15, equipmentNameColumn.widthProperty());
					}
				}
			};
			return cell;
		});
		equipmentNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getBaseItem();
			item.put("Name", event.getNewValue());
			item.notifyListeners(null);
		});

		equipmentNotesColumn.setCellFactory(o -> {
			final TableCell<InventoryItem, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		equipmentNotesColumn.setOnEditCommit(event -> {
			final String note = event.getNewValue();
			final JSONObject item = event.getRowValue().getBaseItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});

		equipmentTable.setRowFactory(tableView -> {
			final TableRow<InventoryItem> row = new TableRow<>();

			GUIUtil.dragDropReorder(row, moved -> {
				if (moved.length > 0) {
					final int rowIndex = row.getIndex();

					final int indexBefore = rowIndex > 0 ? findIndex(tableView.getItems().get(rowIndex - 1)) : -1;
					int indexAfter = rowIndex + moved.length < tableView.getItems().size() ? findIndex(tableView.getItems().get(rowIndex + moved.length))
							: Integer.MAX_VALUE;

					for (final Object movedItem : moved) {
						final InventoryItem item = (InventoryItem) movedItem;
						final int index = findIndex(item);
						if (index < indexBefore) {
							actualEquipment.removeAt(index);
							actualEquipment.add(indexBefore, item.getBaseItem());
						} else if (index > indexAfter) {
							actualEquipment.removeAt(index);
							actualEquipment.add(indexAfter, item.getBaseItem());
							++indexAfter;
						} // Nothing to do if it was already in between
					}

					actualEquipment.notifyListeners(null);
				}
			}, equipmentTable);

			final ContextMenu contextMenu = new ContextMenu();

			final Runnable edit = () -> {
				final InventoryItem item = row.getItem();
				final Window window = pane.getScene().getWindow();
				if (item.getBaseItem().getArr("Kategorien").contains("Pferderüstung")) {
					new HorseArmorEditor(window, equipmentTable.getSelectionModel().getSelectedItem());
				} else {
					new ItemEditor(window, item);
				}
			};

			row.setOnMouseClicked(event -> {
				if (MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2) {
					EventTarget target = event.getTarget();
					while (!(target instanceof TableCell)) {
						target = ((Node) target).getParent();
					}
					if (target == null || !((TableCell<?, ?>) target).getTableColumn().isEditable()) {
						edit.run();
					}
				}
			});

			final MenuItem editItem = new MenuItem("Bearbeiten");
			editItem.setOnAction(event -> edit.run());

			final Menu addItem = new Menu("Hinzufügen zu ...");
			for (int i = 0; i < categoryNames.length; ++i) {
				final MenuItem addCatItem = new MenuItem(categoryLongNames[i]);
				final String categoryName = categoryNames[i];
				addCatItem.setOnAction(event -> {
					final JSONObject item = row.getItem().getBaseItem();
					JSONArray categories = item.getArr("Kategorien");
					if (categories == null) {
						categories = new JSONArray(item);
						item.put("Kategorien", categories);
					}
					if ("Ritualobjekt".equals(categoryName)) {
						boolean found = false;
						for (final String ritualObjectGroup : ritualObjectGroups) {
							final String ritualObjectName = ritualGroups.getObj(ritualObjectGroup).getString("Ritualobjekt");
							if (item.containsKey(ritualObjectName)) {
								categories.add(ritualObjectName);
								found = true;
							}
						}
						if (item.containsKey("Bannschwert")) {
							categories.add("Bannschwert");
							found = true;
						}
						if (!found) {
							categories.add("Bannschwert");
						}
					} else {
						if (!categories.contains(categoryName)) {
							categories.add(categoryName);
						}
					}
					categories.notifyListeners(null);
				});
				addItem.getItems().add(addCatItem);
			}

			final Menu location = new Menu("Ort");
			contextMenu.setOnShowing(e -> updateLocationMenu(row.getItem().getBaseItem(), location, hero));

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final JSONObject item = row.getItem().getBaseItem();
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
			});

			contextMenu.getItems().addAll(editItem, addItem, location, deleteItem);

			row.setContextMenu(contextMenu);

			return row;
		});

		updateEquipment();

		DSAUtil.foreach(item -> true, (itemName, item) -> {
			equipmentList.getItems().add(itemName);
		}, equipment);

		final ObservableList<String> unsorted = equipmentList.getItems();
		equipmentList.setItems(unsorted.sorted());
		final EventHandler<? super KeyEvent> keyPressed = equipmentList.getOnKeyPressed();
		equipmentList.setOnKeyPressed(e -> {
			keyPressed.handle(e);
			if (!e.isConsumed() && e.getCode() == KeyCode.ENTER) {
				addItem();
			}
		});
	}

	public void setHero(final JSONObject hero, final JSONObject inventory, final String name, final JSONArray inventories, final JSONArray defaultInventory) {
		this.hero = hero;
		actualEquipment = inventory.getArr("Ausrüstung");

		pane.setText(name);
		pane.setUserData(inventory);

		if (inventories != null) {
			final ContextMenu contextMenu = new ContextMenu();

			pane.setOnMouseClicked(e -> {
				if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
					new InventoryDialog(pane.getScene().getWindow(), inventories, inventory);
				}
			});

			final MenuItem editItem = new MenuItem("Bearbeiten");
			editItem.setOnAction(event -> new InventoryDialog(pane.getScene().getWindow(), inventories, inventory));
			contextMenu.getItems().add(editItem);

			final int index = inventories.indexOf(inventory);
			if (index > 0) {
				final MenuItem upItem = new MenuItem("Nach oben");
				upItem.setOnAction(event -> {
					inventories.remove(inventory);
					inventories.add(index - 1, inventory);
					inventories.notifyListeners(null);
				});
				contextMenu.getItems().add(upItem);
			}
			if (index < inventories.size() - 1) {
				final MenuItem downItem = new MenuItem("Nach unten");
				downItem.setOnAction(event -> {
					inventories.remove(inventory);
					inventories.add(index + 1, inventory);
					inventories.notifyListeners(null);
				});
				contextMenu.getItems().add(downItem);
			}

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Inventar löschen");
				alert.setHeaderText("Die Ausrüstungsgegenstände werden in das Standardinventar verschoben.");
				alert.setContentText("Soll das Inventar " + name + " wirklich gelöscht werden?");
				alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
				final Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get().equals(ButtonType.OK)) {
					DSAUtil.foreach(o -> true, item -> {
						defaultInventory.add(item.clone(defaultInventory));
					}, inventory.getArr("Ausrüstung"));
					inventories.remove(inventory);
					defaultInventory.notifyListeners(null);
					inventories.notifyListeners(null);
				}
			});
			contextMenu.getItems().add(deleteItem);

			pane.setContextMenu(contextMenu);
		}

		inventory.addListener(itemListener);

		updateEquipment();
	}

	public void unregisterListeners() {
		actualEquipment.removeListener(itemListener);
		if (isBaseInventory) {
			hero.getArr("Tiere").removeListener(itemListener);
		}
		equipmentTable.getItems().clear();
	}

	public void updateEquipment() {
		equipmentTable.getItems().clear();

		if (hero == null) return;

		if (isBaseInventory) {
			final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");

			HeroUtil.foreachInventoryItem(hero, item -> true, (item, fromExtraInventory) -> {
				final JSONArray categories = item.getArr("Kategorien");
				boolean found = false;
				if (categories != null) {
					for (final String category : List.of("Kleidung", "Nahkampfwaffe", "Fernkampfwaffe", "Schild", "Parierwaffe", "Rüstung", "Wertgegenstand",
							"Alchemikum", "Artefakt", "Bannschwert"))
						if (categories.contains(category)) {
							found = true;
						}
					for (final String ritualGroupName : ritualObjectGroups) {
						final JSONObject ritualGroup = ritualGroups.getObj(ritualGroupName);
						if (categories.contains(ritualGroup.getString("Ritualobjekt"))) {
							found = true;
						}
					}
				}
				if (!found && !fromExtraInventory) {
					final InventoryItem newItem = new InventoryItem(item, item);
					equipmentTable.getItems().add(newItem);
				}
			});
		} else {
			HeroUtil.foreachInventoryItem(true, item -> true, (item, unused) -> {
				final InventoryItem newItem = new InventoryItem(item, item);
				equipmentTable.getItems().add(newItem);
			}, actualEquipment);
		}
	}
}
