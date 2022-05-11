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

import dsa41basis.fight.Armor;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.Settings;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class ArmorList {

	@FXML
	private TitledPane pane;
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

	public ArmorList(final JSONObject hero, final String armorSet) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ArmorList.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		pane.setText(armorSet != null ? armorSet : "Rüstung");

		final String armorSetting = Settings.getSettingStringOrDefault("Zonenrüstung", "Kampf", "Rüstungsart");

		final JSONObject fight = hero.getObj("Kampf");

		final boolean isDefault = armorSet == null && fight.getStringOrDefault("Rüstung", null) == null
				|| armorSet != null && armorSet.equals(fight.getStringOrDefault("Rüstung", null));

		if (isDefault) {
			pane.getStyleClass().add("boldTitledPane");
		}

		armorTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

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

		GUIUtil.autosizeTable(armorTable);
		if ("Gesamtrüstung".equals(armorSetting)) {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "totalrs", "totalbe");
		} else {
			GUIUtil.cellValueFactories(armorTable, "name", "head", "breast", "back", "belly", "larm", "rarm", "lleg", "rleg", "zoners", "zonebe");
		}

		armorTable.setRowFactory(table -> {
			final TableRow<Armor> row = new TableRow<>() {
				@Override
				public void updateItem(final Armor weapon, final boolean empty) {
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

			final ContextMenu contextMenu = new ContextMenu();

			final JSONObject armorSets = fight.getObjOrDefault("Rüstungskombinationen", null);
			if (armorSets != null && armorSets.size() > (armorSet == null ? 0 : 1)) {
				final Menu setsItem = new Menu("Hinzufügen zu");
				for (final String set : armorSets.keySet()) {
					if (!set.equals(armorSet)) {
						final MenuItem addItem = new MenuItem(set);
						addItem.setOnAction(event -> {
							final JSONObject item = row.getItem().getItem();
							final JSONArray sets = item.getArr("Rüstungskombinationen");
							sets.add(set);
							item.notifyListeners(null);
						});
						setsItem.getItems().add(addItem);
					}
				}
				contextMenu.getItems().add(setsItem);
			}

			if (armorSet != null) {
				final MenuItem removeItem = new MenuItem("Aus Rüstungskombination entfernen");
				removeItem.setOnAction(event -> {
					final JSONObject item = row.getItem().getItem();
					final JSONArray sets = item.getArrOrDefault("Rüstungskombinationen", null);
					sets.remove(armorSet);
					item.notifyListeners(null);
				});
				contextMenu.getItems().add(removeItem);
			}

			if (!contextMenu.getItems().isEmpty()) {
				row.setContextMenu(contextMenu);
			}

			return row;
		});

		if (armorSet == null) {
			if (!isDefault) {
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem defaultItem = new MenuItem("Zum Standard machen");
				defaultItem.setOnAction(event -> {
					fight.removeKey("Rüstung");
					fight.notifyListeners(null);
				});
				contextMenu.getItems().add(defaultItem);
				pane.setContextMenu(contextMenu);
			}
		} else {
			final ContextMenu contextMenu = new ContextMenu();

			final JSONObject armorSets = fight.getObj("Rüstungskombinationen");

			pane.setOnMouseClicked(e -> {
				if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
					new ArmorSetDialog(pane.getScene().getWindow(), hero, armorSets, armorSets.getObj(armorSet));
				}
			});

			if (!isDefault) {
				final MenuItem defaultItem = new MenuItem("Zum Standard machen");
				defaultItem.setOnAction(event -> {
					fight.put("Rüstung", armorSet);
					fight.notifyListeners(null);
				});
				contextMenu.getItems().add(defaultItem);
			}

			final MenuItem editItem = new MenuItem("Bearbeiten");
			editItem.setOnAction(event -> new ArmorSetDialog(pane.getScene().getWindow(), hero, armorSets, armorSets.getObj(armorSet)));
			contextMenu.getItems().add(editItem);

			final int index = Util.getIndex(armorSets, armorSet);
			if (index > 0) {
				final MenuItem upItem = new MenuItem("Nach oben");
				upItem.setOnAction(event -> {
					final JSONObject current = armorSets.getObj(armorSet);
					armorSets.removeKey(armorSet);
					Util.insertAt(armorSets, armorSet, current, index - 1);
					armorSets.notifyListeners(null);
				});
				contextMenu.getItems().add(upItem);
			}
			if (index < armorSets.size() - 1) {
				final MenuItem downItem = new MenuItem("Nach unten");
				downItem.setOnAction(event -> {
					final JSONObject current = armorSets.getObj(armorSet);
					armorSets.removeKey(armorSet);
					Util.insertAt(armorSets, armorSet, current, index + 1);
					armorSets.notifyListeners(null);
				});
				contextMenu.getItems().add(downItem);
			}

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				armorSets.removeKey(armorSet);
				HeroUtil.foreachInventoryItem(hero, item -> item.containsKey("Kategorien") && item.getArr("Kategorien").contains("Rüstung"),
						(item, extraInventory) -> item.getArrOrDefault("Rüstungskombinationen", new JSONArray(null)).remove(armorSet));
				if (armorSet.equals(fight.getStringOrDefault("Rüstung", null))) {
					fight.removeKey("Rüstung");
					fight.notifyListeners(null);
				}
			});
			contextMenu.getItems().add(deleteItem);

			pane.setContextMenu(contextMenu);
		}

		HeroUtil.foreachInventoryItem(hero, item -> item.containsKey("Kategorien") && item.getArr("Kategorien").contains("Rüstung"), (item, extraInventory) -> {
			final JSONArray sets = item.getArrOrDefault("Rüstungskombinationen", null);
			if (armorSet == null && (sets == null || sets.size() == 0) || sets != null && sets.contains(armorSet)) {
				if (item.containsKey("Rüstung")) {
					armorTable.getItems().add(new Armor(item.getObj("Rüstung"), item));
				} else {
					armorTable.getItems().add(new Armor(item, item));
				}
			}
		});
	}

	public ObservableList<Armor> getArmor() {
		return armorTable.getItems();
	}

	public Control getControl() {
		return pane;
	}

}
