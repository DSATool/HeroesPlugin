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
package heroes.animals;

import java.util.Optional;

import dsa41basis.fight.AttackTable;
import dsa41basis.hero.Attribute;
import dsa41basis.ui.hero.BasicValuesController;
import dsa41basis.ui.hero.BasicValuesController.CharacterType;
import dsa41basis.util.DSAUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import heroes.inventory.EquipmentList;
import heroes.inventory.InventoryDialog;
import heroes.ui.HeroTabController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class AnimalController {

	public class AnimalAttribute extends Attribute {
		private final IntegerProperty bought;

		public AnimalAttribute(final String name, final JSONObject actual) {
			super(name, actual);

			bought = switch (name) {
				case "Lebensenergie", "Astralenergie" -> new SimpleIntegerProperty(actual.getIntOrDefault("Kauf", 0));
				default -> new SimpleIntegerProperty(Integer.MIN_VALUE);
			};
		}

		public final IntegerProperty boughtProperty() {
			return bought;
		}

		public final int getBought() {
			return bought.get();
		}

		public final void setBought(final int bought) {
			if (bought == 0) {
				actual.removeKey("Kauf");
			} else {
				actual.put("Kauf", bought);
			}
			actual.notifyListeners(null);
			this.bought.set(bought);
		}
	}

	public static class ProConSkill {
		private final StringProperty name = new SimpleStringProperty();
		private final StringProperty description = new SimpleStringProperty();
		private final IntegerProperty value = new SimpleIntegerProperty();

		private final JSONObject proConSkill;
		private final JSONObject actual;

		private ProConSkill(final String name, final JSONObject proConSkill, final JSONObject actual) {
			this.proConSkill = proConSkill;
			this.actual = actual;

			this.name.set(name);
			final String desc = proConSkill.containsKey("Auswahl") ? "Auswahl" : "Freitext";
			description.set(actual.getStringOrDefault(desc, proConSkill.getStringOrDefault(desc, "")));
			value.set(actual.getIntOrDefault("Wert",
					proConSkill.containsKey("Abgestuft") || proConSkill.containsKey("Schlechte Eigenschaft") ? 0 : Integer.MIN_VALUE));
		}

		public StringProperty descriptionProperty() {
			return description;
		}

		public String getDescription() {
			return description.get();
		}

		public String getName() {
			return name.get();
		}

		public int getValue() {
			return value.get();
		}

		public ReadOnlyStringProperty nameProperty() {
			return name;
		}

		public void setDescription(final String description) {
			if ("".equals(description)) return;
			actual.put(proConSkill.containsKey("Auswahl") ? "Auswahl" : "Freitext", description);
			this.description.set(description);
			actual.notifyListeners(null);
		}

		public void setValue(final int value) {
			actual.put("Wert", value);
			this.value.set(value);
			actual.notifyListeners(null);
		}

		public IntegerProperty valueProperty() {
			return value;
		}
	}

	@FXML
	private TextField name;
	@FXML
	private TextField race;
	@FXML
	private TextField training;
	@FXML
	private TextField color;
	@FXML
	private ComboBox<String> gender;
	@FXML
	private ReactiveSpinner<Integer> size;
	@FXML
	private ReactiveSpinner<Integer> weight;
	@FXML
	private HBox attributesBox;
	@FXML
	private TableColumn<AnimalAttribute, Integer> attributesModifierColumn;
	@FXML
	private TableView<AnimalAttribute> attributesTable;
	@FXML
	private TableColumn<AnimalAttribute, Integer> attributesValueColumn;
	@FXML
	private Label modLabel;
	@FXML
	private TableColumn<AnimalAttribute, Integer> statsModifierColumn;
	@FXML
	private TableColumn<AnimalAttribute, Integer> statsBoughtColumn;
	@FXML
	private TableView<AnimalAttribute> statsTable;
	@FXML
	private TableColumn<AnimalAttribute, Integer> statsValueColumn;
	@FXML
	private Node skillsBox;
	@FXML
	private TableColumn<ProConSkill, String> proConDescColumn;
	@FXML
	private TableColumn<ProConSkill, String> proConNameColumn;
	@FXML
	protected TableView<ProConSkill> proConsTable;
	@FXML
	protected TableColumn<ProConSkill, Integer> proConValueColumn;
	@FXML
	private ComboBox<String> proConsList;
	@FXML
	private Button proConsAddButton;
	@FXML
	private Node ritualsBox;
	@FXML
	private TableView<String> ritualsTable;
	@FXML
	private TableColumn<String, String> ritualNameColumn;
	@FXML
	private ComboBox<String> ritualsList;
	@FXML
	private Button ritualsAddButton;
	@FXML
	private TableColumn<ProConSkill, String> skillDescColumn;
	@FXML
	private TableColumn<ProConSkill, String> skillNameColumn;
	@FXML
	protected TableView<ProConSkill> skillsTable;
	@FXML
	private ComboBox<String> skillsList;
	@FXML
	private Button skillsAddButton;
	@FXML
	private TitledPane pane;
	@FXML
	private VBox stack;
	@FXML
	private VBox statsAndAttacksBox;

	private final JSONObject actualAnimal;
	private final CharacterType type;
	private final AttackTable attacksTable;
	private final EquipmentList equipment;

	private final JSONListener animalListener;
	private final JSONListener inventoriesListener = o -> updateInventories();

	public AnimalController(final JSONObject animal, final CharacterType type) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Animal.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		pane.setUserData(animal);

		if (type != CharacterType.MAGIC_ANIMAL) {
			ritualsBox.setManaged(false);
			ritualsBox.setVisible(false);

			if (type == CharacterType.HORSE) {
				statsAndAttacksBox.getChildren().remove(statsTable);
			}

		}
		pane.textProperty().bindBidirectional(name.textProperty());

		final JSONArray animals = (JSONArray) animal.getParent();

		final ContextMenu contextMenu = new ContextMenu();

		final int index = animals.indexOf(animal);
		if (index > 0) {
			final MenuItem upItem = new MenuItem("Nach oben");
			upItem.setOnAction(event -> {
				animals.remove(animal);
				animals.add(index - 1, animal);
				animals.notifyListeners(null);
			});
			contextMenu.getItems().add(upItem);
		}
		if (index < animals.size() - 1) {
			final MenuItem downItem = new MenuItem("Nach unten");
			downItem.setOnAction(event -> {
				animals.remove(animal);
				animals.add(index + 1, animal);
				animals.notifyListeners(null);
			});
			contextMenu.getItems().add(downItem);
		}

		final MenuItem deleteItem = new MenuItem("Löschen");
		deleteItem.setOnAction(e -> {
			final Alert deleteConfirmation = new Alert(AlertType.CONFIRMATION);
			deleteConfirmation.setTitle("Tier löschen?");
			deleteConfirmation.setHeaderText("Tier " + animal.getObj("Biografie").getString("Name") + " löschen?");
			deleteConfirmation.setContentText("Das Tier kann danach nicht wiederhergestellt werden!");
			deleteConfirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			final Optional<ButtonType> result = deleteConfirmation.showAndWait();
			if (result.isPresent() && result.get().equals(ButtonType.YES)) {
				animals.remove(animal);
				animals.notifyListeners(null);
			}
		});
		contextMenu.getItems().add(deleteItem);

		attributesTable.setContextMenu(new ContextMenu());
		statsTable.setContextMenu(new ContextMenu());

		pane.setContextMenu(contextMenu);

		this.type = type;
		actualAnimal = animal;

		final BasicValuesController basicValues = new BasicValuesController(HeroTabController.isEditable.not(), type, type == CharacterType.HORSE);
		basicValues.setCharacter(animal);
		stack.getChildren().add(1, basicValues.getControl());

		initBiography();
		initAttributes();
		initStats();
		initProConSkills();

		equipment = new EquipmentList(false);
		equipment.setHero((JSONObject) animals.getParent(), actualAnimal, "Ausrüstung", null, actualAnimal.getArr("Ausrüstung"));
		stack.getChildren().add(stack.getChildren().size() - 1, equipment.getControl());

		updateInventories();

		animalListener = o -> {
			updateProCons();
			updateSkills();
			equipment.updateEquipment();
			if (type == CharacterType.MAGIC_ANIMAL) {
				updateRituals();
			}
		};

		actualAnimal.addListener(animalListener);
		actualAnimal.getArr("Inventare").addListener(inventoriesListener);

		attacksTable = new AttackTable(HeroTabController.isEditable, attributesBox.widthProperty().subtract(2).divide(1.667),
				type == CharacterType.MAGIC_ANIMAL);
		attacksTable.setCharacter(animal);
		statsAndAttacksBox.getChildren().add(attacksTable.getControl());

		name.editableProperty().bind(HeroTabController.isEditable);
		race.editableProperty().bind(HeroTabController.isEditable);
		training.editableProperty().bind(HeroTabController.isEditable);
		color.editableProperty().bind(HeroTabController.isEditable);
		gender.disableProperty().bind(HeroTabController.isEditable.not());
		size.disableProperty().bind(HeroTabController.isEditable.not());
		weight.disableProperty().bind(HeroTabController.isEditable.not());
		attributesTable.editableProperty().bind(HeroTabController.isEditable);
		statsTable.editableProperty().bind(HeroTabController.isEditable);
		proConsTable.editableProperty().bind(HeroTabController.isEditable);
		proConsList.disableProperty().bind(HeroTabController.isEditable.not());
		proConsAddButton.disableProperty().bind(HeroTabController.isEditable.not().or(Bindings.isEmpty(proConsList.getItems())));
		ritualsList.disableProperty().bind(HeroTabController.isEditable.not());
		ritualsAddButton.disableProperty().bind(HeroTabController.isEditable.not().or(Bindings.isEmpty(ritualsList.getItems())));
		skillsList.disableProperty().bind(HeroTabController.isEditable.not());
		skillsAddButton.disableProperty().bind(HeroTabController.isEditable.not().or(Bindings.isEmpty(skillsList.getItems())));
	}

	@FXML
	private void addInventory() {
		new InventoryDialog(pane.getScene().getWindow(), actualAnimal.getArr("Inventare"), null);
	}

	@FXML
	public void addProCon() {
		final String proOrConName = proConsList.getSelectionModel().getSelectedItem();
		final JSONObject proOrCon = ResourceManager.getResource("data/Tiereigenarten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein")
				.getObj(proOrConName);
		final JSONObject actualProsOrCons = actualAnimal.getObj("Eigenarten");
		if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
			final JSONArray actual = actualProsOrCons.getArr(proOrConName);
			actual.add(new JSONObject(actual));
		} else {
			actualProsOrCons.put(proOrConName, new JSONObject(actualProsOrCons));
		}
		actualProsOrCons.notifyListeners(null);
	}

	@FXML
	public void addRitual() {
		final String ritualName = ritualsList.getSelectionModel().getSelectedItem();
		final JSONObject actualSkills = actualAnimal.getObj("Fertigkeiten");
		actualSkills.put(ritualName, new JSONObject(actualSkills));
		actualSkills.notifyListeners(null);
	}

	@FXML
	public void addSkill() {
		final String skillName = skillsList.getSelectionModel().getSelectedItem();
		final JSONObject skill = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein")
				.getObj(skillName);
		final JSONObject actualSkills = actualAnimal.getObj("Fertigkeiten");
		if (skill.containsKey("Auswahl") || skill.containsKey("Freitext")) {
			final JSONArray actual = actualSkills.getArr(skillName);
			actual.add(new JSONObject(actual));
		} else {
			actualSkills.put(skillName, new JSONObject(actualSkills));
		}
		actualSkills.notifyListeners(null);
	}

	public Node getControl() {
		return pane;
	}

	private void initAttributes() {
		attributesTable.prefWidthProperty().bind(attributesBox.widthProperty().subtract(2).divide(2.5));
		GUIUtil.autosizeTable(attributesTable);
		GUIUtil.cellValueFactories(attributesTable, "name", "value", "manualModifier", "current");

		attributesValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 999));
		attributesValueColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setValue(t.getNewValue());
			}
		});
		attributesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		attributesModifierColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setManualModifier(t.getNewValue());
			}
		});

		if (type == CharacterType.HORSE) {
			final JSONObject attributes = actualAnimal.getObj("Eigenschaften");
			for (final String attribute : new String[] { "KO", "KK" }) {
				attributesTable.getItems().add(new AnimalAttribute(attribute, attributes.getObj(attribute)));
			}
			final JSONObject stats = actualAnimal.getObj("Basiswerte");
			for (final String stat : new String[] { "Loyalität", "Lebensenergie" }) {
				attributesTable.getItems().add(new AnimalAttribute(stat, stats.getObj(stat)));
			}
		} else {
			final JSONObject attributes = actualAnimal.getObj("Eigenschaften");
			for (final String attribute : ResourceManager.getResource("data/Eigenschaften").keySet()) {
				attributesTable.getItems().add(new AnimalAttribute(attribute, attributes.getObj(attribute)));
			}
		}

		attributesTable.setRowFactory(t -> {
			final TableRow<AnimalAttribute> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem attributesContextMenuItem = new MenuItem("Bearbeiten");
			contextMenu.getItems().add(attributesContextMenuItem);
			attributesContextMenuItem.setOnAction(o -> {
				final Attribute attribute = row.getItem();
				new AnimalAttributeEditor(pane.getScene().getWindow(), attribute, type == CharacterType.MAGIC_ANIMAL);
			});
			row.setContextMenu(contextMenu);

			return row;
		});
	}

	private void initBiography() {
		final JSONObject biography = actualAnimal.getObj("Biografie");

		final ChangeListener<Object> biographyListener = (o, oldV, newV) -> {
			if (oldV == newV || newV == null || oldV == null) return;
			biography.put("Name", name.getText());
			biography.put("Rasse", race.getText());
			final JSONArray train = biography.getArr("Ausbildung");
			train.clear();
			final String[] trainings = training.getText().trim().split("\\s*,\\s*");
			for (final String singleTraining : trainings) {
				train.add(singleTraining);
			}
			biography.put("Farbe", color.getText());
			biography.put("Geschlecht", gender.getValue());
			biography.put("Größe", size.getValue());
			biography.put("Gewicht", weight.getValue());

			biography.notifyListeners(null);
		};

		name.setText(biography.getStringOrDefault("Name", "Pferd"));
		name.textProperty().addListener(biographyListener);
		race.setText(biography.getStringOrDefault("Rasse", ""));
		race.textProperty().addListener(biographyListener);
		training.setText(String.join(", ", biography.getArr("Ausbildung").getStrings()));
		training.textProperty().addListener(biographyListener);

		gender.setItems(FXCollections.observableArrayList("männlich", "weiblich"));

		color.setText(biography.getStringOrDefault("Farbe", ""));
		color.textProperty().addListener(biographyListener);
		gender.setValue(biography.getStringOrDefault("Geschlecht", "weiblich"));
		gender.valueProperty().addListener(biographyListener);
		size.getValueFactory().setValue(biography.getIntOrDefault("Größe", 0));
		size.valueProperty().addListener(biographyListener);
		weight.getValueFactory().setValue(biography.getIntOrDefault("Gewicht", 0));
		weight.valueProperty().addListener(biographyListener);
	}

	private void initProConSkills() {
		GUIUtil.autosizeTable(proConsTable);
		GUIUtil.cellValueFactories(proConsTable, "name", "description", "value");

		proConNameColumn.setCellFactory(c -> new TextFieldTableCell<>() {
			@Override
			public void updateItem(final String item, final boolean empty) {
				super.updateItem(item, empty);
				final ProConSkill proOrCon = getTableRow().getItem();
				if (proOrCon != null) {
					Util.addReference(this, proOrCon.proConSkill, 15, proConNameColumn.widthProperty());
				}
			}
		});

		proConDescColumn.setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final TextField t = new TextField();
				createGraphic(t, t::getText, t::setText);
			}

			@Override
			public void startEdit() {
				if (getItem() == null || "".equals(getItem())) return;
				super.startEdit();
			}
		});
		proConDescColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setDescription(t.getNewValue());
			}
		});
		proConValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 9999));
		proConValueColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setValue(t.getNewValue());
			}
		});

		proConsTable.setRowFactory(t -> {
			final TableRow<ProConSkill> row = new TableRow<>();

			final ContextMenu proConContextMenu = new ContextMenu();
			final MenuItem proConDeleteItem = new MenuItem("Löschen");
			proConContextMenu.getItems().add(proConDeleteItem);
			proConDeleteItem.setOnAction(o -> {
				final JSONObject actual = actualAnimal.getObj("Eigenarten");
				final ProConSkill item = row.getItem();
				final String proOrConName = item.getName();
				final JSONObject proOrCon = ResourceManager.getResource("data/Tiereigenarten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein")
						.getObj(proOrConName);
				if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
					actual.getArr(proOrConName).remove(item.actual);
				} else {
					actual.removeKey(proOrConName);
				}
				actual.notifyListeners(null);
			});
			row.contextMenuProperty().bind(
					Bindings.when(HeroTabController.isEditable.and(row.itemProperty().isNotNull())).then(proConContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		if (type == CharacterType.MAGIC_ANIMAL) {
			GUIUtil.autosizeTable(ritualsTable);
			ritualNameColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));

			ritualsTable.setRowFactory(t -> {
				final TableRow<String> row = new TableRow<>();

				final ContextMenu ritualContextMenu = new ContextMenu();
				final MenuItem ritualDeleteItem = new MenuItem("Löschen");
				ritualContextMenu.getItems().add(ritualDeleteItem);
				ritualDeleteItem.setOnAction(o -> {
					final JSONObject actual = actualAnimal.getObj("Fertigkeiten");
					final String item = row.getItem();
					actual.removeKey(item);
					actual.notifyListeners(null);
				});
				row.contextMenuProperty().bind(
						Bindings.when(HeroTabController.isEditable.and(row.itemProperty().isNotNull())).then(ritualContextMenu).otherwise((ContextMenu) null));

				return row;
			});

			updateRituals();
		}

		GUIUtil.autosizeTable(skillsTable);
		GUIUtil.cellValueFactories(skillsTable, "name", "description");

		skillNameColumn.setCellFactory(c -> new TextFieldTableCell<>() {
			@Override
			public void updateItem(final String item, final boolean empty) {
				super.updateItem(item, empty);
				final ProConSkill proOrCon = getTableRow().getItem();
				if (proOrCon != null) {
					Util.addReference(this, proOrCon.proConSkill, 15, skillNameColumn.widthProperty());
				}
			}
		});

		skillDescColumn.setCellFactory(c -> new GraphicTableCell<>(false) {
			@Override
			protected void createGraphic() {
				final TextField t = new TextField();
				createGraphic(t, t::getText, t::setText);
			}

			@Override
			public void startEdit() {
				if (getItem() == null || "".equals(getItem())) return;
				super.startEdit();
			}
		});
		skillDescColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setDescription(t.getNewValue());
			}
		});

		skillsTable.setRowFactory(t -> {
			final TableRow<ProConSkill> row = new TableRow<>();

			final ContextMenu skillContextMenu = new ContextMenu();
			final MenuItem skillDeleteItem = new MenuItem("Löschen");
			skillContextMenu.getItems().add(skillDeleteItem);
			skillDeleteItem.setOnAction(o -> {
				final JSONObject actual = actualAnimal.getObj("Fertigkeiten");
				final ProConSkill item = row.getItem();
				final String skillName = item.getName();
				final JSONObject skill = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein")
						.getObj(skillName);
				if (skill.containsKey("Auswahl") || skill.containsKey("Freitext")) {
					actual.getArr(skillName).remove(item.actual);
				} else {
					actual.removeKey(skillName);
				}
				actual.notifyListeners(null);
			});

			row.contextMenuProperty().bind(
					Bindings.when(HeroTabController.isEditable.and(row.itemProperty().isNotNull())).then(skillContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		updateProCons();
		updateSkills();
	}

	private void initStats() {
		statsTable.prefWidthProperty().bind(attributesBox.widthProperty().subtract(2).divide(1.667));
		final JSONObject baseValues = actualAnimal.getObj("Basiswerte");

		GUIUtil.autosizeTable(statsTable);
		GUIUtil.cellValueFactories(statsTable, "name", "value", "bought", "manualModifier", "current");

		statsValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 999));
		statsValueColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setValue(t.getNewValue());
			}
		});
		statsModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		statsModifierColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setManualModifier(t.getNewValue());
			}
		});

		for (final String attribute : new String[] { "Loyalität", "Lebensenergie", "Ausdauer" }) {
			statsTable.getItems().add(new AnimalAttribute(attribute, baseValues.getObj(attribute)));
		}
		if (type == CharacterType.MAGIC_ANIMAL) {
			statsBoughtColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99));
			statsBoughtColumn.setOnEditCommit(t -> {
				if (t.getRowValue() != null) {
					t.getRowValue().setBought(t.getNewValue());
				}
			});
			statsTable.getItems().add(new AnimalAttribute("Astralenergie", baseValues.getObj("Astralenergie")));
		} else {
			statsBoughtColumn.setMinWidth(0.0);
			statsBoughtColumn.setMaxWidth(0.0);
			statsBoughtColumn.setVisible(false);
			statsTable.getItems().add(new AnimalAttribute("Fährtensuchen", baseValues.getObjOrDefault("Fährtensuchen", new JSONObject(baseValues))));
		}

		statsTable.setRowFactory(t -> {
			final TableRow<AnimalAttribute> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem attributesContextMenuItem = new MenuItem("Bearbeiten");
			contextMenu.getItems().add(attributesContextMenuItem);
			attributesContextMenuItem.setOnAction(o -> {
				final Attribute attribute = row.getItem();
				new AnimalAttributeEditor(pane.getScene().getWindow(), attribute, false);
			});

			row.contextMenuProperty().bind(Bindings.when(row.itemProperty().isNotNull()).then(contextMenu).otherwise((ContextMenu) null));

			return row;
		});
	}

	public void unregisterListeners() {
		actualAnimal.removeListener(animalListener);
		actualAnimal.getArr("Inventare").removeListener(inventoriesListener);
	}

	private void updateInventories() {
		stack.getChildren().remove(5, stack.getChildren().size() - 1);

		final JSONArray inventories = actualAnimal.getArrOrDefault("Inventare", null);

		DSAUtil.foreach(inventory -> true, inventory -> {
			final EquipmentList list = new EquipmentList(false);

			final String name = inventory.getStringOrDefault("Name", "Unbenanntes Inventar");
			list.setHero((JSONObject) actualAnimal.getParent().getParent(), inventory, name, inventories, actualAnimal.getArr("Ausrüstung"));

			stack.getChildren().add(stack.getChildren().size() - 1, list.getControl());

			GUIUtil.dragDropReorder(list.getControl(), moved -> {
				final int index = stack.getChildren().indexOf(moved) - 5;
				final JSONObject current = (JSONObject) ((Control) moved).getUserData();
				inventories.remove(current);
				inventories.add(index, current);
				inventories.notifyListeners(null);
			}, stack);
		}, inventories);
	}

	private void updateProCons() {
		proConsTable.getItems().clear();

		final JSONObject prosOrCons = ResourceManager.getResource("data/Tiereigenarten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein");
		final JSONObject actualProsOrCons = actualAnimal.getObj("Eigenarten");

		for (final String proOrConName : actualProsOrCons.keySet()) {
			if (prosOrCons.containsKey(proOrConName)) {
				final JSONObject proOrCon = prosOrCons.getObj(proOrConName);
				if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
					final JSONArray list = actualProsOrCons.getArr(proOrConName);
					for (int i = 0; i < list.size(); ++i) {
						proConsTable.getItems().add(new ProConSkill(proOrConName, proOrCon, list.getObj(i)));
					}
				} else {
					proConsTable.getItems().add(new ProConSkill(proOrConName, proOrCon, actualProsOrCons.getObj(proOrConName)));
				}
			}
		}

		proConsList.getItems().clear();
		for (final String proOrConName : prosOrCons.keySet()) {
			final JSONObject proOrCon = prosOrCons.getObj(proOrConName);
			if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext") || !actualProsOrCons.containsKey(proOrConName)) {
				proConsList.getItems().add(proOrConName);
			}
		}

		proConsList.getSelectionModel().select(0);
	}

	private void updateRituals() {
		ritualsTable.getItems().clear();

		final JSONObject rituals = ResourceManager.getResource("data/Tierfertigkeiten").getObj("Vertrautenmagie");
		final JSONObject actualSkills = actualAnimal.getObj("Fertigkeiten");

		for (final String skillName : actualSkills.keySet()) {
			if (rituals.containsKey(skillName)) {
				ritualsTable.getItems().add(skillName);
			}
		}

		ritualsList.getItems().clear();
		for (final String ritualName : rituals.keySet()) {
			if (!actualSkills.containsKey(ritualName)) {
				ritualsList.getItems().add(ritualName);
			}
		}

		ritualsList.getSelectionModel().select(0);
	}

	private void updateSkills() {
		skillsTable.getItems().clear();

		final JSONObject skills = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == CharacterType.HORSE ? "Reittiere" : "Allgemein");
		final JSONObject actualSkills = actualAnimal.getObj("Fertigkeiten");

		for (final String skillName : actualSkills.keySet()) {
			if (skills.containsKey(skillName)) {
				final JSONObject skill = skills.getObj(skillName);
				if (skill.containsKey("Auswahl") || skill.containsKey("Freitext")) {
					final JSONArray list = actualSkills.getArr(skillName);
					for (int i = 0; i < list.size(); ++i) {
						skillsTable.getItems().add(new ProConSkill(skillName, skill, list.getObj(i)));
					}
				} else {
					skillsTable.getItems().add(new ProConSkill(skillName, skill, actualSkills.getObj(skillName)));
				}
			}
		}

		skillsList.getItems().clear();
		for (final String skillName : skills.keySet()) {
			final JSONObject skill = skills.getObj(skillName);
			if (skill.containsKey("Auswahl") || skill.containsKey("Freitext") || !actualSkills.containsKey(skillName)) {
				skillsList.getItems().add(skillName);
			}
		}

		skillsList.getSelectionModel().select(0);
	}
}
