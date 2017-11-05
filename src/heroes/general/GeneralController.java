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
package heroes.general;

import java.time.LocalDate;

import dsa41basis.hero.Attribute;
import dsa41basis.hero.DerivedValue;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.util.ColoredProgressBarTableCell;
import dsatool.util.ErrorLogger;
import dsatool.util.IntegerSpinnerTableCell;
import dsatool.util.ReactiveSpinner;
import dsatool.util.Tuple;
import heroes.ui.HeroTabController;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class GeneralController extends HeroTabController {

	private static Tuple<String, String[]> splitModifiedString(final String input) {
		final int modifierPos = input.indexOf('(');
		if (modifierPos == -1) return new Tuple<>(input, new String[0]);
		final String base = input.substring(0, modifierPos).trim();
		final int endModifier = input.lastIndexOf(')');
		final String modifier = input.substring(modifierPos + 1, endModifier == -1 ? input.length() : endModifier);
		return new Tuple<>(base, modifier.trim().split("\\s*,\\s*"));
	}

	@FXML
	private Label age;
	@FXML
	private TableColumn<Attribute, Integer> attributesCurrentColumn;
	@FXML
	private TableColumn<Attribute, Integer> attributesModifierColumn;
	@FXML
	private TableColumn<Attribute, String> attributesNameColumn;
	@FXML
	private TableView<Attribute> attributesTable;
	@FXML
	private TableColumn<Attribute, Integer> attributesValueColumn;
	@FXML
	private ReactiveSpinner<Integer> birthday;
	@FXML
	private ComboBox<String> birthmonth;
	@FXML
	private ReactiveSpinner<Integer> birthyear;
	@FXML
	private TextField culture;
	@FXML
	private TableColumn<DerivedValue, Integer> derivedCurrentColumn;
	@FXML
	private TableColumn<DerivedValue, Integer> derivedModifierColumn;
	@FXML
	private TableColumn<DerivedValue, String> derivedNameColumn;
	@FXML
	private TableView<DerivedValue> derivedValuesTable;
	@FXML
	private TableColumn<Energy, Integer> energiesBoughtColumn;
	@FXML
	private TableColumn<Energy, Double> energiesCurrentColumn;
	@FXML
	private TableColumn<Energy, Integer> energiesModifierColumn;
	@FXML
	private TableColumn<Energy, String> energiesNameColumn;
	@FXML
	private TableColumn<Energy, Integer> energiesPermanentColumn;
	@FXML
	private TableView<Energy> energiesTable;
	@FXML
	private ComboBox<String> eyecolor;
	@FXML
	private ComboBox<String> gender;
	@FXML
	private ComboBox<String> haircolor;
	@FXML
	private Label haircolorLabel;
	@FXML
	private TextField name;
	@FXML
	private ScrollPane pane;
	@FXML
	private TextField player;
	@FXML
	private TextField profession;
	@FXML
	private Label professionModifier;
	@FXML
	private TextField race;
	@FXML
	private ReactiveSpinner<Integer> size;
	@FXML
	private ComboBox<String> skincolor;
	@FXML
	private Label skincolorLabel;
	@FXML
	private TextField surname;
	@FXML
	private ReactiveSpinner<Integer> weight;
	@FXML
	private ReactiveSpinner<Integer> socialstate;
	@FXML
	private ReactiveSpinner<Integer> ap;
	@FXML
	private Label investedAp;
	@FXML
	private ReactiveSpinner<Integer> freeAp;

	private boolean update = false;

	private final JSONListener heroBioListener = o -> {
		setBiography();
		setAppearance();
	};

	private final ChangeListener<Object> bioListener = (observable, oldValue, newValue) -> {
		if (update || newValue == null || oldValue == null || oldValue.equals(newValue)) return;
		final JSONObject bio = getHero().getObj("Biografie");
		bio.put("Vorname", name.getText());
		bio.put("Nachname", surname.getText());
		getHero().put("Spieler", player.getText());
		getHero().getObj("Basiswerte").getObj("Sozialstatus").put("Wert", socialstate.getValue());
		bio.put("Abenteuerpunkte", ap.getValue());
		bio.put("Abenteuerpunkte-Guthaben", freeAp.getValue());
		investedAp.setText(Integer.toString(ap.getValue() - freeAp.getValue()));
		changeModifiedString("Rasse", race.getText());
		changeModifiedString("Kultur", culture.getText());
		changeModifiedString("Profession", profession.getText());
		bio.put("Geburtstag", birthday.getValue());
		bio.put("Geburtsmonat", birthmonth.getSelectionModel().getSelectedIndex() + 1);
		bio.put("Geburtsjahr", birthyear.getValue());
		bio.put("Geschlecht", gender.getSelectionModel().getSelectedItem());
		bio.put("Größe", size.getValue());
		bio.put("Gewicht", weight.getValue());
		bio.put("Augenfarbe", eyecolor.getValue());
		if (!haircolor.valueProperty().equals(observable) || !"".equals(newValue)) {
			bio.put(bio.containsKey("Schuppenfarbe 1") ? "Schuppenfarbe 1" : "Haarfarbe", haircolor.getValue());
		}
		if (!skincolor.valueProperty().equals(observable) || !"".equals(newValue)) {
			bio.put(bio.containsKey("Schuppenfarbe 2") ? "Schuppenfarbe 2" : "Hautfarbe", skincolor.getValue());
		}
		bio.notifyListeners(heroBioListener);
		getHero().getObj("Basiswerte").notifyListeners(heroBioListener);
		if (name.focusedProperty().equals(observable)) {
			ResourceManager.moveResource(getHero(), "characters/" + bio.getString("Vorname"));
		}
	};

	public GeneralController(final TabPane tabPane) {
		super(tabPane);
	}

	@Override
	protected void changeEditable() {
		name.setEditable(HeroTabController.isEditable.get());
		surname.setEditable(HeroTabController.isEditable.get());
		player.setEditable(HeroTabController.isEditable.get());
		race.setEditable(HeroTabController.isEditable.get());
		culture.setEditable(HeroTabController.isEditable.get());
		profession.setEditable(HeroTabController.isEditable.get());
		freeAp.setDisable(!HeroTabController.isEditable.get());
		socialstate.setDisable(!HeroTabController.isEditable.get());
		birthday.setDisable(!HeroTabController.isEditable.get());
		birthmonth.setDisable(!HeroTabController.isEditable.get());
		birthyear.setDisable(!HeroTabController.isEditable.get());
		gender.setDisable(!HeroTabController.isEditable.get());
		size.setDisable(!HeroTabController.isEditable.get());
		weight.setDisable(!HeroTabController.isEditable.get());
		eyecolor.setDisable(!HeroTabController.isEditable.get());
		haircolor.setDisable(!HeroTabController.isEditable.get());
		skincolor.setDisable(!HeroTabController.isEditable.get());
		energiesPermanentColumn.setEditable(HeroTabController.isEditable.get());
	}

	private void changeModifiedString(final String base, final String input) {
		final JSONObject bio = hero.getObj("Biografie");
		final Tuple<String, String[]> tuple = splitModifiedString(input);
		bio.put(base, tuple._1);
		if (tuple._2.length > 0) {
			final JSONArray modifikation = new JSONArray(bio);
			for (final String modifier : tuple._2) {
				modifikation.add(modifier);
			}
			bio.put(base + ":Modifikation", modifikation);
		} else {
			bio.removeKey(base + ":Modifikation");
		}
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	private JSONObject getHero() {
		return hero;
	}

	@Override
	protected String getText() {
		return "Allgemein";
	}

	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("General.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject general = ResourceManager.getResource("data/Allgemein");
		if (general.containsKey("Zeit")) {
			final JSONObject time = general.getObj("Zeit");
			time.addLocalListener(o -> {
				setAge((JSONObject) o);
			});
		}

		super.init();

		birthmonth.setItems(FXCollections.observableArrayList(DSAUtil.months));

		gender.setItems(FXCollections.observableArrayList("männlich", "weiblich"));

		eyecolor.setItems(FXCollections.observableArrayList(HeroUtil.eyeColors));

		attributesTable.prefWidthProperty().bind(pane.widthProperty().subtract(22).divide(2));

		GUIUtil.autosizeTable(attributesTable, 0, 2);
		GUIUtil.cellValueFactories(attributesTable, "name", "value", "manualModifier", "current");
		attributesValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 30, 1, false));
		attributesValueColumn.setOnEditCommit(t -> {
			if (HeroTabController.isEditable.get()) {
				t.getRowValue().setValue(t.getNewValue());
			} else {
				new AttributeEnhancementDialog(pane.getScene().getWindow(), t.getRowValue(), hero,
						t.getNewValue());
			}
		});
		attributesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
		attributesModifierColumn.setOnEditCommit(t -> t.getRowValue().setManualModifier(t.getNewValue()));

		final ContextMenu attributesContextMenu = new ContextMenu();
		final MenuItem attributesEnhanceItem = new MenuItem("Steigern");
		attributesContextMenu.getItems().add(attributesEnhanceItem);
		attributesEnhanceItem.setOnAction(o -> {
			final Attribute attribute = attributesTable.getSelectionModel().getSelectedItem();
			new AttributeEnhancementDialog(pane.getScene().getWindow(), attribute, hero, attribute.getValue() + 1);
		});
		final MenuItem attributesEditItem = new MenuItem("Bearbeiten");
		attributesContextMenu.getItems().add(attributesEditItem);
		attributesEditItem.setOnAction(o -> {
			final Attribute attribute = attributesTable.getSelectionModel().getSelectedItem();
			new AttributeEditor(pane.getScene().getWindow(), attribute);
		});
		attributesTable.setContextMenu(attributesContextMenu);

		derivedValuesTable.prefWidthProperty().bind(pane.widthProperty().subtract(22).divide(2));

		GUIUtil.autosizeTable(derivedValuesTable, 0, 2);
		GUIUtil.cellValueFactories(derivedValuesTable, "name", "manualModifier", "current");
		derivedModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
		derivedModifierColumn.setOnEditCommit(t -> t.getRowValue().setManualModifier(t.getNewValue()));
		derivedCurrentColumn.setCellValueFactory(new PropertyValueFactory<DerivedValue, Integer>("current"));

		final ContextMenu derivedContextMenu = new ContextMenu();
		final MenuItem derivedContextMenuItem = new MenuItem("Bearbeiten");
		derivedContextMenu.getItems().add(derivedContextMenuItem);
		derivedContextMenuItem.setOnAction(o -> {
			final DerivedValue value = derivedValuesTable.getSelectionModel().getSelectedItem();
			new DerivedValueEditor(pane.getScene().getWindow(), value, false);
		});
		derivedValuesTable.setContextMenu(derivedContextMenu);

		energiesTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(energiesTable, 4, 2);
		GUIUtil.cellValueFactories(energiesTable, "name", "permanent", "bought", "manualModifier", "currentPercentage");
		energiesPermanentColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
		energiesPermanentColumn.setOnEditCommit(t -> t.getRowValue().setPermanent(t.getNewValue()));
		energiesBoughtColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99, 1, false));
		energiesBoughtColumn.setOnEditCommit(t -> {
			if (HeroTabController.isEditable.get()) {
				t.getRowValue().setBought(t.getNewValue());
			} else {
				final Energy energy = t.getRowValue();
				new EnergyEnhancementDialog(pane.getScene().getWindow(), energy, hero, energy.getMax() - energy.getBought() + t.getNewValue());
			}
		});
		energiesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99, 1, false));
		energiesModifierColumn.setOnEditCommit(t -> t.getRowValue().setManualModifier(t.getNewValue()));
		energiesCurrentColumn.setCellFactory(o -> new ColoredProgressBarTableCell<>());

		final ContextMenu energiesContextMenu = new ContextMenu();
		final MenuItem energiesEnhanceItem = new MenuItem("Zukaufen");
		energiesContextMenu.getItems().add(energiesEnhanceItem);
		energiesEnhanceItem.setOnAction(o -> {
			final Energy value = energiesTable.getSelectionModel().getSelectedItem();
			if ("Karmaenergie".equals(value.getName())) {
				final Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Zukauf nicht möglich");
				alert.setHeaderText("Karmaenergie kann nicht zugekauft werden");
				alert.setContentText("Nutze die Karmalqueste, um mehr Karmaenegie zu erhalten");
				alert.showAndWait();
			} else {
				new EnergyEnhancementDialog(pane.getScene().getWindow(), value, hero, value.getMax() + 1);
			}
		});
		final MenuItem energiesEditItem = new MenuItem("Bearbeiten");
		energiesContextMenu.getItems().add(energiesEditItem);
		energiesEditItem.setOnAction(o -> {
			final DerivedValue value = energiesTable.getSelectionModel().getSelectedItem();
			new DerivedValueEditor(pane.getScene().getWindow(), value, !"Karmaenergie".equals(value.getName()));
		});
		energiesTable.setContextMenu(energiesContextMenu);

		registerListeners();
	}

	private void registerListeners() {
		name.focusedProperty().addListener(bioListener);
		surname.focusedProperty().addListener(bioListener);
		player.focusedProperty().addListener(bioListener);

		socialstate.valueProperty().addListener(bioListener);
		ap.valueProperty().addListener((o, oldV, newV) -> {
			freeAp.getValueFactory().setValue(freeAp.getValue() + newV - oldV);
			if (!update && oldV != newV) {
				final JSONArray history = getHero().getArr("Historie");
				final JSONObject lastEntry = history.size() == 0 ? null : history.getObj(history.size() - 1);
				final LocalDate currentDate = LocalDate.now();
				if (lastEntry != null && "Abenteuerpunkte".equals(lastEntry.getString("Typ"))
						&& currentDate.equals(LocalDate.parse(lastEntry.getString("Datum")))) {
					lastEntry.put("Auf", newV);
				} else {
					final JSONObject historyEntry = new JSONObject(history);
					historyEntry.put("Typ", "Abenteuerpunkte");
					historyEntry.put("Von", oldV);
					historyEntry.put("Auf", newV);
					historyEntry.put("Datum", currentDate.toString());
					history.add(historyEntry);
				}
			}
		});
		freeAp.valueProperty().addListener(bioListener);

		race.focusedProperty().addListener(bioListener);
		culture.focusedProperty().addListener(bioListener);
		profession.focusedProperty().addListener(bioListener);

		birthday.valueProperty().addListener(bioListener);
		birthmonth.getSelectionModel().selectedIndexProperty().addListener(bioListener);
		birthyear.valueProperty().addListener(bioListener);
		gender.getSelectionModel().selectedIndexProperty().addListener(bioListener);
		size.valueProperty().addListener(bioListener);
		weight.valueProperty().addListener(bioListener);
		eyecolor.valueProperty().addListener(bioListener);
		haircolor.valueProperty().addListener(bioListener);
		skincolor.valueProperty().addListener(bioListener);
	}

	private void setAge(final JSONObject currentDate) {
		if (currentDate != null) {
			final JSONObject bio = hero.getObj("Biografie");
			age.setText(Integer.toString(DSAUtil.getDaysBetween(bio.getIntOrDefault("Geburtstag", 1), bio.getIntOrDefault("Geburtsmonat", 1),
					bio.getIntOrDefault("Geburtsjahr", 1000), currentDate.getIntOrDefault("Tag", 1), currentDate.getIntOrDefault("Monat", 1),
					currentDate.getIntOrDefault("Jahr", 1000)) / 365));
		}
	}

	private void setAppearance() {
		final JSONObject bio = hero.getObj("Biografie");

		birthday.getValueFactory().setValue(bio.getIntOrDefault("Geburtstag", 1));
		birthmonth.getSelectionModel().clearAndSelect(bio.getIntOrDefault("Geburtsmonat", 1) - 1);
		birthyear.getValueFactory().setValue(bio.getIntOrDefault("Geburtsjahr", 1000));

		setAge(ResourceManager.getResource("data/Allgemein").getObj("Zeit"));

		gender.getSelectionModel().clearAndSelect("weiblich".equals(bio.getString("Geschlecht")) ? 1 : 0);

		size.getValueFactory().setValue(bio.getIntOrDefault("Größe", 0));

		weight.getValueFactory().setValue(bio.getIntOrDefault("Gewicht", 0));

		eyecolor.setValue(bio.getStringOrDefault("Augenfarbe", "braun"));

		final boolean scalecolor = bio.containsKey("Schuppenfarbe 1");
		haircolorLabel.setText(scalecolor ? "Schuppenfarbe 1:" : "Haarfarbe:");
		skincolorLabel.setText(scalecolor ? "Schuppenfarbe 2:" : "Hautfarbe:");

		final String newHairColor = bio.getStringOrDefault(scalecolor ? "Schuppenfarbe 1" : "Haarfarbe", scalecolor ? "hellgrün" : "schwarz");
		final String newSkinColor = bio.getStringOrDefault(scalecolor ? "Schuppenfarbe 2" : "Hautfarbe", scalecolor ? "hellgrün" : "weiß");

		haircolor.setItems(FXCollections.observableArrayList(scalecolor ? HeroUtil.scaleColors : HeroUtil.hairColors));
		skincolor.setItems(FXCollections.observableArrayList(scalecolor ? HeroUtil.scaleColors : HeroUtil.skinColors));
		haircolor.setValue(newHairColor);
		skincolor.setValue(newSkinColor);
	}

	private void setAttributesAndDerivedValues() {
		attributesTable.getItems().clear();
		derivedValuesTable.getItems().clear();
		energiesTable.getItems().clear();

		final JSONObject derivedValues = ResourceManager.getResource("data/Basiswerte");
		final JSONObject attributes = ResourceManager.getResource("data/Eigenschaften");
		final JSONObject actualAttributes = hero.getObj("Eigenschaften");
		final JSONObject basicValues = hero.getObj("Basiswerte");

		for (final String attribute : attributes.keySet()) {
			attributesTable.getItems().add(new Attribute(attribute, actualAttributes.getObj(attribute)));
		}

		for (final String derivedValue : new String[] { "Attacke-Basis", "Parade-Basis", "Fernkampf-Basis", "Initiative-Basis", "Wundschwelle",
				"Geschwindigkeit" }) {
			derivedValuesTable.getItems().add(new DerivedValue(derivedValue, derivedValues.getObj(derivedValue), actualAttributes, basicValues));
		}

		energiesTable.getItems().add(new Energy("Lebensenergie", derivedValues.getObj("Lebensenergie"), actualAttributes, basicValues, Color.RED));
		energiesTable.getItems().add(new Energy("Ausdauer", derivedValues.getObj("Ausdauer"), actualAttributes, basicValues, Color.DODGERBLUE));
		if (HeroUtil.isMagical(hero)) {
			energiesTable.getItems()
					.add(new Energy("Astralenergie", derivedValues.getObj("Astralenergie"), actualAttributes, basicValues, Color.VIOLET.saturate()));
		}
		if (HeroUtil.isClerical(hero, false)) {
			energiesTable.getItems().add(new Energy("Karmaenergie", new JSONObject(null), actualAttributes, basicValues, Color.YELLOW.darker()));
		}
		energiesTable.getItems().add(new Energy("Magieresistenz", derivedValues.getObj("Magieresistenz"), actualAttributes, basicValues, Color.WHITE));

		attributesTable.setMinHeight(attributesTable.getItems().size() * 28 + 26);
		attributesTable.setMaxHeight(attributesTable.getItems().size() * 28 + 26);
		derivedValuesTable.setMinHeight(derivedValuesTable.getItems().size() * 28 + 26);
		derivedValuesTable.setMaxHeight(derivedValuesTable.getItems().size() * 28 + 26);
		energiesTable.setMinHeight(energiesTable.getItems().size() * 28 + 24);
		energiesTable.setMaxHeight(energiesTable.getItems().size() * 28 + 24);
	}

	private void setBiography() {
		final JSONObject bio = hero.getObj("Biografie");

		name.setText(bio.getStringOrDefault("Vorname", ""));
		surname.setText(bio.getStringOrDefault("Nachname", ""));
		player.setText(hero.getStringOrDefault("Spieler", ""));

		socialstate.getValueFactory().setValue(hero.getObj("Basiswerte").getObj("Sozialstatus").getIntOrDefault("Wert", 0));
		final boolean isUpdate = update;
		final int apTotal = bio.getIntOrDefault("Abenteuerpunkte", 0);
		final int apFree = bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0);
		update = true;
		ap.getValueFactory().setValue(apTotal);
		update = isUpdate;
		investedAp.setText(Integer.toString(apTotal - apFree));
		freeAp.getValueFactory().setValue(apFree);

		final StringBuilder raceString = new StringBuilder(bio.getStringOrDefault("Rasse", ""));
		if (bio.containsKey("Rasse:Modifikation")) {
			final JSONArray raceModifiers = bio.getArr("Rasse:Modifikation");
			raceString.append(" (");
			raceString.append(String.join(", ", raceModifiers.getStrings()));
			raceString.append(")");
		}
		race.setText(raceString.toString());

		final StringBuilder cultureString = new StringBuilder(bio.getStringOrDefault("Kultur", ""));
		if (bio.containsKey("Kultur:Modifikation")) {
			final JSONArray cultureModifiers = bio.getArr("Kultur:Modifikation");
			cultureString.append(" (");
			cultureString.append(String.join(", ", cultureModifiers.getStrings()));
			cultureString.append(")");
		}
		culture.setText(cultureString.toString());

		final StringBuilder professionString = new StringBuilder(bio.getStringOrDefault("Profession", ""));
		if (bio.containsKey("Profession:Modifikation")) {
			final JSONArray professionModifiers = bio.getArr("Profession:Modifikation");
			professionString.append(" (");
			professionString.append(String.join(", ", professionModifiers.getStrings()));
			professionString.append(")");
		}
		profession.setText(professionString.toString());

		final StringBuilder professionModifierString = new StringBuilder();
		final JSONObject pros = hero.getObj("Vorteile");
		if (pros != null) {
			if (pros.containsKey("Veteran")) {
				final JSONObject veteran = pros.getObj("Veteran");
				professionModifierString.append("Veteran");
				if (veteran != null && veteran.containsKey("Profession:Modifikation")) {
					professionModifierString.append(' ');
					professionModifierString.append(String.join(", ", veteran.getArr("Profession:Modifikation").getStrings()));
				}
			}
			if (pros.containsKey("Breitgefächerte Bildung")) {
				final JSONObject bgb = pros.getObj("Breitgefächerte Bildung");
				professionModifierString.append("BGB ");
				professionModifierString.append(bgb.getString("Profession"));
				if (bgb.containsKey("Profession:Modifikation")) {
					professionModifierString.append(" (");
					professionModifierString.append(String.join(", ", bgb.getArr("Profession:Modifikation").getStrings()));
					professionModifierString.append(")");
				}
			}
		}
		professionModifier.setText(professionModifierString.toString());
	}

	@Override
	public void setHero(final JSONObject hero) {
		if (this.hero != null) {
			this.hero.removeListener(heroBioListener);
		}
		super.setHero(hero);
		if (hero != null) {
			hero.addListener(heroBioListener);
		}
	}

	@FXML
	private void toggleEditable() {
		HeroTabController.isEditable.set(!HeroTabController.isEditable.get());
	}

	@Override
	protected void update() {
		update = true;
		setBiography();
		setAppearance();
		setAttributesAndDerivedValues();
		update = false;
	}
}
