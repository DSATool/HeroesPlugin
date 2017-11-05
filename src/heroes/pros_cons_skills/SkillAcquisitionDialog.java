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
package heroes.pros_cons_skills;

import java.time.LocalDate;

import dsa41basis.hero.ProOrCon;
import dsa41basis.hero.ProOrCon.ChoiceOrTextEnum;
import dsa41basis.util.HeroUtil;
import dsatool.resources.ResourceManager;
import dsatool.resources.Settings;
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class SkillAcquisitionDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label nameLabel;
	@FXML
	private Node descriptionBox;
	@FXML
	private ComboBox<String> description;
	@FXML
	private Node variantBox;
	@FXML
	private ComboBox<String> variant;
	@FXML
	private ReactiveSpinner<Integer> ap;
	@FXML
	private HBox costBox;
	@FXML
	private ReactiveSpinner<Double> cost;

	public SkillAcquisitionDialog(final Window window, final ProOrCon actualSkill, final JSONObject hero) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("SkillAcquisitionDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final boolean includeCost = Settings.getSettingBoolOrDefault(true, "Steigerung", "Lehrmeisterkosten");

		final JSONObject skill = actualSkill.getProOrCon();
		final JSONObject actual = actualSkill.getActual();
		final String name = actualSkill.getName();
		final boolean hasChoice = skill.containsKey("Auswahl");
		final boolean hasText = skill.containsKey("Freitext");

		if (!includeCost) {
			costBox.setVisible(false);
			costBox.setManaged(false);
		}

		final Stage stage = new Stage();
		stage.setTitle("Sonderfertigkeit erwerben");
		stage.setScene(new Scene(root, 300, 157 - (hasChoice ? 0 : 27) - (hasText ? 0 : 27) - (includeCost ? 0 : 27)));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		if (!hasChoice || !hasText) {
			variantBox.setManaged(false);
			variantBox.setVisible(false);
			if (!hasChoice && !hasText) {
				descriptionBox.setManaged(false);
				descriptionBox.setVisible(false);
			}
		}

		description.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
			actualSkill.setDescription(newV);
			variant.setItems(FXCollections.observableArrayList(actualSkill.getSecondChoiceItems(true)));
			variant.getSelectionModel().select(0);
			ap.getValueFactory().setValue(actualSkill.getCost());
		});

		variant.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
			actualSkill.setVariant(newV);
			ap.getValueFactory().setValue(actualSkill.getCost());
		});

		ap.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(actualSkill)));

		okButton.setOnAction(event -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - ap.getValue());
			final JSONObject skills = hero.getObj("Sonderfertigkeiten");
			final JSONObject cheaperSkills = hero.getObj("Verbilligte Sonderfertigkeiten");
			JSONObject newSkill;
			if (hasChoice || hasText) {
				final JSONArray choices = skills.getArr(name);
				newSkill = actual.clone(choices);
				newSkill.put("Kosten", ap.getValue());
				choices.add(newSkill);
				choices.notifyListeners(null);
				if (cheaperSkills.containsKey(name)) {
					final JSONArray actualArray = cheaperSkills.getArr(name);
					for (int i = 0; i < actualArray.size(); ++i) {
						final JSONObject current = actualArray.getObj(i);
						if (!hasChoice || !current.containsKey("Auswahl") || current.getString("Auswahl").equals(newSkill.getString("Auswahl"))) {
							if (!hasText || !current.containsKey("Freitext") || current.getString("Freitext").equals(newSkill.getString("Freitext"))) {
								actualArray.removeAt(i);
								actualArray.notifyListeners(null);
								break;
							}
						}
					}
				}
			} else {
				newSkill = actual.clone(skills);
				newSkill.put("Kosten", ap.getValue());
				skills.put(name, newSkill);
				skills.notifyListeners(null);
				if (cheaperSkills.containsKey(name)) {
					cheaperSkills.removeKey(name);
					cheaperSkills.notifyListeners(null);
				}
			}
			HeroUtil.applyEffect(hero, name, skill, actual);

			final JSONArray history = hero.getArr("Historie");
			final JSONObject historyEntry = new JSONObject(history);
			historyEntry.put("Typ", "Sonderfertigkeit");
			historyEntry.put("Sonderfertigkeit", actualSkill.getName());
			if (skill.containsKey("Auswahl")) {
				historyEntry.put("Auswahl", newSkill.getString("Auswahl"));
			}
			if (skill.containsKey("Freitext")) {
				historyEntry.put("Freitext", newSkill.getString("Freitext"));
			}
			historyEntry.put("AP", ap.getValue());

			if (includeCost && cost.getValue() != 0) {
				historyEntry.put("Kosten", cost.getValue());
				HeroUtil.addMoney(hero, (int) (cost.getValue() * -100));
			}

			final LocalDate currentDate = LocalDate.now();
			historyEntry.put("Datum", currentDate.toString());
			history.add(historyEntry);
			history.notifyListeners(null);

			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		nameLabel.setText(actualSkill.getName());
		description.setEditable(actualSkill.firstChoiceOrText() == ChoiceOrTextEnum.TEXT);
		description.setItems(FXCollections.observableArrayList(actualSkill.getFirstChoiceItems(true)));
		description.getSelectionModel().select(actualSkill.getDescription());
		if (description.getSelectionModel().getSelectedIndex() < 0) {
			description.getSelectionModel().select(0);
		}
		variant.setEditable(actualSkill.secondChoiceOrText() == ChoiceOrTextEnum.TEXT);
		variant.setItems(FXCollections.observableArrayList(actualSkill.getSecondChoiceItems(true)));
		variant.getSelectionModel().select(actualSkill.getVariant());
		if (variant.getSelectionModel().getSelectedIndex() < 0) {
			variant.getSelectionModel().select(0);
		}
		ap.getValueFactory().setValue(actualSkill.getCost());

		stage.show();
	}

	private double getCalculatedCost(final ProOrCon skill) {
		if (!Settings.getSettingBoolOrDefault(true, "Steigerung", "Lehrmeisterkosten")) return 0;

		final JSONObject group = (JSONObject) skill.getProOrCon().getParent();
		if (group == ResourceManager.getResource("data/Sonderfertigkeiten").getObj("Magische Sonderfertigkeiten") ||
				group == ResourceManager.getResource("data/Rituale") || group == ResourceManager.getResource("data/Schamanenrituale"))
			return ap.getValue() * 5;
		return ap.getValue() * 0.7;
	}
}
