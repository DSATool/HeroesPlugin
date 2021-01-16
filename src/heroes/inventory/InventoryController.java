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
import java.util.Map;

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
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
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
	private ComboBox<String> equipmentList;
	@FXML
	private TableColumn<InventoryItem, String> equipmentNameColumn;
	@FXML
	private TableColumn<InventoryItem, String> equipmentNotesColumn;
	@FXML
	private TableView<InventoryItem> equipmentTable;

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

	private final JSONObject equipment;
	private JSONArray items;
	private final JSONListener heroMoneyListener = o -> refreshMoney();
	private final JSONListener heroItemListener = o -> refreshTables();

	private final HashMap<ComboBox<String>, ObservableList<String>> itemLists = new HashMap<>();

	private final List<String> ritualObjectGroups = new ArrayList<>();

	private final String[] categoryNames = { "Nahkampfwaffe", "Fernkampfwaffe", "Schild", "Parierwaffe", "Rüstung", "Ritualobjekt", "Wertrgegenstand",
			"Alchemikum", "Artefakt", "Kleidung" };
	private final String[] categoryLongNames = { "Nahkampfwaffen", "Fernkampfwaffen", "Schilde", "Parierwaffen", "Rüstung", "Ritualobjekte", "Wertgegenstände",
			"Alchemika", "Artefakte", "Kleidung" };

	private JSONObject money;

	private final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");
	private final JSONListener equipmentListener = o -> updateLists();

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

	@SuppressWarnings("unchecked")
	public void addItem(final ActionEvent event) {
		addItem((ComboBox<String>) ((Button) event.getSource()).getParent().getChildrenUnmodifiable().get(0));
	}

	public void addItem(final ComboBox<String> list) {
		final String itemName = list.getSelectionModel().getSelectedItem();
		final JSONObject item = equipment.getObj(itemName).clone(items);
		if (!item.containsKey("Name")) {
			item.put("Name", itemName);
		}
		if (list == potionsList && (!item.containsKey("Kategorien") || !item.getArr("Kategorien").getStrings().contains("Alchemikum"))) {
			item.getArr("Kategorien").add("Alchemikum");
		} else if (list == clothingList && (!item.containsKey("Kategorien") || !item.getArr("Kategorien").getStrings().contains("Kleidung"))) {
			item.getArr("Kategorien").add("Kleidung");
		}
		if (list == potionsList || list == clothingList || list == equipmentList) {
			list.setValue("");
		}
		addItem(item);
	}

	private void addItem(final JSONObject item) {
		if (HeroTabController.isEditable.get()) {
			items.add(item);
			items.notifyListeners(null);
		} else if (item.getArr("Kategorien").getStrings().contains("Alchemikum")) {
			new PotionPurchaseDialog(pane.getScene().getWindow(), hero, item);
		} else {
			new ItemPurchaseDialog(pane.getScene().getWindow(), hero, item);
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

	private final <T extends InventoryItem> Callback<TableView<T>, TableRow<T>> contextMenu(final String name, final String category) {
		return tableView -> {
			final TableRow<T> row = new TableRow<>();

			row.setOnDragDetected(e -> {
				if (row.isEmpty()) return;
				final Dragboard dragBoard = tableView.startDragAndDrop(TransferMode.MOVE);
				final ClipboardContent content = new ClipboardContent();
				content.put(DataFormat.PLAIN_TEXT, row.getIndex());
				dragBoard.setContent(content);
				e.consume();
			});

			row.setOnDragDropped(e -> {
				final InventoryItem toMove = tableView.getItems().get((Integer) e.getDragboard().getContent(DataFormat.PLAIN_TEXT));
				int appearance = 0;
				int index = tableView.getItems().indexOf(toMove);
				while (index != (Integer) e.getDragboard().getContent(DataFormat.PLAIN_TEXT)) {
					++appearance;
					index += 1 + tableView.getItems().subList(index + 1, tableView.getItems().size()).indexOf(toMove);
				}
				final JSONObject item = toMove.getItem();
				index = items.indexOf(item);
				for (; appearance > 0; --appearance) {
					index = items.indexOf(item, index + 1);
				}
				items.removeAt(index);
				final InventoryItem target = row.getItem();
				appearance = 0;
				int targetIndex = tableView.getItems().indexOf(target);
				while (targetIndex != row.getIndex()) {
					++appearance;
					targetIndex += 1 + tableView.getItems().subList(targetIndex + 1, tableView.getItems().size()).indexOf(target);
				}
				final JSONObject targetItem = target.getItem();
				targetIndex = items.indexOf(targetItem);
				for (; appearance > 0; --appearance) {
					targetIndex = items.indexOf(targetItem, targetIndex + 1);
				}
				if (targetIndex == -1 || targetIndex > items.size()) {
					items.add(item);
				} else {
					items.add(targetIndex, item);
				}
				e.setDropCompleted(true);
				items.notifyListeners(null);
			});

			row.setOnDragOver(e -> {
				if (e.getGestureSource() == row.getTableView()) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
			});

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
				if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
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
					final JSONObject item = row.getItem().getItem();
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
				final JSONObject item = row.getItem().getItem();
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
			rowMenu.setOnShowing(e -> updateLocationMenu(row.getItem().getItem(), location));

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final JSONObject item = row.getItem().getItem();
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
		};
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
		initializeEquipmentTable();

		for (final ComboBox<String> list : new ComboBox[] { closeCombatList, rangedList, shieldsList, defensiveWeaponsList, armorList, ritualObjectList,
				potionsList, clothingList, equipmentList }) {
			final ObservableList<String> unsorted = list.getItems();
			itemLists.put(list, unsorted);
			list.setItems(unsorted.sorted());
			final EventHandler<? super KeyEvent> keyPressed = list.getOnKeyPressed();
			list.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.ENTER) {
					addItem(list);
				} else {
					keyPressed.handle(e);
				}
			});
		}

		for (final TableView<? extends InventoryItem> table : new TableView[] { closeCombatTable, rangedTable, shieldsTable, defensiveWeaponsTable, armorTable,
				ritualObjectTable, potionsTable, clothingTable, equipmentTable }) {
			((TableColumn<InventoryItem, String>) table.getColumns().get(0)).setCellFactory(c -> new TextFieldTableCell<>() {
				@Override
				public void updateItem(final String name, final boolean empty) {
					super.updateItem(name, empty);
					final InventoryItem item = getTableRow().getItem();
					if (item != null) {
						final String type = item.getItemType();
						Util.addReference(this, equipment.getObj(type.isEmpty() ? name : type), 15, table.getColumns().get(0).widthProperty());
					}
				}
			});
		}

		equipment.addListener(equipmentListener);

		updateLists();
	}

	private void initializeArmorTable() {
		final String armorSetting = Settings.getSettingStringOrDefault("Zonenrüstung", "Kampf", "Rüstungsart");

		DoubleBinding armorWidth = armorTable.widthProperty().subtract(2);
		if ("Zonenrüstung".equals(armorSetting)) {
			armorWidth = armorWidth.subtract(armorHeadColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorBreastColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorBackColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorBellyColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorLarmColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorRarmColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorLlegColumn.widthProperty());
			armorWidth = armorWidth.subtract(armorRlegColumn.widthProperty());
			armorRsColumn.setVisible(false);
		} else {
			armorWidth = armorWidth.subtract(armorRsColumn.widthProperty());
			armorHeadColumn.setVisible(false);
			armorBreastColumn.setVisible(false);
			armorBackColumn.setVisible(false);
			armorBellyColumn.setVisible(false);
			armorLarmColumn.setVisible(false);
			armorRarmColumn.setVisible(false);
			armorLlegColumn.setVisible(false);
			armorRlegColumn.setVisible(false);
		}
		armorWidth = armorWidth.subtract(armorBeColumn.widthProperty());
		armorWidth = armorWidth.subtract(armorWeightColumn.widthProperty());

		armorNameColumn.prefWidthProperty().bind(armorWidth);
		if ("Gesamtrüstung".equals(armorSetting)) {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "totalrs", "totalbe", "weight");
		} else {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "zoners", "zonebe", "weight");
		}

		armorTable.setRowFactory(contextMenu("Rüstung", "Rüstung"));
	}

	private void initializeArtifactTable() {
		GUIUtil.cellValueFactories(artifactTable, "name", "notes");

		artifactTable.setRowFactory(contextMenu("Artefakte", "Artefakt"));
	}

	private void initializeCloseCombatTable() {
		GUIUtil.autosizeTable(closeCombatTable, 0, 2);
		GUIUtil.cellValueFactories(closeCombatTable, "name", "tp", "tpkk", "weight", "length", "bf", "ini", "wm", "special", "dk");

		closeCombatBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12, 1, false));
		closeCombatBFColumn.setOnEditCommit(t -> t.getRowValue().setBf(t.getNewValue()));

		closeCombatTable.setRowFactory(contextMenu("Nahkampfwaffen", "Nahkampfwaffe"));
	}

	private void initializeClothingTable() {
		GUIUtil.cellValueFactories(clothingTable, "name", "notes");

		clothingTable.setRowFactory(contextMenu("Kleidung", "Kleidung"));

		clothingNameColumn.setCellFactory(o -> {
			final TableCell<Clothing, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		clothingNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getItem();
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
			final JSONObject item = event.getRowValue().getItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});
	}

	private void initializeDefensiveWeaponsTable() {
		GUIUtil.autosizeTable(defensiveWeaponsTable, 0, 2);
		GUIUtil.cellValueFactories(defensiveWeaponsTable, "name", "wm", "ini", "bf", "weight");

		defensiveWeaponsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12, 1, false));
		defensiveWeaponsBFColumn.setOnEditCommit(t -> t.getRowValue().setBf(t.getNewValue()));

		defensiveWeaponsTable.setRowFactory(contextMenu("Parierwaffen", "Parierwaffe"));
	}

	private void initializeEquipmentTable() {
		GUIUtil.cellValueFactories(equipmentTable, "name", "notes");

		equipmentTable.setRowFactory(contextMenu("Ausrüstung", ""));

		equipmentNameColumn.setCellFactory(o -> {
			final TableCell<InventoryItem, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		equipmentNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getItem();
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
			final JSONObject item = event.getRowValue().getItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});
	}

	private void initializePotionsTable() {
		GUIUtil.cellValueFactories(potionsTable, "name", "notes", "quality", "amount");

		DoubleBinding potionsWidth = potionsTable.widthProperty().subtract(2);
		potionsWidth = potionsWidth.subtract(potionsQualityColumn.widthProperty());
		potionsWidth = potionsWidth.subtract(potionsAmountColumn.widthProperty());
		potionsWidth = potionsWidth.divide(2);

		potionsNameColumn.prefWidthProperty().bind(potionsWidth);
		potionsNotesColumn.prefWidthProperty().bind(potionsWidth);

		potionsAmountColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99, 1, false));
		potionsAmountColumn.setOnEditCommit(t -> t.getRowValue().setAmount(t.getNewValue()));

		potionsTable.setRowFactory(contextMenu("Alchemika", "Alchemikum"));
	}

	private void initializeRangedTable() {
		GUIUtil.autosizeTable(rangedTable, 0, 2);
		GUIUtil.cellValueFactories(rangedTable, "name", "tp", "distance", "distancetp", "weight", "load");

		rangedTable.setRowFactory(contextMenu("Fernkampfwaffen", "Fernkampfwaffe"));
	}

	private void initializeRitualObjectTable() {
		GUIUtil.autosizeTable(ritualObjectTable, 0, 2);
		GUIUtil.cellValueFactories(ritualObjectTable, "name", "type");

		ritualObjectTable.setRowFactory(contextMenu("Ritualobjekte", "Ritualobjekt"));
	}

	private void initializeShieldsTable() {
		GUIUtil.autosizeTable(shieldsTable, 0, 2);
		GUIUtil.cellValueFactories(shieldsTable, "name", "wm", "ini", "bf", "weight");

		shieldsBFColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-12, 12, 1, false));
		shieldsBFColumn.setOnEditCommit(t -> t.getRowValue().setBf(t.getNewValue()));

		shieldsTable.setRowFactory(contextMenu("Schilde", "Schild"));
	}

	private void initializeValuablesTable() {
		GUIUtil.cellValueFactories(valuablesTable, "name", "notes");

		valuablesNameColumn.setCellFactory(o -> {
			final TableCell<Valuable, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, t::getText, t::setText);
				}
			};
			return cell;
		});
		valuablesNameColumn.setOnEditCommit(event -> {
			final JSONObject item = event.getRowValue().getItem();
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
			final JSONObject item = event.getRowValue().getItem();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			item.notifyListeners(null);
		});

		valuablesTable.setRowFactory(contextMenu("Wertgegenstände", "Wertgegenstand"));
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
		equipmentTable.getItems().clear();

		final JSONObject ritualGroups = ResourceManager.getResource("data/Ritualgruppen");

		HeroUtil.foreachInventoryItem(hero, item -> true, (item, fromAnimal) -> {
			final JSONArray categories = item.getArr("Kategorien");
			boolean found = false;
			if (categories != null) {
				if (categories.contains("Kleidung")) {
					final JSONObject actual = item.getObjOrDefault("Kleidung", item);
					final Clothing newItem = new Clothing(actual, item);
					clothingTable.getItems().add(newItem);
					found = true;
				}
				if (categories.contains("Nahkampfwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Nahkampfwaffe", item);
					closeCombatTable.getItems()
							.add(new CloseCombatWeapon(null, actual, item, ResourceManager.getResource("data/Talente").getObj("Nahkampftalente"), null));
					found = true;
				}
				if (categories.contains("Fernkampfwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Fernkampfwaffe", item);
					rangedTable.getItems()
							.add(new RangedWeapon(null, actual, item, ResourceManager.getResource("data/Talente").getObj("Fernkampftalente"), null));
					found = true;
				}
				if (categories.contains("Schild")) {
					final JSONObject actual = item.getObjOrDefault("Schild", item);
					shieldsTable.getItems().add(new DefensiveWeapon(true, null, actual, item));
					found = true;
				}
				if (categories.contains("Parierwaffe")) {
					final JSONObject actual = item.getObjOrDefault("Parierwaffe", item);
					defensiveWeaponsTable.getItems().add(new DefensiveWeapon(false, null, actual, item));
					found = true;
				}
				if (categories.contains("Rüstung")) {
					final JSONObject actual = item.getObjOrDefault("Rüstung", item);
					armorTable.getItems().add(new Armor(actual, item));
					found = true;
				}
				if (categories.contains("Wertgegenstand")) {
					final JSONObject actual = item.getObjOrDefault("Wertgegenstand", item);
					valuablesTable.getItems().add(new Valuable(actual, item));
					found = true;
				}
				if (categories.contains("Alchemikum")) {
					final JSONObject actual = item.getObjOrDefault("Alchemikum", item);
					potionsTable.getItems().add(new Potion(actual, item));
					found = true;
				}
				if (categories.contains("Artefakt")) {
					final JSONObject actual = item.getObjOrDefault("Artefakt", item);
					artifactTable.getItems().add(new Artifact(actual, item));
					found = true;
				}
				for (final String ritualGroupName : ritualObjectGroups) {
					final JSONObject ritualGroup = ritualGroups.getObj(ritualGroupName);
					if (categories.contains(ritualGroup.getString("Ritualobjekt"))) {
						final JSONObject actual = item.getObjOrDefault(ritualGroup.getString("Ritualobjekt"), item);
						ritualObjectTable.getItems().add(new RitualObject(actual, item, ritualGroupName));
						found = true;
					}
				}
				if (categories.contains("Bannschwert")) {
					final JSONObject actual = item.getObjOrDefault("Bannschwert", item);
					ritualObjectTable.getItems().add(new RitualObject(actual, item, "Bannschwert"));
					found = true;
				}
			}
			if (!found && !fromAnimal) {
				final InventoryItem newItem = new InventoryItem(item, item);
				equipmentTable.getItems().add(newItem);
			}
		});

		closeCombatTable.setPrefHeight((closeCombatTable.getItems().size() + 1) * 25 + 1);
		closeCombatTable.setMinHeight((closeCombatTable.getItems().size() + 1) * 25 + 1);
		rangedTable.setPrefHeight((rangedTable.getItems().size() + 1) * 25 + 1);
		rangedTable.setMinHeight((rangedTable.getItems().size() + 1) * 25 + 1);
		shieldsTable.setPrefHeight((shieldsTable.getItems().size() + 1) * 25 + 1);
		shieldsTable.setMinHeight((shieldsTable.getItems().size() + 1) * 25 + 1);
		defensiveWeaponsTable.setPrefHeight((defensiveWeaponsTable.getItems().size() + 1) * 25 + 1);
		defensiveWeaponsTable.setMinHeight((defensiveWeaponsTable.getItems().size() + 1) * 25 + 1);
		armorTable.setPrefHeight((armorTable.getItems().size() + 1) * 25 + 1);
		armorTable.setMinHeight((armorTable.getItems().size() + 1) * 25 + 1);
		ritualObjectTable.setPrefHeight((ritualObjectTable.getItems().size() + 1) * 25 + 1);
		ritualObjectTable.setMinHeight((ritualObjectTable.getItems().size() + 1) * 25 + 1);
		valuablesTable.setPrefHeight((valuablesTable.getItems().size() + 1) * 25 + 1);
		valuablesTable.setMinHeight((valuablesTable.getItems().size() + 1) * 25 + 1);
		potionsTable.setPrefHeight((potionsTable.getItems().size() + 1) * 25 + 1);
		potionsTable.setMinHeight((potionsTable.getItems().size() + 1) * 25 + 1);
		artifactTable.setPrefHeight((artifactTable.getItems().size() + 1) * 25 + 1);
		artifactTable.setMinHeight((artifactTable.getItems().size() + 1) * 25 + 1);
		clothingTable.setPrefHeight((clothingTable.getItems().size() + 1) * 26 + 1);
		clothingTable.setMinHeight((clothingTable.getItems().size() + 1) * 26 + 1);
		equipmentTable.setPrefHeight((equipmentTable.getItems().size() + 1) * 26 + 1);
		equipmentTable.setMinHeight((equipmentTable.getItems().size() + 1) * 26 + 1);
	}

	@Override
	protected void registerListeners() {
		money.addLocalListener(heroMoneyListener);
		if (items != null) {
			items.addListener(heroItemListener);
		}
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
		equipmentTable.getItems().clear();
	}

	@Override
	protected void update() {
		final JSONObject inventory = hero.getObj("Besitz");

		money = inventory.getObj("Geld");

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

		items = inventory.getArr("Ausrüstung");

		inventoryBox.getChildren().remove(ritualObjectPane);
		if (HeroUtil.isMagical(hero)) {
			inventoryBox.getChildren().add(inventoryBox.getChildren().size() - 2, ritualObjectPane);
		}

		refreshTables();
	}

	private void updateLists() {
		itemLists.values().forEach(ObservableList::clear);

		DSAUtil.foreach(item -> true, (itemName, item) -> {
			final JSONArray categories = item.getArr("Kategorien");
			boolean found = false;
			if (categories.contains("Kleidung")) {
				itemLists.get(clothingList).add(itemName);
				found = true;
			}
			if (categories.contains("Nahkampfwaffe")) {
				itemLists.get(closeCombatList).add(itemName);
				found = true;
			}
			if (categories.contains("Fernkampfwaffe")) {
				itemLists.get(rangedList).add(itemName);
				found = true;
			}
			if (categories.contains("Schild")) {
				itemLists.get(shieldsList).add(itemName);
				found = true;
			}
			if (categories.contains("Parierwaffe")) {
				itemLists.get(defensiveWeaponsList).add(itemName);
				found = true;
			}
			if (categories.contains("Rüstung")) {
				itemLists.get(armorList).add(itemName);
				found = true;
			}
			if (categories.contains("Alchemikum")) {
				itemLists.get(potionsList).add(itemName);
				found = true;
			}
			for (final String ritualGroupName : ritualObjectGroups) {
				final JSONObject ritualGroup = ritualGroups.getObj(ritualGroupName);
				if (categories.contains(ritualGroup.getString("Ritualobjekt"))) {
					itemLists.get(ritualObjectList).add(itemName);
					found = true;
					break;
				}
			}
			if (!found) {
				itemLists.get(equipmentList).add(itemName);
			}
		}, equipment);

		setState(closeCombatList, closeCombatAddButton);
		setState(rangedList, rangedAddButton);
		setState(shieldsList, shieldsAddButton);
		setState(defensiveWeaponsList, defensiveWeaponsAddButton);
		setState(armorList, armorAddButton);
		setState(ritualObjectList, ritualObjectAddButton);
	}

	private void updateLocationMenu(final JSONObject item, final Menu location) {
		location.getItems().clear();
		final ToggleGroup locationGroup = new ToggleGroup();
		final RadioMenuItem heroLocationItem = new RadioMenuItem(hero.getObj("Biografie").getString("Vorname"));
		heroLocationItem.setToggleGroup(locationGroup);
		location.getItems().add(heroLocationItem);
		final JSONArray animals = hero.getArr("Tiere");
		final Map<JSONObject, RadioMenuItem> names = new HashMap<>(animals.size());
		for (int i = 0; i < animals.size(); ++i) {
			final JSONObject animal = animals.getObj(i);
			final String name = animal.getObj("Biografie").getString("Name");
			final RadioMenuItem animalLocationItem = new RadioMenuItem(name);
			animalLocationItem.setToggleGroup(locationGroup);
			names.put(animal, animalLocationItem);
			location.getItems().add(animalLocationItem);
		}

		final JSONValue possessor = item.getParent() != null ? item.getParent().getParent() : null;
		if (possessor != null && possessor.getParent() instanceof JSONObject) {
			heroLocationItem.setSelected(true);
			for (final JSONObject animal : names.keySet()) {
				names.get(animal).setOnAction(e -> {
					final JSONValue parent = item.getParent();
					parent.remove(item);
					parent.notifyListeners(null);
					final JSONArray equipment = animal.getArr("Ausrüstung");
					equipment.add(item.clone(equipment));
					equipment.notifyListeners(null);
					location.getParentPopup().hide();
				});
			}
		} else {
			heroLocationItem.setOnAction(e -> {
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
				final JSONArray equipment = hero.getObj("Besitz").getArr("Ausrüstung");
				equipment.add(item.clone(equipment));
				equipment.notifyListeners(null);
				location.getParentPopup().hide();
			});
			for (final JSONObject animal : names.keySet()) {
				if (possessor != null && animal.equals(possessor)) {
					names.get(animal).setSelected(true);
				} else {
					names.get(animal).setOnAction(e -> {
						final JSONValue parent = item.getParent();
						parent.remove(item);
						parent.notifyListeners(null);
						final JSONArray equipment = animal.getArr("Ausrüstung");
						equipment.add(item.clone(equipment));
						equipment.notifyListeners(null);
						location.getParentPopup().hide();
					});
				}
			}
		}
	}

}
