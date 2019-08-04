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

import dsa41basis.inventory.Artifact;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
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
import jsonant.value.JSONValue;

public class ArtifactEditor {
	public class Spell {
		private final StringProperty name = new SimpleStringProperty();
		private final StringProperty variant = new SimpleStringProperty();
		private final JSONObject actual;

		private Spell(final JSONObject spell) {
			actual = spell;
			name.set(actual.getStringOrDefault("Spruch", ""));
			variant.set(actual.getStringOrDefault("Variante", ""));
		}

		public String getName() {
			return name.get();
		}

		public String getVariant() {
			return variant.get();
		}

		public StringProperty nameProperty() {
			return name;
		}

		public void setName(final String name) {
			actual.put("Spruch", name);
			this.name.set(name);
			actual.notifyListeners(null);
		}

		public void setVariant(final String variant) {
			if (variant.length() > 0) {
				actual.put("Variante", variant);
			} else {
				actual.removeKey("Variante");
			}
			this.variant.set(variant);
			actual.notifyListeners(null);
		}

		public StringProperty variantProperty() {
			return variant;
		}
	}

	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private TextField notes;
	@FXML
	private Button okButton;
	@FXML
	private ComboBox<String> type;
	@FXML
	private Node loadBox;
	@FXML
	private ReactiveSpinner<Integer> loadNum;
	@FXML
	private ComboBox<String> loadFreq;
	@FXML
	private Node stabilityBox;
	@FXML
	private ComboBox<String> stability;
	@FXML
	private ReactiveSpinner<Integer> asp;
	@FXML
	private ReactiveSpinner<Integer> pasp;
	@FXML
	private ComboBox<String> triggerType;
	@FXML
	private ReactiveSpinner<Integer> triggerActions;
	@FXML
	private TextField triggerDesc;
	@FXML
	private TableView<Spell> spellTable;
	@FXML
	private TableColumn<Spell, String> spellNameColumn;
	@FXML
	private TableColumn<Spell, String> spellVariantColumn;
	@FXML
	private ReactiveSpinner<Double> weight;
	@FXML
	private Button cancelButton;

	private Artifact artifact;

	public ArtifactEditor(final Window window, final Artifact artifact) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ArtifactEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 330, 400));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		final JSONObject triggers = ResourceManager.getResource("data/Artefakt_Ausloeser");

		type.setItems(FXCollections.observableArrayList(Artifact.types));
		loadFreq.setItems(FXCollections.observableArrayList("Tag", "Woche", "Monat", "Jahr"));
		stability.setItems(FXCollections.observableArrayList("labil", "stabil", "sehr stabil", "unempfindlich"));
		triggerType.setItems(FXCollections.observableArrayList(triggers.keySet()));
		triggerType.getItems().add("anderer");

		this.artifact = artifact;

		final JSONArray spells = artifact.getSpells();

		GUIUtil.cellValueFactories(spellTable, "name", "variant");

		spellTable.setRowFactory(table -> {
			final TableRow<Spell> row = new TableRow<>();
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final JSONObject item = row.getItem().actual;
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
				updateSpellTable();
			});
			contextMenu.getItems().add(deleteItem);
			row.setContextMenu(contextMenu);
			return row;
		});

		spellNameColumn.setCellFactory(o -> {
			final TableCell<Spell, String> cell = new GraphicTableCell<Spell, String>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
				}
			};
			cell.itemProperty().addListener((no, ov, nv) -> {
				if (cell.getTableRow().getIndex() < spellTable.getItems().size() - 1) {
					cell.setContextMenu(cell.getTableRow().getContextMenu());
				} else {
					cell.setContextMenu(null);
				}
			});
			return cell;
		});

		spellNameColumn.setOnEditCommit(event -> {
			if (event.getTablePosition().getRow() == spellTable.getItems().size() - 1) {
				if (event.getNewValue() != null && !"".equals(event.getNewValue())) {
					final JSONObject newSpell = new JSONObject(spells);
					newSpell.put("Spruch", event.getNewValue());
					spells.add(newSpell);
					spells.notifyListeners(null);
					updateSpellTable();
				}
			} else {
				spellTable.getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());
			}
		});

		spellVariantColumn.setCellFactory(o -> {
			final TableCell<Spell, String> cell = new GraphicTableCell<Spell, String>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
				}
			};
			cell.itemProperty().addListener((no, ov, nv) -> {
				if (cell.getTableRow().getIndex() < spellTable.getItems().size() - 1) {
					cell.setContextMenu(cell.getTableRow().getContextMenu());
				} else {
					cell.setContextMenu(null);
				}
			});
			return cell;
		});
		spellVariantColumn.setOnEditCommit(event -> {
			if (event.getTablePosition().getRow() < spellTable.getItems().size() - 1) {
				final String variant = event.getNewValue();
				spellTable.getItems().get(event.getTablePosition().getRow()).setVariant(variant);
			}
		});

		type.valueProperty().addListener((o, oldV, newV) -> {
			loadNum.setDisable(true);
			loadFreq.setDisable(true);
			stability.setDisable(true);
			switch (type.getValue()) {
				case "Arcanovi (semipermanent)":
					loadFreq.setDisable(false);
				case "Applicatus":
				case "Arcanovi (einmalig)":
				case "Arcanovi (aufladbar)":
					loadNum.setDisable(false);
					break;
				case "Matrixgeber":
				case "Zaubertalisman":
					stability.setDisable(false);
					break;
			}
		});

		triggerType.valueProperty().addListener((o, oldV, newV) -> {
			if (!"andere".equals(newV)) {
				final JSONObject trigger = triggers.getObj(newV);
				triggerActions.getValueFactory().setValue(trigger.getBoolOrDefault("Reaktion", false) ? 0 : trigger.getIntOrDefault("Aktionen", 1));
			}
		});

		name.setText(artifact.getName());
		type.setValue(artifact.getType());
		switch (type.getValue()) {
			case "Arcanovi (semipermanent)":
				loadFreq.setValue(artifact.getLoadFreq());
			case "Applicatus":
			case "Arcanovi (einmalig)":
			case "Arcanovi (aufladbar)":
				loadNum.getValueFactory().setValue(artifact.getLoadNum());
				break;
			default:
				artifact.setLoads(Integer.MIN_VALUE, null);
				break;
		}
		loadNum.getValueFactory().setValue(artifact.getLoadNum());
		stability.getSelectionModel().select(artifact.getStability());
		asp.getValueFactory().setValue(artifact.getAsp());
		pasp.getValueFactory().setValue(artifact.getPAsp());
		triggerType.setValue(artifact.getTriggerType());
		triggerActions.getValueFactory().setValue(artifact.getTriggerActions());
		triggerDesc.setText(artifact.getTriggerDesc());
		weight.getValueFactory().setValue(artifact.getWeight());
		notes.setText(artifact.getNotes());

		okButton.setOnAction(event -> {
			artifact.setName(name.getText());
			artifact.setType(type.getValue());
			switch (type.getValue()) {
				case "Arcanovi (semipermanent)":
					artifact.setLoads(loadNum.getValue(), loadFreq.getValue());
					break;
				case "Applicatus":
				case "Arcanovi (einmalig)":
				case "Arcanovi (aufladbar)":
					artifact.setLoads(loadNum.getValue(), null);
					break;
				default:
					artifact.setLoads(Integer.MIN_VALUE, null);
			}
			switch (type.getValue()) {
				case "Matrixgeber":
				case "Zaubertalisman":
					artifact.setStability(stability.getSelectionModel().getSelectedItem());
					break;
				default:
					artifact.setStability(null);
			}
			artifact.setAsp(asp.getValue(), pasp.getValue());
			artifact.setTrigger(triggerType.getValue(), triggerActions.getValue(), triggerDesc.getText());
			artifact.setWeight(weight.getValue());
			artifact.setNotes(notes.getText());
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		updateSpellTable();

		stage.show();
	}

	private void updateSpellTable() {
		spellTable.getItems().clear();
		final JSONArray spells = artifact.getSpells();
		for (int i = 0; i < spells.size(); ++i) {
			spellTable.getItems().add(new Spell(spells.getObj(i)));
		}
		spellTable.getItems().add(new Spell(new JSONObject(null)));
		spellTable.setPrefHeight((spellTable.getItems().size() + 1) * 25 + 1);
		spellTable.setMinHeight((spellTable.getItems().size() + 1) * 25 + 1);
	}
}
