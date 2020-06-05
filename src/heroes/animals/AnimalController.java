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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import dsa41basis.hero.Attribute;
import dsa41basis.inventory.InventoryItem;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.GraphicTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import heroes.ui.HeroTabController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;
import jsonant.value.JSONValue;

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

	public enum AnimalType {
		HORSE, MAGIC, ANIMAL
	}

	public class ProConSkill {
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
	private Node boughtLabel;
	@FXML
	private ReactiveSpinner<Integer> iniBase;
	@FXML
	private ReactiveSpinner<Integer> iniDiceNum;
	@FXML
	private ReactiveSpinner<Integer> iniDiceType;
	@FXML
	private ReactiveSpinner<Integer> iniMod;
	@FXML
	private ComboBox<String> mrChoice;
	@FXML
	private ReactiveSpinner<Integer> mr;
	@FXML
	private Node mrBox;
	@FXML
	private ReactiveSpinner<Integer> mrMind;
	@FXML
	private ReactiveSpinner<Integer> mrBody;
	@FXML
	private ReactiveSpinner<Integer> mrMod;
	@FXML
	private Node mrModBox;
	@FXML
	private ReactiveSpinner<Integer> mrMindMod;
	@FXML
	private ReactiveSpinner<Integer> mrBodyMod;
	@FXML
	private ReactiveSpinner<Integer> mrBought;
	@FXML
	private Node mrBoughtBox;
	@FXML
	private ReactiveSpinner<Integer> mrMindBought;
	@FXML
	private ReactiveSpinner<Integer> mrBodyBought;
	@FXML
	private ComboBox<String> speedChoice;
	@FXML
	private ReactiveSpinner<Double> speed;
	@FXML
	private Node speedBox;
	@FXML
	private ReactiveSpinner<Double> speedGround;
	@FXML
	private ReactiveSpinner<Double> speedAir;
	@FXML
	private ReactiveSpinner<Double> speedMod;
	@FXML
	private Node speedModBox;
	@FXML
	private ReactiveSpinner<Double> speedGroundMod;
	@FXML
	private ReactiveSpinner<Double> speedAirMod;
	@FXML
	private ReactiveSpinner<Double> speedBought;
	@FXML
	private Node speedBoughtBox;
	@FXML
	private ReactiveSpinner<Double> speedGroundBought;
	@FXML
	private ReactiveSpinner<Double> speedAirBought;
	@FXML
	private Node horseSpeedBox;
	@FXML
	private ReactiveSpinner<Integer> speedWalk;
	@FXML
	private ReactiveSpinner<Integer> speedTrot;
	@FXML
	private ReactiveSpinner<Integer> speedGallop;
	@FXML
	private Node horseSpeedModBox;
	@FXML
	private ReactiveSpinner<Integer> speedWalkMod;
	@FXML
	private ReactiveSpinner<Integer> speedTrotMod;
	@FXML
	private ReactiveSpinner<Integer> speedGallopMod;
	@FXML
	private Label staminaLabel;
	@FXML
	private Node staminaBox;
	@FXML
	private Node staminaModBox;
	@FXML
	private ReactiveSpinner<Integer> staminaTrot;
	@FXML
	private ReactiveSpinner<Integer> staminaGallop;
	@FXML
	private ReactiveSpinner<Integer> staminaTrotMod;
	@FXML
	private ReactiveSpinner<Integer> staminaGallopMod;
	@FXML
	private Label feedLabel;
	@FXML
	private Node feedBox;
	@FXML
	private ReactiveSpinner<Integer> feedBase;
	@FXML
	private ReactiveSpinner<Integer> feedLight;
	@FXML
	private ReactiveSpinner<Integer> feedMedium;
	@FXML
	private ReactiveSpinner<Integer> feedHeavy;
	@FXML
	private Node apBox;
	@FXML
	private ReactiveSpinner<Integer> ap;
	@FXML
	private ReactiveSpinner<Integer> freeAp;
	@FXML
	private ReactiveSpinner<Integer> rkw;
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
	private TableColumn<Attack, String> attackNameColumn;
	@FXML
	private TableColumn<Attack, String> attackTPColumn;
	@FXML
	private TableColumn<Attack, Integer> attackATColumn;
	@FXML
	private TableColumn<Attack, Integer> attackPAColumn;
	@FXML
	private TableColumn<Attack, String> attackDistanceColumn;
	@FXML
	private TableColumn<Attack, String> attackNotesColumn;
	@FXML
	private TableView<Attack> attacksTable;
	@FXML
	private TextField newAttackField;
	@FXML
	private Button attackAddButton;
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
	private TableColumn<InventoryItem, String> equipmentNameColumn;
	@FXML
	private TableColumn<InventoryItem, String> equipmentNotesColumn;
	@FXML
	private TableView<InventoryItem> equipmentTable;
	@FXML
	private ComboBox<String> equipmentList;
	@FXML
	private TitledPane pane;

	private final JSONObject hero;
	private final JSONObject actualAnimal;
	private final AnimalType type;

	public AnimalController(final JSONObject hero, final JSONObject animal, final AnimalType type) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Animal.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final ObservableList<Node> controls = ((GridPane) pane.getContent()).getChildren();
		if (type == AnimalType.HORSE) {
			((VBox) statsTable.getParent()).getChildren().remove(statsTable);
			controls.remove(mr);
			controls.remove(speed);
			controls.remove(speedBox);
			controls.remove(speedMod);
			controls.remove(speedModBox);
			controls.remove(speedBought);
			controls.remove(speedBoughtBox);
			mrChoice.setDisable(true);
			speedChoice.setDisable(true);
		} else {
			controls.remove(horseSpeedBox);
			controls.remove(horseSpeedModBox);
			controls.remove(staminaLabel);
			controls.remove(staminaBox);
			controls.remove(staminaModBox);
			controls.remove(feedLabel);
			controls.remove(feedBox);
			if (type == AnimalType.MAGIC) {
				GridPane.setRowIndex(attributesBox, 7);
				GridPane.setRowIndex(skillsBox, 8);
				GridPane.setRowIndex(equipmentTable, 9);
			} else {
				GridPane.setRowIndex(attributesBox, 6);
				GridPane.setRowIndex(skillsBox, 7);
				GridPane.setRowIndex(equipmentTable, 8);
				controls.remove(speedBought);
				controls.remove(speedBoughtBox);
			}
		}
		if (type != AnimalType.MAGIC) {
			controls.remove(apBox);
			ritualsBox.setManaged(false);
			ritualsBox.setVisible(false);
			controls.remove(boughtLabel);
		}

		pane.textProperty().bindBidirectional(name.textProperty());

		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem deleteItem = new MenuItem("Löschen");
		deleteItem.setOnAction(e -> {
			final Alert deleteConfirmation = new Alert(AlertType.CONFIRMATION);
			deleteConfirmation.setTitle("Tier löschen?");
			deleteConfirmation.setHeaderText("Tier " + animal.getObj("Biografie").getString("Name") + " löschen?");
			deleteConfirmation.setContentText("Das Tier kann danach nicht wiederhergestellt werden!");
			deleteConfirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			final Optional<ButtonType> result = deleteConfirmation.showAndWait();
			if (result.isPresent() && result.get().equals(ButtonType.YES)) {
				hero.getArr("Tiere").remove(animal);
				hero.getArr("Tiere").notifyListeners(null);
			}
		});
		contextMenu.getItems().add(deleteItem);

		attributesTable.setContextMenu(new ContextMenu());
		statsTable.setContextMenu(new ContextMenu());

		pane.setContextMenu(contextMenu);

		this.hero = hero;
		actualAnimal = animal;
		this.type = type;

		initBiography();
		initAttributes();
		initStats();
		initAttacks();
		initProConSkills();
		initEquipment();

		name.editableProperty().bind(HeroTabController.isEditable);
		race.editableProperty().bind(HeroTabController.isEditable);
		training.editableProperty().bind(HeroTabController.isEditable);
		color.editableProperty().bind(HeroTabController.isEditable);
		gender.disableProperty().bind(HeroTabController.isEditable.not());
		size.disableProperty().bind(HeroTabController.isEditable.not());
		weight.disableProperty().bind(HeroTabController.isEditable.not());
		iniBase.disableProperty().bind(HeroTabController.isEditable.not());
		iniDiceNum.disableProperty().bind(HeroTabController.isEditable.not());
		iniDiceType.disableProperty().bind(HeroTabController.isEditable.not());
		iniMod.disableProperty().bind(HeroTabController.isEditable.not());
		mrChoice.disableProperty().bind(HeroTabController.isEditable.not().or(new SimpleBooleanProperty(type == AnimalType.HORSE)));
		mr.disableProperty().bind(HeroTabController.isEditable.not());
		mrMind.disableProperty().bind(HeroTabController.isEditable.not());
		mrBody.disableProperty().bind(HeroTabController.isEditable.not());
		mrBought.disableProperty().bind(HeroTabController.isEditable.not());
		mrMindBought.disableProperty().bind(HeroTabController.isEditable.not());
		mrBodyBought.disableProperty().bind(HeroTabController.isEditable.not());
		mrMod.disableProperty().bind(HeroTabController.isEditable.not());
		mrMindMod.disableProperty().bind(HeroTabController.isEditable.not());
		mrBodyMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedChoice.disableProperty().bind(HeroTabController.isEditable.not().or(new SimpleBooleanProperty(type == AnimalType.HORSE)));
		speed.disableProperty().bind(HeroTabController.isEditable.not());
		speedGround.disableProperty().bind(HeroTabController.isEditable.not());
		speedAir.disableProperty().bind(HeroTabController.isEditable.not());
		speedBought.disableProperty().bind(HeroTabController.isEditable.not());
		speedGroundBought.disableProperty().bind(HeroTabController.isEditable.not());
		speedAirBought.disableProperty().bind(HeroTabController.isEditable.not());
		speedMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedGroundMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedAirMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedWalk.disableProperty().bind(HeroTabController.isEditable.not());
		speedTrot.disableProperty().bind(HeroTabController.isEditable.not());
		speedGallop.disableProperty().bind(HeroTabController.isEditable.not());
		speedWalkMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedTrotMod.disableProperty().bind(HeroTabController.isEditable.not());
		speedGallopMod.disableProperty().bind(HeroTabController.isEditable.not());
		staminaTrot.disableProperty().bind(HeroTabController.isEditable.not());
		staminaGallop.disableProperty().bind(HeroTabController.isEditable.not());
		staminaTrotMod.disableProperty().bind(HeroTabController.isEditable.not());
		staminaGallopMod.disableProperty().bind(HeroTabController.isEditable.not());
		feedBase.disableProperty().bind(HeroTabController.isEditable.not());
		feedLight.disableProperty().bind(HeroTabController.isEditable.not());
		feedMedium.disableProperty().bind(HeroTabController.isEditable.not());
		feedHeavy.disableProperty().bind(HeroTabController.isEditable.not());
		freeAp.disableProperty().bind(HeroTabController.isEditable.not());
		rkw.disableProperty().bind(HeroTabController.isEditable.not());
		attributesTable.editableProperty().bind(HeroTabController.isEditable);
		statsTable.editableProperty().bind(HeroTabController.isEditable);
		attacksTable.editableProperty().bind(HeroTabController.isEditable);
		newAttackField.editableProperty().bind(HeroTabController.isEditable);
		attackAddButton.disableProperty().bind(HeroTabController.isEditable.not());
		proConsTable.editableProperty().bind(HeroTabController.isEditable);
		proConsList.disableProperty().bind(HeroTabController.isEditable.not());
		proConsAddButton.disableProperty().bind(HeroTabController.isEditable.not());
		ritualsList.disableProperty().bind(HeroTabController.isEditable.not());
		ritualsAddButton.disableProperty().bind(HeroTabController.isEditable.not());
		skillsList.disableProperty().bind(HeroTabController.isEditable.not());
		skillsAddButton.disableProperty().bind(HeroTabController.isEditable.not());
	}

	@FXML
	private void addAttack() {
		final JSONObject attacks = actualAnimal.getObj("Angriffe");
		final JSONObject newAttack = new JSONObject(attacks);
		newAttack.put("Trefferpunkte", new JSONObject(newAttack));
		String name = newAttackField.getText();
		name = "".equals(name) ? "Attacke" : name;
		if (attacks.containsKey(name)) {
			for (int i = 2; i < 100; ++i) {
				if (!attacks.containsKey(name + i)) {
					name = name + i;
					break;
				}
			}
		}
		attacks.put(name, newAttack);
		attacks.notifyListeners(null);
	}

	@FXML
	public void addItem() {
		final String itemName = equipmentList.getSelectionModel().getSelectedItem();
		final JSONArray items = actualAnimal.getArr("Ausrüstung");
		final JSONObject item = ResourceManager.getResource("data/Ausruestung").getObj(itemName).clone(items);
		item.put("Name", itemName);
		items.add(item);
		items.notifyListeners(null);
	}

	@FXML
	public void addProCon() {
		final String proOrConName = proConsList.getSelectionModel().getSelectedItem();
		final JSONObject proOrCon = ResourceManager.getResource("data/Tiereigenarten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein")
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
		final JSONObject skill = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein")
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

	private void initAttacks() {
		attacksTable.prefWidthProperty().bind(attributesBox.widthProperty().subtract(2).divide(1.667));
		GUIUtil.autosizeTable(attacksTable, 0, 2);
		GUIUtil.cellValueFactories(attacksTable, "name", "tp", "at", "pa", "dk", "notes");

		attackATColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99, 1, false));
		attackATColumn.setOnEditCommit(t -> t.getRowValue().setAt(t.getNewValue()));
		attackPAColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99, 1, false));
		attackPAColumn.setOnEditCommit(t -> t.getRowValue().setPa(t.getNewValue()));

		final JSONObject attacks = actualAnimal.getObj("Angriffe");

		attackNameColumn.setCellFactory(o -> {
			final TableCell<Attack, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
				}
			};
			return cell;
		});
		attackNameColumn.setOnEditCommit(event -> {
			final Attack attack = event.getRowValue();
			attack.setName(event.getNewValue());
		});

		attackNotesColumn.setCellFactory(o -> {
			final TableCell<Attack, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
				}
			};
			return cell;
		});
		attackNotesColumn.setOnEditCommit(event -> {
			final String note = event.getNewValue();
			final Attack attack = event.getRowValue();
			attack.setNotes(note);
		});

		attacksTable.setRowFactory(tableView -> {
			final TableRow<Attack> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();

			final Consumer<Object> edit = obj -> {
				final Attack attack = row.getItem();
				final Window window = pane.getScene().getWindow();
				if (attack != null && !"".equals(attack.getName())) {
					new AttackEditor(window, attack, type == AnimalType.MAGIC);
				}
			};

			row.setOnMouseClicked(event -> {
				if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
					edit.accept(null);
				}
			});

			final MenuItem editItem = new MenuItem("Bearbeiten");
			contextMenu.getItems().add(editItem);
			editItem.setOnAction(event -> edit.accept(null));

			final MenuItem deleteItem = new MenuItem("Löschen");
			contextMenu.getItems().add(deleteItem);
			deleteItem.setOnAction(o -> {
				final Attack attack = row.getItem();
				if (!"".equals(attack.getName())) {
					final String name = attack.getName();
					attacks.removeKey(name);
					attacks.notifyListeners(null);
				}
			});

			row.setContextMenu(contextMenu);

			return row;
		});

		actualAnimal.getObj("Angriffe").addListener(o -> updateAttacks());

		updateAttacks();
	}

	private void initAttributes() {
		attributesTable.prefWidthProperty().bind(attributesBox.widthProperty().subtract(2).divide(2.5));
		GUIUtil.autosizeTable(attributesTable, 0, 2);
		GUIUtil.cellValueFactories(attributesTable, "name", "value", "manualModifier", "current");

		attributesValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 999, 1, false));
		attributesValueColumn.setOnEditCommit(t -> t.getRowValue().setValue(t.getNewValue()));
		attributesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
		attributesModifierColumn.setOnEditCommit(t -> t.getRowValue().setManualModifier(t.getNewValue()));

		if (type == AnimalType.HORSE) {
			final JSONObject attributes = actualAnimal.getObj("Eigenschaften");
			for (final String attribute : new String[] { "KO", "KK" }) {
				attributesTable.getItems().add(new AnimalAttribute(attribute, attributes.getObj(attribute)));
			}
			final JSONObject stats = actualAnimal.getObj("Basiswerte");
			for (final String stat : new String[] { "Tragkraft", "Zugkraft", "Loyalität", "Lebensenergie" }) {
				attributesTable.getItems().add(new AnimalAttribute(stat, stats.getObj(stat)));
			}
		} else {
			final JSONObject attributes = actualAnimal.getObj("Eigenschaften");
			for (final String attribute : ResourceManager.getResource("data/Eigenschaften").keySet()) {
				attributesTable.getItems().add(new AnimalAttribute(attribute, attributes.getObj(attribute)));
			}
		}

		attributesTable.setMinHeight(attributesTable.getItems().size() * 28 + 26);
		attributesTable.setMaxHeight(attributesTable.getItems().size() * 28 + 26);

		attributesTable.setRowFactory(t -> {
			final TableRow<AnimalAttribute> row = new TableRow<>();

			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem attributesContextMenuItem = new MenuItem("Bearbeiten");
			contextMenu.getItems().add(attributesContextMenuItem);
			attributesContextMenuItem.setOnAction(o -> {
				final Attribute attribute = row.getItem();
				new AnimalAttributeEditor(pane.getScene().getWindow(), attribute, type == AnimalType.MAGIC);
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
			biography.put("Abenteuerpunkte", ap.getValue());
			biography.put("Abenteuerpunkte-Guthaben", freeAp.getValue());

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

		ap.getValueFactory().setValue(biography.getIntOrDefault("Abenteuerpunkte", 0));
		ap.valueProperty().addListener((o, oldV, newV) -> freeAp.getValueFactory().setValue(freeAp.getValue() + newV - oldV));
		freeAp.getValueFactory().setValue(biography.getIntOrDefault("Abenteuerpunkte-Guthaben", 0));
		freeAp.valueProperty().addListener(biographyListener);
		rkw.getValueFactory().setValue(actualAnimal.getObj("Basiswerte").getObj("Ritualkenntnis (Vertrautenmagie)").getIntOrDefault("TaW", 3));
		rkw.valueProperty().addListener((o, oldV, newV) -> {
			if (oldV == newV || newV == null || oldV == null) return;
			final JSONObject ritualKnowledge = actualAnimal.getObj("Basiswerte").getObj("Ritualkenntnis (Vertrautenmagie)");
			ritualKnowledge.put("TaW", newV);
			ritualKnowledge.notifyListeners(null);
		});
	}

	private void initEquipment() {
		GUIUtil.cellValueFactories(equipmentTable, "name", "notes");

		final JSONArray items = actualAnimal.getArr("Ausrüstung");

		equipmentNameColumn.setCellFactory(o -> {
			final TableCell<InventoryItem, String> cell = new GraphicTableCell<>(false) {
				@Override
				protected void createGraphic() {
					final TextField t = new TextField();
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
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
					createGraphic(t, () -> t.getText(), s -> t.setText(s));
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

		equipmentTable.setRowFactory(tableView -> {
			final TableRow<InventoryItem> row = new TableRow<>();

			row.setOnDragDetected(e -> {
				if (row.isEmpty()) return;
				final Dragboard dragBoard = equipmentTable.startDragAndDrop(TransferMode.MOVE);
				final ClipboardContent content = new ClipboardContent();
				content.put(DataFormat.PLAIN_TEXT, row.getIndex());
				dragBoard.setContent(content);
			});

			row.setOnDragDropped(e -> {
				final JSONObject item = equipmentTable.getItems().get((Integer) e.getDragboard().getContent(DataFormat.PLAIN_TEXT)).getItem();
				items.remove(item);
				final int targetIndex = items.indexOf(row.getItem().getItem()) + 1;
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

			final ContextMenu contextMenu = new ContextMenu();

			final MenuItem editItem = new MenuItem("Bearbeiten");
			editItem.setOnAction(event -> new HorseArmorEditor(pane.getScene().getWindow(), equipmentTable.getSelectionModel().getSelectedItem()));

			final Menu location = new Menu("Ort");

			final JSONListener animalListener = o -> {
				if (row.getItem() != null) {
					updateLocationMenu(row.getItem().getItem(), location);
				}
			};
			final JSONArray[] animals = new JSONArray[] { hero.getArr("Tiere") };
			row.itemProperty().addListener((o, oldV, newV) -> {
				if (newV != null) {
					updateLocationMenu(newV.getItem(), location);
					animals[0].removeListener(animalListener);
					animals[0] = hero.getArr("Tiere");
					animals[0].addListener(animalListener);
				}
			});

			final MenuItem deleteItem = new MenuItem("Löschen");
			deleteItem.setOnAction(event -> {
				final JSONObject item = row.getItem().getItem();
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
			});

			contextMenu.getItems().addAll(editItem, location, deleteItem);

			row.setContextMenu(contextMenu);

			return row;
		});

		actualAnimal.getArr("Ausrüstung").addListener(o -> updateEquipment());

		updateEquipment();

		equipmentList.getItems().clear();

		DSAUtil.foreach(item -> true, (itemName, item) -> {
			equipmentList.getItems().add(itemName);
		}, ResourceManager.getResource("data/Ausruestung"));
	}

	private void initProConSkills() {
		GUIUtil.autosizeTable(proConsTable, 0, 2);
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
				createGraphic(t, () -> t.getText(), s -> t.setText(s));
			}

			@Override
			public void startEdit() {
				if (getItem() == null || "".equals(getItem())) return;
				super.startEdit();
			}
		});
		proConDescColumn.setOnEditCommit(t -> t.getRowValue().setDescription(t.getNewValue()));
		proConValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 9999, 1, false));
		proConValueColumn.setOnEditCommit(t -> t.getRowValue().setValue(t.getNewValue()));

		proConsTable.setRowFactory(t -> {
			final TableRow<ProConSkill> row = new TableRow<>();

			final ContextMenu proConContextMenu = new ContextMenu();
			final MenuItem proConDeleteItem = new MenuItem("Löschen");
			proConContextMenu.getItems().add(proConDeleteItem);
			proConDeleteItem.setOnAction(o -> {
				final JSONObject actual = actualAnimal.getObj("Eigenarten");
				final ProConSkill item = row.getItem();
				final String proOrConName = item.getName();
				final JSONObject proOrCon = ResourceManager.getResource("data/Tiereigenarten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein")
						.getObj(proOrConName);
				if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext")) {
					actual.getArr(proOrConName).remove(item.actual);
				} else {
					actual.removeKey(proOrConName);
				}
				actual.notifyListeners(null);
			});
			row.contextMenuProperty().bind(Bindings.when(row.itemProperty().isNotNull()).then(proConContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		actualAnimal.getObj("Eigenarten").addListener(o -> updateProCons());

		if (type == AnimalType.MAGIC) {
			GUIUtil.autosizeTable(ritualsTable, 0, 2);
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
				row.contextMenuProperty().bind(Bindings.when(row.itemProperty().isNotNull()).then(ritualContextMenu).otherwise((ContextMenu) null));

				return row;
			});

			actualAnimal.getObj("Fertigkeiten").addListener(o -> updateRituals());

			updateRituals();
		}

		GUIUtil.autosizeTable(skillsTable, 0, 2);
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
				createGraphic(t, () -> t.getText(), s -> t.setText(s));
			}

			@Override
			public void startEdit() {
				if (getItem() == null || "".equals(getItem())) return;
				super.startEdit();
			}
		});
		skillDescColumn.setOnEditCommit(t -> t.getRowValue().setDescription(t.getNewValue()));

		skillsTable.setRowFactory(t -> {
			final TableRow<ProConSkill> row = new TableRow<>();

			final ContextMenu skillContextMenu = new ContextMenu();
			final MenuItem skillDeleteItem = new MenuItem("Löschen");
			skillContextMenu.getItems().add(skillDeleteItem);
			skillDeleteItem.setOnAction(o -> {
				final JSONObject actual = actualAnimal.getObj("Fertigkeiten");
				final ProConSkill item = row.getItem();
				final String skillName = item.getName();
				final JSONObject skill = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein")
						.getObj(skillName);
				if (skill.containsKey("Auswahl") || skill.containsKey("Freitext")) {
					actual.getArr(skillName).remove(item.actual);
				} else {
					actual.removeKey(skillName);
				}
				actual.notifyListeners(null);
			});

			row.contextMenuProperty().bind(Bindings.when(row.itemProperty().isNotNull()).then(skillContextMenu).otherwise((ContextMenu) null));

			return row;
		});

		actualAnimal.getObj("Fertigkeiten").addListener(o -> updateSkills());

		updateProCons();
		updateSkills();
	}

	private void initStats() {
		statsTable.prefWidthProperty().bind(attributesBox.widthProperty().subtract(2).divide(1.667));
		final JSONObject baseValues = actualAnimal.getObj("Basiswerte");

		final JSONObject ini = baseValues.getObj("Initiative");
		iniBase.getValueFactory().setValue(ini.getIntOrDefault("Basis", 0));
		iniBase.valueProperty().addListener((o, oldV, newV) -> ini.put("Basis", newV));
		iniDiceNum.getValueFactory().setValue(ini.getIntOrDefault("Würfel:Anzahl", 1));
		iniDiceNum.valueProperty().addListener((o, oldV, newV) -> ini.put("WürfelAnzahl", newV));
		iniDiceType.getValueFactory().setValue(ini.getIntOrDefault("Würfel:Typ", 6));
		iniDiceType.valueProperty().addListener((o, oldV, newV) -> ini.put("Würfel:Typ", newV));
		iniMod.getValueFactory().setValue(ini.getIntOrDefault("Modifikator", 0));
		iniMod.valueProperty().addListener((o, oldV, newV) -> ini.put("Modifikator", newV));

		mrChoice.setItems(FXCollections.observableArrayList("Magieresistenz", "MR (Geist/Körper)"));
		final JSONObject actualMr = baseValues.getObj("Magieresistenz");
		mrChoice.getSelectionModel().selectedIndexProperty().addListener((o, oldV, newV) -> {
			if (newV.intValue() == 0 && type != AnimalType.HORSE) {
				mr.setManaged(true);
				mr.setVisible(true);
				mrBox.setManaged(false);
				mrBox.setVisible(false);
				if (type == AnimalType.MAGIC) {
					mrMod.setManaged(true);
					mrMod.setVisible(true);
					mrBought.setManaged(true);
					mrBought.setVisible(true);
				}
				mrModBox.setManaged(false);
				mrModBox.setVisible(false);
				mrBoughtBox.setManaged(false);
				mrBoughtBox.setVisible(false);
				actualMr.removeKey("Geist");
			} else {
				mr.setManaged(false);
				mr.setVisible(false);
				mrBox.setManaged(true);
				mrBox.setVisible(true);
				mrMod.setManaged(false);
				mrMod.setVisible(false);
				mrBought.setManaged(false);
				mrBought.setVisible(false);
				if (type == AnimalType.MAGIC) {
					mrModBox.setManaged(true);
					mrModBox.setVisible(true);
					mrBoughtBox.setManaged(true);
					mrBoughtBox.setVisible(true);
				}
			}
		});
		mrChoice.getSelectionModel().select(actualMr.containsKey("Geist") || type == AnimalType.HORSE ? 1 : 0);
		mr.getValueFactory().setValue(actualMr.getIntOrDefault("Wert", 0));
		mr.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Wert", newV));
		mrMind.getValueFactory().setValue(actualMr.getIntOrDefault("Geist", 0));
		mrMind.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Geist", newV));
		mrBody.getValueFactory().setValue(actualMr.getIntOrDefault("Körper", 0));
		mrBody.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Körper", newV));
		if (type == AnimalType.MAGIC) {
			mrMod.getValueFactory().setValue(actualMr.getIntOrDefault("Modifikator", 0));
			mrMod.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Modifikator", newV));
			mrMindMod.getValueFactory().setValue(actualMr.getIntOrDefault("Geist:Modifikator", 0));
			mrMindMod.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Geist:Modifikator", newV));
			mrBodyMod.getValueFactory().setValue(actualMr.getIntOrDefault("Körper:Modifikator", 0));
			mrBodyMod.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Körper:Modifikator", newV));
			mrBought.getValueFactory().setValue(actualMr.getIntOrDefault("Kauf", 0));
			mrBought.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Kauf", newV));
			mrMindBought.getValueFactory().setValue(actualMr.getIntOrDefault("Geist:Kauf", 0));
			mrMindBought.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Geist:Kauf", newV));
			mrBodyBought.getValueFactory().setValue(actualMr.getIntOrDefault("Körper:Kauf", 0));
			mrBodyBought.valueProperty().addListener((o, oldV, newV) -> actualMr.put("Körper:Kauf", newV));
		} else {
			mrMod.setVisible(false);
			mrModBox.setVisible(false);
			mrBought.setVisible(false);
			mrBoughtBox.setVisible(false);
		}

		if (type == AnimalType.HORSE) {
			speedChoice.setItems(FXCollections.observableArrayList("GS (Schritt/Trab/Galopp)"));
			speedChoice.getSelectionModel().select(0);
			final JSONObject speed = baseValues.getObj("Geschwindigkeit");
			speedWalk.getValueFactory().setValue(speed.getIntOrDefault("Schritt", 0));
			speedWalk.valueProperty().addListener((o, oldV, newV) -> speed.put("Schritt", newV));
			speedTrot.getValueFactory().setValue(speed.getIntOrDefault("Trab", 0));
			speedTrot.valueProperty().addListener((o, oldV, newV) -> speed.put("Trab", newV));
			speedGallop.getValueFactory().setValue(speed.getIntOrDefault("Galopp", 0));
			speedGallop.valueProperty().addListener((o, oldV, newV) -> speed.put("Galopp", newV));
			speedWalkMod.getValueFactory().setValue(speed.getIntOrDefault("Schritt:Modifikator", 0));
			speedWalkMod.valueProperty().addListener((o, oldV, newV) -> speed.put("Schritt:Modifikator", newV));
			speedTrotMod.getValueFactory().setValue(speed.getIntOrDefault("Trab:Modifikator", 0));
			speedTrotMod.valueProperty().addListener((o, oldV, newV) -> speed.put("Trab:Modifikator", newV));
			speedGallopMod.getValueFactory().setValue(speed.getIntOrDefault("Galopp:Modifikator", 0));
			speedGallopMod.valueProperty().addListener((o, oldV, newV) -> speed.put("Galopp:Modifikator", newV));

			final JSONObject stamina = baseValues.getObj("Ausdauer");
			staminaTrot.getValueFactory().setValue(stamina.getIntOrDefault("Trab", 0));
			staminaTrot.valueProperty().addListener((o, oldV, newV) -> stamina.put("Trab", newV));
			staminaGallop.getValueFactory().setValue(stamina.getIntOrDefault("Galopp", 0));
			staminaGallop.valueProperty().addListener((o, oldV, newV) -> stamina.put("Galopp", newV));
			staminaTrotMod.getValueFactory().setValue(stamina.getIntOrDefault("Trab:Modifikator", 0));
			staminaTrotMod.valueProperty().addListener((o, oldV, newV) -> stamina.put("Trab:Modifikator", newV));
			staminaGallopMod.getValueFactory().setValue(stamina.getIntOrDefault("Galopp:Modifikator", 0));
			staminaGallopMod.valueProperty().addListener((o, oldV, newV) -> stamina.put("Galopp:Modifikator", newV));

			final JSONObject feed = baseValues.getObj("Futterbedarf");
			feedBase.getValueFactory().setValue(feed.getIntOrDefault("Erhaltung", 0));
			feedBase.valueProperty().addListener((o, oldV, newV) -> feed.put("Erhaltung", newV));
			feedLight.getValueFactory().setValue(feed.getIntOrDefault("Leicht", 0));
			feedLight.valueProperty().addListener((o, oldV, newV) -> feed.put("Leicht", newV));
			feedMedium.getValueFactory().setValue(feed.getIntOrDefault("Mittel", 0));
			feedMedium.valueProperty().addListener((o, oldV, newV) -> feed.put("Mittel", newV));
			feedHeavy.getValueFactory().setValue(feed.getIntOrDefault("Schwer", 0));
			feedHeavy.valueProperty().addListener((o, oldV, newV) -> feed.put("Schwer", newV));
		} else {
			speedChoice.setItems(FXCollections.observableArrayList("Geschwindigkeit", "GS (Boden/Luft)"));
			final JSONObject actualSpeed = baseValues.getObj("Geschwindigkeit");
			speedChoice.getSelectionModel().selectedIndexProperty().addListener((o, oldV, newV) -> {
				if (newV.intValue() == 0) {
					speed.setManaged(true);
					speed.setVisible(true);
					speedBox.setManaged(false);
					speedBox.setVisible(false);
					speedMod.setManaged(true);
					speedMod.setVisible(true);
					speedModBox.setManaged(false);
					speedModBox.setVisible(false);
					speedBought.setManaged(true);
					speedBought.setVisible(true);
					actualSpeed.removeKey("Boden");
				} else {
					speed.setManaged(false);
					speed.setVisible(false);
					speedBox.setManaged(true);
					speedBox.setVisible(true);
					speedMod.setManaged(false);
					speedMod.setVisible(false);
					speedModBox.setManaged(true);
					speedModBox.setVisible(true);
					speedBought.setManaged(false);
					speedBought.setVisible(false);
					speedBoughtBox.setManaged(true);
					speedBoughtBox.setVisible(true);
				}
			});
			speedChoice.getSelectionModel().select(actualSpeed.containsKey("Boden") ? 1 : 0);

			speed.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Wert", 0.0));
			speed.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Wert", newV));
			speedGround.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Boden", 0.0));
			speedGround.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Boden", newV));
			speedAir.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Luft", 0.0));
			speedAir.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Luft", newV));
			speedMod.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Modifikator", 0.0));
			speedMod.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Modifikator", newV));
			speedGroundMod.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Boden:Modifikator", 0.0));
			speedGroundMod.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Boden:Modifikator", newV));
			speedAirMod.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Luft:Modifikator", 0.0));
			speedAirMod.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Luft:Modifikator", newV));

			if (type == AnimalType.MAGIC) {
				speedBought.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Kauf", 0.0));
				speedBought.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Kauf", newV));
				speedGroundBought.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Boden:Kauf", 0.0));
				speedGroundBought.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Boden:Kauf", newV));
				speedAirBought.getValueFactory().setValue(actualSpeed.getDoubleOrDefault("Luft:Kauf", 0.0));
				speedAirBought.valueProperty().addListener((o, oldV, newV) -> actualSpeed.put("Luft:Kauf", newV));
			}

			if (type != AnimalType.MAGIC) {
				statsBoughtColumn.setMinWidth(0.0);
				statsBoughtColumn.setPrefWidth(0.0);
				statsBoughtColumn.setMaxWidth(0.0);
				statsBoughtColumn.setVisible(false);
			}

			GUIUtil.autosizeTable(statsTable, 0, 2);
			GUIUtil.cellValueFactories(statsTable, "name", "value", "bought", "manualModifier", "current");

			statsValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 999, 1, false));
			statsValueColumn.setOnEditCommit(t -> t.getRowValue().setValue(t.getNewValue()));
			statsBoughtColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99, 1, false));
			statsBoughtColumn.setOnEditCommit(t -> t.getRowValue().setBought(t.getNewValue()));
			statsModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
			statsModifierColumn.setOnEditCommit(t -> t.getRowValue().setManualModifier(t.getNewValue()));

			for (final String attribute : new String[] { "Loyalität", "Rüstungsschutz", "Lebensenergie", "Ausdauer" }) {
				statsTable.getItems().add(new AnimalAttribute(attribute, baseValues.getObj(attribute)));
			}
			if (type == AnimalType.MAGIC) {
				statsTable.getItems().add(new AnimalAttribute("Astralenergie", baseValues.getObj("Astralenergie")));
			} else {
				statsTable.getItems().add(new AnimalAttribute("Fährtensuchen", baseValues.getObjOrDefault("Fährtensuchen", new JSONObject(baseValues))));
			}

			statsTable.setPrefHeight(statsTable.getItems().size() * 28 + 26);
			statsTable.setMinHeight(statsTable.getItems().size() * 28 + 26);

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
	}

	private void updateAttacks() {
		attacksTable.getItems().clear();

		final JSONObject attacks = actualAnimal.getObj("Angriffe");
		for (final String attack : attacks.keySet()) {
			attacksTable.getItems().add(new Attack(attack, attacks.getObj(attack)));
		}

		attacksTable.setMinHeight(attacksTable.getItems().size() * 28 + 26);
		attacksTable.setMaxHeight(attacksTable.getItems().size() * 28 + 26);
	}

	private void updateEquipment() {
		equipmentTable.getItems().clear();

		HeroUtil.foreachInventoryItem(true, item -> true, (item, fromAnimal) -> {
			final InventoryItem newItem = new InventoryItem(item, item);
			newItem.recompute();
			equipmentTable.getItems().add(newItem);
		}, actualAnimal);

		equipmentTable.setMinHeight((equipmentTable.getItems().size() + 1) * 26 + 1);
		equipmentTable.setMaxHeight((equipmentTable.getItems().size() + 1) * 26 + 1);
	}

	private void updateLocationMenu(final JSONObject item, final Menu location) {
		location.getItems().clear();

		final ToggleGroup locationGroup = new ToggleGroup();

		final RadioMenuItem heroLocationItem = new RadioMenuItem(hero.getObj("Biografie").getString("Vorname"));
		hero.getObj("Biografie").addLocalListener(o -> heroLocationItem.setText(hero.getObj("Biografie").getString("Vorname")));
		heroLocationItem.setToggleGroup(locationGroup);
		location.getItems().add(heroLocationItem);

		final JSONArray animals = hero.getArr("Tiere");
		final Map<JSONObject, RadioMenuItem> animalItems = new HashMap<>(animals.size());
		for (int i = 0; i < animals.size(); ++i) {
			final JSONObject animal = animals.getObj(i);
			final String name = animal.getObj("Biografie").getString("Name");
			final RadioMenuItem animalLocationItem = new RadioMenuItem(name);
			animalLocationItem.setToggleGroup(locationGroup);
			animalItems.put(animal, animalLocationItem);
			location.getItems().add(animalLocationItem);
		}

		location.setOnShowing(e -> {
			final JSONValue possessor = item.getParent() != null ? item.getParent().getParent() : null;
			heroLocationItem.setOnAction(e2 -> {
				final JSONValue parent = item.getParent();
				parent.remove(item);
				parent.notifyListeners(null);
				final JSONArray equipment = hero.getObj("Besitz").getArr("Ausrüstung");
				equipment.add(item.clone(equipment));
				equipment.notifyListeners(null);
				location.getParentPopup().hide();
			});
			for (final JSONObject animal : animalItems.keySet()) {
				if (possessor != null && animal.equals(possessor)) {
					animalItems.get(animal).setSelected(true);
				} else {
					animalItems.get(animal).setOnAction(e2 -> {
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
		});
	}

	private void updateProCons() {
		proConsTable.getItems().clear();

		final JSONObject prosOrCons = ResourceManager.getResource("data/Tiereigenarten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein");
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

		proConsTable.setMinHeight(proConsTable.getItems().size() * 28 + 26);
		proConsTable.setMaxHeight(proConsTable.getItems().size() * 28 + 26);

		proConsList.getItems().clear();
		for (final String proOrConName : prosOrCons.keySet()) {
			final JSONObject proOrCon = prosOrCons.getObj(proOrConName);
			if (proOrCon.containsKey("Auswahl") || proOrCon.containsKey("Freitext") || !actualProsOrCons.containsKey(proOrConName)) {
				proConsList.getItems().add(proOrConName);
			}
		}

		proConsList.getSelectionModel().select(0);
		proConsAddButton.setDisable(proConsList.getItems().isEmpty());
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

		ritualsTable.setMinHeight(ritualsTable.getItems().size() * 28 + 26);
		ritualsTable.setMaxHeight(ritualsTable.getItems().size() * 28 + 26);

		ritualsList.getItems().clear();
		for (final String ritualName : rituals.keySet()) {
			if (!actualSkills.containsKey(ritualName)) {
				ritualsList.getItems().add(ritualName);
			}
		}

		ritualsList.getSelectionModel().select(0);
		ritualsAddButton.setDisable(ritualsList.getItems().isEmpty());
	}

	private void updateSkills() {
		skillsTable.getItems().clear();

		final JSONObject skills = ResourceManager.getResource("data/Tierfertigkeiten").getObj(type == AnimalType.HORSE ? "Reittiere" : "Allgemein");
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

		skillsTable.setMinHeight(skillsTable.getItems().size() * 28 + 26);
		skillsTable.setMaxHeight(skillsTable.getItems().size() * 28 + 26);

		skillsList.getItems().clear();
		for (final String skillName : skills.keySet()) {
			final JSONObject skill = skills.getObj(skillName);
			if (skill.containsKey("Auswahl") || skill.containsKey("Freitext") || !actualSkills.containsKey(skillName)) {
				skillsList.getItems().add(skillName);
			}
		}

		skillsList.getSelectionModel().select(0);
		skillsAddButton.setDisable(skillsList.getItems().isEmpty());
	}
}
