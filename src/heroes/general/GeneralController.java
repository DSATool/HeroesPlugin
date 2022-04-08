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
import dsa41basis.ui.hero.SingleRollDialog;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.ColoredProgressBarTableCell;
import dsatool.ui.IntegerSpinnerTableCell;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple;
import dsatool.util.Util;
import heroes.ui.HeroTabController;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
	private TableColumn<HeroEnergy, Integer> energiesBoughtColumn;
	@FXML
	private TableColumn<HeroEnergy, Double> energiesCurrentColumn;
	@FXML
	private TableColumn<HeroEnergy, Integer> energiesModifierColumn;
	@FXML
	private TableColumn<HeroEnergy, String> energiesNameColumn;
	@FXML
	private TableColumn<HeroEnergy, Integer> energiesPermanentColumn;
	@FXML
	private TableView<HeroEnergy> energiesTable;
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

	private final JSONListener timeListener = o -> setAge((JSONObject) o);

	public GeneralController(final TabPane tabPane) {
		super(tabPane);
	}

	private void changeModifiedProfession(final String input) {
		final JSONObject bio = hero.getObj("Biografie");
		final boolean female = "weiblich".equals(gender.getSelectionModel().getSelectedItem());
		final Tuple<String, String[]> tuple = splitModifiedString(input);
		bio.put("Profession", female ? DSAUtil.replaceProfessionGender(tuple._1) : tuple._1);
		if (tuple._2.length > 0) {
			final JSONArray modifikation = new JSONArray(bio);
			for (final String modifier : tuple._2) {
				modifikation.add(female ? DSAUtil.replaceProfessionGender(modifier) : modifier);
			}
			bio.put("Profession:Modifikation", modifikation);
		} else {
			bio.removeKey("Profession:Modifikation");
		}
		bio.notifyListeners(heroBioListener);
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
		bio.notifyListeners(heroBioListener);
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
			time.addLocalListener(timeListener);
		}

		super.init();

		birthmonth.setItems(FXCollections.observableArrayList(DSAUtil.months));

		gender.setItems(FXCollections.observableArrayList("männlich", "weiblich"));

		eyecolor.setItems(FXCollections.observableArrayList(HeroUtil.eyeColors));

		attributesTable.prefWidthProperty().bind(pane.widthProperty().subtract(22).divide(2));

		GUIUtil.autosizeTable(attributesTable);
		GUIUtil.cellValueFactories(attributesTable, "name", "value", "manualModifier", "current");
		attributesValueColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 30));
		attributesValueColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null)
				if (HeroTabController.isEditable.get()) {
					t.getRowValue().setValue(t.getNewValue());
				} else if (!t.getNewValue().equals(t.getOldValue())) {
					new AttributeEnhancementDialog(pane.getScene().getWindow(), t.getRowValue(), hero,
							t.getNewValue());
				}
		});
		attributesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		attributesModifierColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setManualModifier(t.getNewValue());
			}
		});

		attributesTable.setRowFactory(t -> {
			final TableRow<Attribute> row = new TableRow<>();

			final ContextMenu attributesContextMenu = new ContextMenu();

			final MenuItem attributesEnhanceItem = new MenuItem("Steigern");
			attributesContextMenu.getItems().add(attributesEnhanceItem);
			attributesEnhanceItem.setOnAction(o -> {
				final Attribute attribute = row.getItem();
				new AttributeEnhancementDialog(pane.getScene().getWindow(), attribute, hero, attribute.getValue() + 1);
			});

			final MenuItem attributesEditItem = new MenuItem("Bearbeiten");
			attributesContextMenu.getItems().add(attributesEditItem);
			attributesEditItem.setOnAction(o -> {
				final Attribute attribute = row.getItem();
				new AttributeEditor(pane.getScene().getWindow(), attribute);
			});

			final MenuItem rollItem = new MenuItem("Eigenschaftsprobe");
			attributesContextMenu.getItems().add(rollItem);
			rollItem.setOnAction(e -> {
				final Attribute item = row.getItem();
				new SingleRollDialog(pane.getScene().getWindow(), SingleRollDialog.Type.ATTRIBUTE, hero, item);
			});

			row.setContextMenu(attributesContextMenu);

			return row;
		});

		derivedValuesTable.prefWidthProperty().bind(pane.widthProperty().subtract(22).divide(2));

		GUIUtil.autosizeTable(derivedValuesTable);
		GUIUtil.cellValueFactories(derivedValuesTable, "name", "manualModifier", "current");
		derivedModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		derivedModifierColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setManualModifier(t.getNewValue());
			}
		});
		derivedCurrentColumn.setCellValueFactory(new PropertyValueFactory<DerivedValue, Integer>("current"));

		derivedValuesTable.setRowFactory(t -> {
			final TableRow<DerivedValue> row = new TableRow<>();

			final ContextMenu derivedContextMenu = new ContextMenu();
			final MenuItem derivedContextMenuItem = new MenuItem("Bearbeiten");
			derivedContextMenu.getItems().add(derivedContextMenuItem);
			derivedContextMenuItem.setOnAction(o -> {
				final DerivedValue value = row.getItem();
				new DerivedValueEditor(pane.getScene().getWindow(), value, false);
			});

			row.setContextMenu(derivedContextMenu);

			return row;
		});

		energiesTable.prefWidthProperty().bind(pane.widthProperty().subtract(17));

		GUIUtil.autosizeTable(energiesTable);
		GUIUtil.cellValueFactories(energiesTable, "name", "permanent", "bought", "manualModifier", "currentPercentage");
		energiesPermanentColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		energiesPermanentColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setPermanent(t.getNewValue());
			}
		});
		energiesBoughtColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(0, 99));
		energiesBoughtColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null)
				if (HeroTabController.isEditable.get()) {
					t.getRowValue().setBought(t.getNewValue());
				} else {
					final HeroEnergy energy = t.getRowValue();
					new EnergyEnhancementDialog(pane.getScene().getWindow(), energy, hero, energy.getMax() - energy.getBought() + t.getNewValue());
				}
		});
		energiesModifierColumn.setCellFactory(o -> new IntegerSpinnerTableCell<>(-99, 99));
		energiesModifierColumn.setOnEditCommit(t -> {
			if (t.getRowValue() != null) {
				t.getRowValue().setManualModifier(t.getNewValue());
			}
		});
		energiesCurrentColumn.setCellFactory(o -> new ColoredProgressBarTableCell<>());

		energiesTable.setRowFactory(t -> {
			final TableRow<HeroEnergy> row = new TableRow<>();

			final ContextMenu energiesContextMenu = new ContextMenu();
			final MenuItem energiesEnhanceItem = new MenuItem("Zukaufen");
			energiesContextMenu.getItems().add(energiesEnhanceItem);
			energiesEnhanceItem.setOnAction(o -> {
				final HeroEnergy value = row.getItem();
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
				final DerivedValue value = row.getItem();
				new DerivedValueEditor(pane.getScene().getWindow(), value, !"Karmaenergie".equals(value.getName()));
			});
			row.setContextMenu(energiesContextMenu);

			return row;
		});

		registerUIListeners();

		name.editableProperty().bind(HeroTabController.isEditable);
		surname.editableProperty().bind(HeroTabController.isEditable);
		player.editableProperty().bind(HeroTabController.isEditable);
		race.editableProperty().bind(HeroTabController.isEditable);
		culture.editableProperty().bind(HeroTabController.isEditable);
		profession.editableProperty().bind(HeroTabController.isEditable);
		freeAp.disableProperty().bind(HeroTabController.isEditable.not());
		socialstate.disableProperty().bind(HeroTabController.isEditable.not());
		birthday.disableProperty().bind(HeroTabController.isEditable.not());
		birthmonth.disableProperty().bind(HeroTabController.isEditable.not());
		birthyear.disableProperty().bind(HeroTabController.isEditable.not());
		gender.disableProperty().bind(HeroTabController.isEditable.not());
		size.disableProperty().bind(HeroTabController.isEditable.not());
		weight.disableProperty().bind(HeroTabController.isEditable.not());
		eyecolor.disableProperty().bind(HeroTabController.isEditable.not());
		haircolor.disableProperty().bind(HeroTabController.isEditable.not());
		skincolor.disableProperty().bind(HeroTabController.isEditable.not());
		energiesPermanentColumn.editableProperty().bind(HeroTabController.isEditable);
	}

	@Override
	protected void registerListeners() {
		hero.getObj("Biografie").addListener(heroBioListener);
	}

	private void registerUIListeners() {
		name.focusedProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Vorname", name.getText());
			ResourceManager.moveResource(getHero(), "characters/" + name.getText());
			bio.notifyListeners(heroBioListener);
		}));
		surname.focusedProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Nachname", surname.getText());
			bio.notifyListeners(heroBioListener);
		}));
		player.focusedProperty().addListener(Util.changeListener(() -> update, newV -> {
			hero.put("Spieler", player.getText());
			hero.notifyListeners(heroBioListener);
		}));

		socialstate.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject so = hero.getObj("Basiswerte").getObj("Sozialstatus");
			so.put("Wert", socialstate.getValue());
			so.notifyListeners(heroBioListener);
		}));

		ap.valueProperty().addListener((o, oldV, newV) -> {
			freeAp.getValueFactory().setValue(freeAp.getValue() + newV - oldV);
			if (!update && !oldV.equals(newV)) {
				final JSONArray history = getHero().getArr("Historie");
				final JSONObject lastEntry = history.size() == 0 ? null : history.getObj(history.size() - 1);
				final LocalDate currentDate = LocalDate.now();
				if (lastEntry != null && "Abenteuerpunkte".equals(lastEntry.getString("Typ"))
						&& currentDate.equals(LocalDate.parse(lastEntry.getString("Datum")))) {
					if (lastEntry.getInt("Von").equals(newV)) {
						history.removeAt(history.size() - 1);
					} else {
						lastEntry.put("Auf", newV);
					}
				} else {
					final JSONObject historyEntry = new JSONObject(history);
					historyEntry.put("Typ", "Abenteuerpunkte");
					historyEntry.put("Von", oldV);
					historyEntry.put("Auf", newV);
					historyEntry.put("Datum", currentDate.toString());
					history.add(historyEntry);
				}
				final JSONObject bio = hero.getObj("Biografie");
				bio.put("Abenteuerpunkte", ap.getValue());
				bio.notifyListeners(heroBioListener);
			}
		});
		freeAp.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", freeAp.getValue());
			bio.notifyListeners(heroBioListener);
			investedAp.setText(Integer.toString(ap.getValue() - freeAp.getValue()));
		}));

		race.focusedProperty().addListener(Util.changeListener(() -> update, newV -> changeModifiedString("Rasse", race.getText())));
		culture.focusedProperty().addListener(Util.changeListener(() -> update, newV -> changeModifiedString("Kultur", culture.getText())));
		profession.focusedProperty().addListener(Util.changeListener(() -> update, newV -> changeModifiedProfession(profession.getText())));

		birthday.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Geburtstag", birthday.getValue());
			bio.notifyListeners(heroBioListener);
			setAge(ResourceManager.getResource("data/Allgemein").getObj("Zeit"));
		}));
		birthmonth.getSelectionModel().selectedIndexProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Geburtsmonat", birthmonth.getSelectionModel().getSelectedIndex() + 1);
			bio.notifyListeners(heroBioListener);
			setAge(ResourceManager.getResource("data/Allgemein").getObj("Zeit"));
		}));
		birthyear.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Geburtsjahr", birthyear.getValue());
			bio.notifyListeners(heroBioListener);
			setAge(ResourceManager.getResource("data/Allgemein").getObj("Zeit"));
		}));
		gender.getSelectionModel().selectedIndexProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Geschlecht", gender.getSelectionModel().getSelectedItem());
			bio.notifyListeners(heroBioListener);
		}));
		size.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Größe", size.getValue());
			bio.notifyListeners(heroBioListener);
		}));
		weight.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Gewicht", weight.getValue());
			bio.notifyListeners(heroBioListener);
		}));
		eyecolor.valueProperty().addListener(Util.changeListener(() -> update, newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Augenfarbe", eyecolor.getValue());
			bio.notifyListeners(heroBioListener);
		}));

		haircolor.valueProperty().addListener(Util.changeListener(() -> update || "".equals(haircolor.getValue()), newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put(bio.containsKey("Schuppenfarbe 1") ? "Schuppenfarbe 1" : "Haarfarbe", haircolor.getValue());
			bio.notifyListeners(heroBioListener);
		}));
		skincolor.valueProperty().addListener(Util.changeListener(() -> update || "".equals(skincolor.getValue()), newV -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put(bio.containsKey("Schuppenfarbe 2") ? "Schuppenfarbe 2" : "Hautfarbe", skincolor.getValue());
			bio.notifyListeners(heroBioListener);
		}));
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
		update = true;

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

		update = false;
	}

	private void setAttributesAndDerivedValues() {
		update = true;

		attributesTable.getItems().clear();
		derivedValuesTable.getItems().clear();
		energiesTable.getItems().clear();

		final JSONObject derivedValues = ResourceManager.getResource("data/Basiswerte");
		final JSONObject attributes = ResourceManager.getResource("data/Eigenschaften");
		final JSONObject actualAttributes = hero.getObj("Eigenschaften");

		for (final String attribute : attributes.keySet()) {
			attributesTable.getItems().add(new Attribute(attribute, actualAttributes.getObj(attribute)));
		}

		for (final String derivedValue : new String[] { "Attacke-Basis", "Parade-Basis", "Fernkampf-Basis", "Initiative-Basis", "Artefaktkontrolle",
				"Wundschwelle", "Geschwindigkeit" }) {
			derivedValuesTable.getItems().add(new DerivedValue(derivedValue, derivedValues.getObj(derivedValue), hero));
		}

		energiesTable.getItems().add(new HeroEnergy("Lebensenergie", derivedValues.getObj("Lebensenergie"), hero, dsa41basis.hero.Energy.COLOR_LEP));
		energiesTable.getItems().add(new HeroEnergy("Ausdauer", derivedValues.getObj("Ausdauer"), hero, dsa41basis.hero.Energy.COLOR_AUP));
		if (HeroUtil.isMagical(hero)) {
			energiesTable.getItems()
					.add(new HeroEnergy("Astralenergie", derivedValues.getObj("Astralenergie"), hero, dsa41basis.hero.Energy.COLOR_ASP));
		}
		if (HeroUtil.isClerical(hero, false)) {
			energiesTable.getItems().add(new HeroEnergy("Karmaenergie", new JSONObject(null), hero, dsa41basis.hero.Energy.COLOR_KAP));
		}
		energiesTable.getItems().add(new HeroEnergy("Magieresistenz", derivedValues.getObj("Magieresistenz"), hero, dsa41basis.hero.Energy.COLOR_MR));

		update = false;
	}

	private void setBiography() {
		update = true;

		final JSONObject bio = hero.getObj("Biografie");

		name.setText(bio.getStringOrDefault("Vorname", ""));
		surname.setText(bio.getStringOrDefault("Nachname", ""));
		player.setText(hero.getStringOrDefault("Spieler", ""));

		socialstate.getValueFactory().setValue(hero.getObj("Basiswerte").getObj("Sozialstatus").getIntOrDefault("Wert", 0));
		final int apTotal = bio.getIntOrDefault("Abenteuerpunkte", 0);
		final int apFree = bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0);
		ap.getValueFactory().setValue(apTotal);
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

		final JSONObject professions = ResourceManager.getResource("data/Professionen");

		profession.setText(HeroUtil.getProfessionString(hero, bio, professions, false));

		professionModifier.setText(HeroUtil.getVeteranBGBString(hero, bio, professions).toString());

		update = false;
	}

	@Override
	protected void unregisterListeners() {
		hero.getObj("Biografie").removeListener(heroBioListener);
		attributesTable.getItems().clear();
		derivedValuesTable.getItems().clear();
		energiesTable.getItems().clear();
	}

	@Override
	protected void update() {
		if (hero != null) {
			setBiography();
			setAppearance();
			setAttributesAndDerivedValues();
		}
	}
}
