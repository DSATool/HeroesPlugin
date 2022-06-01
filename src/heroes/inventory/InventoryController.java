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
import java.util.HashMap;
import java.util.List;

import dsa41basis.fight.Armor;
import dsa41basis.fight.ArmorEditor;
import dsa41basis.fight.CloseCombatWeapon;
import dsa41basis.fight.DefensiveWeapon;
import dsa41basis.fight.RangedWeapon;
import dsa41basis.inventory.Artifact;
import dsa41basis.inventory.Clothing;
import dsa41basis.inventory.InventoryItem;
import dsa41basis.inventory.Potion;
import dsa41basis.inventory.RitualObject;
import dsa41basis.inventory.Valuable;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.resources.Settings;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import heroes.ui.HeroTabController;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;
import jsonant.value.JSONValue;

public class InventoryController extends HeroTabController {
	@FXML
	private ScrollPane pane;
	@FXML
	private VBox inventoryBox;

	@FXML
	private HBox moneyBox;
	@FXML
	private ReactiveSpinner<Integer> ducats;
	@FXML
	private ReactiveSpinner<Integer> silver;
	@FXML
	private ReactiveSpinner<Integer> heller;
	@FXML
	private ReactiveSpinner<Integer> kreuzer;

	@FXML
	private Button armorAddButton;
	@FXML
	private TableColumn<Armor, Integer> armorBackColumn;
	@FXML
	private TableColumn<Armor, Double> armorBeColumn;
	@FXML
	private TableColumn<Armor, Integer> armorBellyColumn;
	@FXML
	private TableColumn<Armor, Integer> armorBreastColumn;
	@FXML
	private TableColumn<Armor, Integer> armorHeadColumn;
	@FXML
	private TableColumn<Armor, Integer> armorLarmColumn;
	@FXML
	private ComboBox<String> armorList;
	@FXML
	private TableColumn<Armor, Integer> armorLlegColumn;
	@FXML
	private TableColumn<Armor, String> armorNameColumn;
	@FXML
	private TableColumn<Armor, Integer> armorRarmColumn;
	@FXML
	private TableColumn<Armor, Integer> armorRlegColumn;
	@FXML
	private TableColumn<Armor, Double> armorRsColumn;
	@FXML
	private TableView<Armor> armorTable;
	@FXML
	private TableColumn<Armor, Double> armorWeightColumn;

	@FXML
	private Button artifactAddButton;
	@FXML
	private TableColumn<Artifact, String> artifactNameColumn;
	@FXML
	private TableColumn<Artifact, String> artifactNotesColumn;
	@FXML
	private TableView<Artifact> artifactTable;
	@FXML
	private TextField newArtifactField;

	@FXML
	private Button closeCombatAddButton;
	@FXML
	private TableColumn<CloseCombatWeapon, Integer> closeCombatBFColumn;
	@FXML
	private TableColumn<CloseCombatWeapon, String> closeCombatNameColumn;
	@FXML
	private ComboBox<String> closeCombatList;
	@FXML
	private TableView<CloseCombatWeapon> closeCombatTable;
	@FXML
	private TableColumn<CloseCombatWeapon, Double> closeCombatWeightColumn;

	@FXML
	private ComboBox<String> clothingList;
	@FXML
	private TableColumn<Clothing, String> clothingNameColumn;
	@FXML
	private TableColumn<Clothing, String> clothingNotesColumn;
	@FXML
	private TableView<Clothing> clothingTable;

	@FXML
	private Button defensiveWeaponsAddButton;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> defensiveWeaponsBFColumn;
	@FXML
	private ComboBox<String> defensiveWeaponsList;
	@FXML
	private TableView<DefensiveWeapon> defensiveWeaponsTable;
	@FXML
	private TableColumn<DefensiveWeapon, Double> defensiveWeaponsWeightColumn;

	@FXML
	private ComboBox<String> potionsList;
	@FXML
	private TableView<Potion> potionsTable;
	@FXML
	private TableColumn<Potion, Integer> potionsAmountColumn;
	@FXML
	private TableColumn<Potion, String> potionsNameColumn;
	@FXML
	private TableColumn<Potion, String> potionsNotesColumn;
	@FXML
	private TableColumn<Potion, String> potionsQualityColumn;

	@FXML
	private Button rangedAddButton;
	@FXML
	private ComboBox<String> rangedList;
	@FXML
	private TableView<RangedWeapon> rangedTable;
	@FXML
	private TableColumn<RangedWeapon, Double> rangedWeightColumn;

	@FXML
	private Button ritualObjectAddButton;
	@FXML
	private ComboBox<String> ritualObjectList;
	@FXML
	private TableColumn<RitualObject, String> ritualObjectNameColumn;
	@FXML
	private TitledPane ritualObjectPane;
	@FXML
	private TableView<RitualObject> ritualObjectTable;
	@FXML
	private TableColumn<RitualObject, String> ritualObjectTypeColumn;

	@FXML
	private Button shieldsAddButton;
	@FXML
	private TableColumn<DefensiveWeapon, Integer> shieldsBFColumn;
	@FXML
	private ComboBox<String> shieldsList;
	@FXML
	private TableView<DefensiveWeapon> shieldsTable;
	@FXML
	private TableColumn<DefensiveWeapon, Double> shieldsWeightColumn;

	@FXML
	private Button valuablesAddButton;
	@FXML
	private TableColumn<Valuable, String> valuablesNameColumn;
	@FXML
	private TableColumn<Valuable, String> valuablesNotesColumn;
	@FXML
	private TableView<Valuable> valuablesTable;
	@FXML
	private TextField newValuableField;

	private final EquipmentList equipmentList = new EquipmentList(true);

	private final JSONObject equipment;
	private JSONArray items;
	private final JSONListener heroMoneyListener = o -> refreshMoney();
	private final JSONListener heroItemListener = o -> refreshTables();
	private final JSONListener heroInventoriesListener = o -> update();

	private final HashMap<ComboBox<String>, ObservableList<String>> itemLists = new HashMap<>();

	private final List<String> ritualObjectGroups = new ArrayList<>();

	private final String[] categoryNames = { "Nahkampfwaffe", "Fernkampfwaffe", "Schild", "Parierwaffe", "Rüstung", "Ritualobjekt", "Wertgegenstand",
			"Alchemikum", "Artefakt", "Kleidung" };
	private final String[] categoryLongNames = { "Nahkampfwaffen", "Fernkampfwaffen", "Schilde", "Parierwaffen", "Rüstung", "Ritualobjekte", "Wertgegenstände",
			"Alchemika", "Artefakte", "Kleidung" };

	private JSONObject money;

	private final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");

	public InventoryController(final TabPane tabPane) {
		super(tabPane);
		equipment = ResourceManager.getResource("data/Ausruestung");
	}

	public void addArtifact() {
		final String itemName = newArtifactField.getText();
		newArtifactField.setText("");
		final JSONObject item = new JSONObject(items);
		item.put("Name", itemName);
		item.put("Kategorien", new JSONArray(new ArrayList<>(List.of("Artefakt")), item));
		addItem(item);
	}

	@FXML
	private void addInventory() {
		new InventoryDialog(pane.getScene().getWindow(), hero.getObj("Besitz").getArr("Inventare"), null);
	}

	@SuppressWarnings("unchecked")
	public void addItem(final ActionEvent event) {
		addItem((ComboBox<String>) ((Button) event.getSource()).getParent().getChildrenUnmodifiable().get(0));
	}

	public void addItem(final ComboBox<String> list) {
		String itemName = list.getSelectionModel().getSelectedItem();
		if (itemName == null) {
			itemName = list.getEditor().getText();
		}
		final JSONObject item = equipment.getObj(itemName).clone(items);
		if (!item.containsKey("Name")) {
			item.put("Name", itemName);
		}
		if (list == potionsList && (!item.containsKey("Kategorien") || !item.getArr("Kategorien").getStrings().contains("Alchemikum"))) {
			item.getArr("Kategorien").add("Alchemikum");
		} else if (list == clothingList && (!item.containsKey("Kategorien") || !item.getArr("Kategorien").getStrings().contains("Kleidung"))) {
			item.getArr("Kategorien").add("Kleidung");
		}
		if (list == potionsList || list == clothingList) {
			list.setValue("");
		}
		addItem(item);
	}

	private void addItem(final JSONObject item) {
		if (HeroTabController.isEditable.get()) {
			items.add(item);
			items.notifyListeners(null);
		} else if (item.getArr("Kategorien").getStrings().contains("Alchemikum")) {
			new PotionPurchaseDialog(pane.getScene().getWindow(), hero, items, item);
		} else {
			new ItemPurchaseDialog(pane.getScene().getWindow(), hero, items, item);
		}
	}

	public void addValuable() {
		final String itemName = newValuableField.getText();
		newValuableField.setText("");
		final JSONObject item = new JSONObject(items);
		item.put("Name", itemName);
		item.put("Kategorien", new JSONArray(new ArrayList<>(List.of("Wertgegenstand")), item));
		addItem(item);
	}

	private int findIndex(final InventoryItem item) {
		final JSONObject actual = item.getBaseItem();
		int index = items.indexOf(actual); // Can't just indexOf(item) because there may be several which are equals but not ==
		while (items.getObj(index) != actual) {
			index = items.indexOf(actual, index + 1);
		}
		return index;
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Inventar";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Inventory.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		super.init();

		final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");
		DSAUtil.foreach(group -> group.getString("Ritualobjekt") != null, (name, group) -> {
			ritualObjectGroups.add(name);
		}, ritualGroups);

		ducats.setConverter(new IntegerStringConverter());
		silver.setConverter(new IntegerStringConverter());
		heller.setConverter(new IntegerStringConverter());
		kreuzer.setConverter(new IntegerStringConverter());

		initializeCloseCombatTable();
		initializeRangedTable();
		initializeShieldsTable();
		initializeDefensiveWeaponsTable();
		initializeArmorTable();
		initializeRitualObjectTable();
		initializeValuablesTable();
		initializePotionsTable();
		initializeArtifactTable();
		initializeClothingTable();

		for (final ComboBox<String> list : new ComboBox[] { closeCombatList, rangedList, shieldsList, defensiveWeaponsList, armorList, ritualObjectList,
				potionsList, clothingList }) {
			final ObservableList<String> unsorted = list.getItems();
			itemLists.put(list, unsorted);
			list.setItems(unsorted.sorted());
			final EventHandler<? super KeyEvent> keyPressed = list.getOnKeyPressed();
			list.setOnKeyPressed(e -> {
				keyPressed.handle(e);
				if (!e.isConsumed() && e.getCode() == KeyCode.ENTER) {
					addItem(list);
				}
			});
		}

		for (final TableView<? extends InventoryItem> table : new TableView[] { closeCombatTable, rangedTable, shieldsTable, defensiveWeaponsTable, armorTable,
				ritualObjectTable, valuablesTable, potionsTable, artifactTable, clothingTable }) {
			((TableColumn<InventoryItem, String>) table.getColumns().get(0)).setCellFactory(c -> new GraphicTableCell<>(false) {
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
						Util.addReference(this, referencedObject, 15, table.getColumns().get(0).widthProperty());
					}
				}
			});
		}

		inventoryBox.getChildren().add(inventoryBox.getChildren().size() - 1, equipmentList.getControl());

		updateLists();
	}

	private void initializeArmorTable() {
		final String armorSetting = Settings.getSettingStringOrDefault("Zonenrüstung", "Kampf", "Rüstungsart");

		if ("Zonenrüstung".equals(armorSetting)) {
			armorRsColumn.setVisible(false);
		} else {
			armorHeadColumn.setVisible(false);
			armorBreastColumn.setVisible(false);
			armorBackColumn.setVisible(false);
			armorBellyColumn.setVisible(false);
			armorLarmColumn.setVisible(false);
			armorRarmColumn.setVisible(false);
			armorLlegColumn.setVisible(false);
			armorRlegColumn.setVisible(false);
		}

		initTable(armorTable, "Rüstung", "Rüstung");

		if ("Gesamtrüstung".equals(armorSetting)) {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "totalrs", "totalbe", "weight");
		} else {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "zoners", "zonebe", "weight");
		}
	}

	private void initializeArtifactTable() {
		initTable(artifactTable, "Artefakte", "Artefakt");
		GUIUtil.cellValueFactories(artifactTable, "name", "notes");
	}

	private void initializeCloseCombatTable() {
		initTable(closeCombatTable, "Nahkampfwaffen", "Nahkampfwaffe");
		GUIUtil.cellValueFactories(closeCombatTable, "name", "tp", "tpkk", "weight", "length", "bf", "ini", "wm", "special", "dk");

		closeCombatBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		closeCombatBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});
	}

	private void initializeClothingTable() {
		initTable(clothingTable, "Kleidung", "Kleidung");
		GUIUtil.cellValueFactories(clothingTable, "name", "notes");

		clothingNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getBaseItem();
			item.put("Name", event.getNewValue());
			item.notifyListeners(null);
		});

		clothingNotesColumn.setCellFactory(o -> {
			final TableCell<Clothing, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		clothingNotesColumn.setOnEditCommit(event -> {
			final String note = event.getNewValue();
			final JSONObject item = event.getRowValue().getBaseItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});
	}

	private void initializeDefensiveWeaponsTable() {
		initTable(defensiveWeaponsTable, "Parierwaffen", "Parierwaffe");
		GUIUtil.cellValueFactories(defensiveWeaponsTable, "name", "wm", "ini", "bf", "weight");

		defensiveWeaponsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		defensiveWeaponsBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});
	}

	private void initializePotionsTable() {
		initTable(potionsTable, "Alchemika", "Alchemikum");
		GUIUtil.cellValueFactories(potionsTable, "name", "notes", "quality", "amount");

		DoubleBinding potionsWidth = potionsTable.widthProperty().subtract(2);
		potionsWidth = potionsWidth.subtract(potionsQualityColumn.widthProperty());
		potionsWidth = potionsWidth.subtract(potionsAmountColumn.widthProperty());
		potionsWidth = potionsWidth.divide(2);

		potionsNameColumn.prefWidthProperty().bind(potionsWidth);
		potionsNotesColumn.prefWidthProperty().bind(potionsWidth);

		potionsAmountColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99));
		potionsAmountColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setAmount(t.getNewValue());
			}
		});
	}

	private void initializeRangedTable() {
		initTable(rangedTable, "Fernkampfwaffen", "Fernkampfwaffe");
		GUIUtil.cellValueFactories(rangedTable, "name", "tp", "distance", "distancetp", "weight", "load");
	}

	private void initializeRitualObjectTable() {
		initTable(ritualObjectTable, "Ritualobjekte", "Ritualobjekt");
		GUIUtil.cellValueFactories(ritualObjectTable, "name", "type");
	}

	private void initializeShieldsTable() {
		initTable(shieldsTable, "Schilde", "Schild");
		GUIUtil.cellValueFactories(shieldsTable, "name", "wm", "ini", "bf", "weight");

		shieldsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12));
		shieldsBFColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setBf(t.getNewValue());
			}
		});
	}

	private void initializeValuablesTable() {
		initTable(valuablesTable, "Wertgegenstände", "Wertgegenstand");
		GUIUtil.cellValueFactories(valuablesTable, "name", "notes");

		valuablesNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getBaseItem();
			item.put("Name", event.getNewValue());
			item.notifyListeners(null);
		});

		valuablesNotesColumn.setCellFactory(o -> {
			final TableCell<Valuable, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		valuablesNotesColumn.setOnEditCommit(event -> {
			final String note = event.getNewValue();
			final JSONObject item = event.getRowValue().getBaseItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});
	}

	private <T extends InventoryItem> void initTable(final TableView<T> table, final String name, final String category) {
		GUIUtil.autosizeTable(table);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setRowFactory(tableView -> {
			final TableRow<T> row = new TableRow<>();

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
							items.removeAt(index);
							items.add(indexBefore, item.getBaseItem());
						} else if (index > indexAfter) {
							items.removeAt(index);
							items.add(indexAfter, item.getBaseItem());
							++indexAfter;
						} // Nothing to do if it was already in between
					}

					items.notifyListeners(null);
				}
			}, tableView);

			final ContextMenu rowMenu = new ContextMenu();

			final Runnable edit = () -> {
				final InventoryItem item = row.getItem();
				final Window window = pane.getScene().getWindow();
				switch (category) {
					case "Nahkampfwaffe" -> new CloseCombatWeaponEditor(window, (CloseCombatWeapon) item);
					case "Fernkampfwaffe" -> new RangedWeaponEditor(window, (RangedWeapon) item);
					case "Schild", "Parierwaffe" -> new DefensiveWeaponEditor(window, (DefensiveWeapon) item);
					case "Rüstung" -> new ArmorEditor(window, (Armor) item);
					case "Ritualobjekt" -> new RitualObjectEditor(window, hero, (RitualObject) item);
					case "Wertgegenstand" -> new ValuableEditor(window, (Valuable) item);
					case "Alchemikum" -> new PotionEditor(window, (Potion) item);
					case "Artefakt" -> new ArtifactEditor(window, (Artifact) item);
					case "Kleidung" -> new ClothingEditor(window, (Clothing) item);
					default -> new ItemEditor(window, item);
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

			final MenuItem removeItem = new MenuItem("Entfernen aus " + name);
			removeItem.setOnAction(event -> {
				final JSONObject item = row.getItem().getBaseItem();
				final JSONArray categories = item.getArr("Kategorien");

				if ("Ritualobjekte".equals(name)) {
					for (final String ritualObjectGroup : ritualObjectGroups) {
						final String ritualObjectName = ritualGroups.getObj(ritualObjectGroup).getString("Ritualobjekt");
						categories.remove(ritualObjectName);
					}
					categories.remove("Bannschwert");
				} else {
					categories.remove(category);
				}

				if (categories.size() == 0) {
					item.removeKey("Kategorien");
				}
				item.notifyListeners(null);
			});

			final Menu location = new Menu("Ort");
			rowMenu.setOnShowing(e -> EquipmentList.updateLocationMenu(row.getItem().getBaseItem(), location, hero));

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final JSONObject item = row.getItem().getBaseItem();
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
			});

			rowMenu.getItems().addAll(editItem, addItem);
			if (!"Ausrüstung".equals(category)) {
				rowMenu.getItems().add(removeItem);
			}
			rowMenu.getItems().addAll(location, deleteItem);
			row.setContextMenu(rowMenu);

			return row;
		});
	}

	private void refreshMoney() {
		final JSONObject money = hero.getObj("Besitz").getObj("Geld");
		ducats.getValueFactory().setValue(money.getIntOrDefault("Dukaten", 0));
		silver.getValueFactory().setValue(money.getIntOrDefault("Silbertaler", 0));
		heller.getValueFactory().setValue(money.getIntOrDefault("Heller", 0));
		kreuzer.getValueFactory().setValue(money.getIntOrDefault("Kreuzer", 0));
	}

	private void refreshTables() {
		closeCombatTable.getItems().clear();
		rangedTable.getItems().clear();
		shieldsTable.getItems().clear();
		defensiveWeaponsTable.getItems().clear();
		armorTable.getItems().clear();
		ritualObjectTable.getItems().clear();
		valuablesTable.getItems().clear();
		potionsTable.getItems().clear();
		artifactTable.getItems().clear();
		clothingTable.getItems().clear();

		final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");

		HeroUtil.foreachInventoryItem(hero, item -> true, (item, fromExtraInventory) -> {
			final JSONArray categories = item.getArr("Kategorien");
			if (categories != null) {
				if (categories.contains("Kleidung")) {
					final JSONObject actual = item.getObjOrDefault("Kleidung", item);
					final Clothing newItem = new Clothing(actual, item);
					clothingTable.getItems().add(newItem);
				}
				if (categories.contains("Nahkampfwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Nahkampfwaffe", item);
					closeCombatTable.getItems()
							.add(new CloseCombatWeapon(null, actual, item, ResourceManager.getResource("data/Talente").getObj("Nahkampftalente"), null));
				}
				if (categories.contains("Fernkampfwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Fernkampfwaffe", item);
					rangedTable.getItems()
							.add(new RangedWeapon(null, actual, item, ResourceManager.getResource("data/Talente").getObj("Fernkampftalente"), null));
				}
				if (categories.contains("Schild")) {
					final JSONObject actual = item.getObjOrDefault("Schild", item);
					shieldsTable.getItems().add(new DefensiveWeapon(true, null, actual, item));
				}
				if (categories.contains("Parierwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Parierwaffe", item);
					defensiveWeaponsTable.getItems().add(new DefensiveWeapon(false, null, actual, item));
				}
				if (categories.contains("Rüstung")) {
					final JSONObject actual = item.getObjOrDefault("Rüstung", item);
					armorTable.getItems().add(new Armor(actual, item));
				}
				if (categories.contains("Wertgegenstand")) {
					final JSONObject actual = item.getObjOrDefault("Wertgegenstand", item);
					valuablesTable.getItems().add(new Valuable(actual, item));
				}
				if (categories.contains("Alchemikum")) {
					final JSONObject actual = item.getObjOrDefault("Alchemikum", item);
					potionsTable.getItems().add(new Potion(actual, item));
				}
				if (categories.contains("Artefakt")) {
					final JSONObject actual = item.getObjOrDefault("Artefakt", item);
					artifactTable.getItems().add(new Artifact(actual, item));
				}
				for (final String ritualGroupName : ritualObjectGroups) {
					final JSONObject ritualGroup = ritualGroups.getObj(ritualGroupName);
					if (categories.contains(ritualGroup.getString("Ritualobjekt"))) {
						final JSONObject actual = item.getObjOrDefault(ritualGroup.getString("Ritualobjekt"), item);
						ritualObjectTable.getItems().add(new RitualObject(actual, item, ritualGroupName));
					}
				}
				if (categories.contains("Bannschwert")) {
					final JSONObject actual = item.getObjOrDefault("Bannschwert", item);
					ritualObjectTable.getItems().add(new RitualObject(actual, item, "Bannschwert"));
				}
			}
		});

	}

	@Override
	protected void registerListeners() {
		money.addLocalListener(heroMoneyListener);
		if (items != null) {
			items.addListener(heroItemListener);
		}
		hero.getObj("Besitz").addListener(heroInventoriesListener);
		hero.getArr("Tiere").addListener(heroItemListener);
	}

	private void setState(final ComboBox<String> list, final Button button) {
		if (list.getItems().size() > 0) {
			list.getSelectionModel().select(0);
			button.setDisable(false);
		} else {
			button.setDisable(true);
		}
	}

	@Override
	protected void unregisterListeners() {
		hero.getObj("Besitz").getObj("Geld").removeListener(heroMoneyListener);
		items.removeListener(heroItemListener);
		hero.getObj("Besitz").remove(heroInventoriesListener);
		hero.getArr("Tiere").removeListener(heroItemListener);
		closeCombatTable.getItems().clear();
		rangedTable.getItems().clear();
		shieldsTable.getItems().clear();
		defensiveWeaponsTable.getItems().clear();
		armorTable.getItems().clear();
		ritualObjectTable.getItems().clear();
		valuablesTable.getItems().clear();
		potionsTable.getItems().clear();
		artifactTable.getItems().clear();
		clothingTable.getItems().clear();
		equipmentList.unregisterListeners();
	}

	@Override
	protected void update() {
		final JSONObject possessions = hero.getObj("Besitz");

		money = possessions.getObj("Geld");

		refreshMoney();

		ducats.valueProperty().addListener((o, oldV, newV) -> {
			if (newV == null || oldV == null || oldV.equals(newV) || newV.equals(money.getIntOrDefault("Dukaten", 0))) return;
			money.put("Dukaten", newV);
			money.notifyListeners(heroMoneyListener);
		});
		silver.valueProperty().addListener((o, oldV, newV) -> {
			if (newV == null || oldV == null || oldV.equals(newV) || newV.equals(money.getIntOrDefault("Silbertaler", 0))) return;
			money.put("Silbertaler", newV);
			money.notifyListeners(heroMoneyListener);
		});
		heller.valueProperty().addListener((o, oldV, newV) -> {
			if (newV == null || oldV == null || oldV.equals(newV) || newV.equals(money.getIntOrDefault("Heller", 0))) return;
			money.put("Heller", newV);
			money.notifyListeners(heroMoneyListener);
		});
		kreuzer.valueProperty().addListener((o, oldV, newV) -> {
			if (newV == null || oldV == null || oldV.equals(newV) || newV.equals(money.getIntOrDefault("Kreuzer", 0))) return;
			money.put("Kreuzer", newV);
			money.notifyListeners(heroMoneyListener);
		});

		items = possessions.getArr("Ausrüstung");

		inventoryBox.getChildren().remove(ritualObjectPane);
		if (HeroUtil.isMagical(hero)) {
			inventoryBox.getChildren().add(inventoryBox.getChildren().size() - 2, ritualObjectPane);
		}

		refreshTables();

		equipmentList.setHero(hero, possessions, "Ausrüstung", null, items);

		if (inventoryBox.getChildren().size() > 11) {
			inventoryBox.getChildren().remove(11, inventoryBox.getChildren().size() - 1);
		}

		final JSONArray inventories = possessions.getArrOrDefault("Inventare", null);

		DSAUtil.foreach(inventory -> true, inventory -> {
			final EquipmentList list = new EquipmentList(false);

			final String name = inventory.getStringOrDefault("Name", "Unbenanntes Inventar");
			list.setHero(hero, inventory, name, inventories, possessions.getArr("Ausrüstung"));

			inventoryBox.getChildren().add(inventoryBox.getChildren().size() - 1, list.getControl());

			GUIUtil.dragDropReorder(list.getControl(), moved -> {
				final int index = inventoryBox.getChildren().indexOf(moved) - 11;
				final JSONObject current = (JSONObject) moved.getUserData();
				inventories.remove(current);
				inventories.add(index, current);
				inventories.notifyListeners(null);
			}, inventoryBox);
		}, inventories);
	}

	private void updateLists() {
		itemLists.values().forEach(ObservableList::clear);

		DSAUtil.foreach(item -> true, (itemName, item) -> {
			final JSONArray categories = item.getArr("Kategorien");
			if (categories.contains("Kleidung")) {
				itemLists.get(clothingList).add(itemName);
			}
			if (categories.contains("Nahkampfwaffe")) {
				itemLists.get(closeCombatList).add(itemName);
			}
			if (categories.contains("Fernkampfwaffe")) {
				itemLists.get(rangedList).add(itemName);
			}
			if (categories.contains("Schild")) {
				itemLists.get(shieldsList).add(itemName);
			}
			if (categories.contains("Parierwaffe")) {
				itemLists.get(defensiveWeaponsList).add(itemName);
			}
			if (categories.contains("Rüstung")) {
				itemLists.get(armorList).add(itemName);
			}
			if (categories.contains("Alchemikum")) {
				itemLists.get(potionsList).add(itemName);
			}
			for (final String ritualGroupName : ritualObjectGroups) {
				final JSONObject ritualGroup = ritualGroups.getObj(ritualGroupName);
				if (categories.contains(ritualGroup.getString("Ritualobjekt"))) {
					itemLists.get(ritualObjectList).add(itemName);
					break;
				}
			}
		}, equipment);

		setState(closeCombatList, closeCombatAddButton);
		setState(rangedList, rangedAddButton);
		setState(shieldsList, shieldsAddButton);
		setState(defensiveWeaponsList, defensiveWeaponsAddButton);
		setState(armorList, armorAddButton);
		setState(ritualObjectList, ritualObjectAddButton);
	}
}
