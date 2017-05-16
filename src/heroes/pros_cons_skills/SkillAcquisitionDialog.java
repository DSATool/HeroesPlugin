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

import dsa41basis.hero.ProOrCon;
import dsa41basis.hero.ProOrCon.ChoiceOrTextEnum;
import dsa41basis.util.HeroUtil;
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
	private ReactiveSpinner<Integer> cost;

	public SkillAcquisitionDialog(final Window window, ProOrCon actualSkill, JSONObject hero) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("SkillAcquisitionDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject skill = actualSkill.getProOrCon();
		final JSONObject actual = actualSkill.getActual();
		final String name = actualSkill.getName();
		final boolean hasChoice = skill.containsKey("Auswahl");
		final boolean hasText = skill.containsKey("Freitext");

		final Stage stage = new Stage();
		stage.setTitle("Sonderfertigkeit erwerben");
		stage.setScene(new Scene(root, 300, 130 - (hasChoice ? 0 : 27) - (hasText ? 0 : 27)));
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
			cost.getValueFactory().setValue(actualSkill.getCost());
		});

		variant.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
			actualSkill.setVariant(newV);
			cost.getValueFactory().setValue(actualSkill.getCost());
		});

		okButton.setOnAction(event -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - cost.getValue());
			final JSONObject skills = hero.getObj("Sonderfertigkeiten");
			final JSONObject cheaperSkills = hero.getObj("Verbilligte Sonderfertigkeiten");
			if (hasChoice || hasText) {
				final JSONArray choices = skills.getArr(name);
				final JSONObject newSkill = actual.clone(choices);
				newSkill.put("Kosten", cost.getValue());
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
				final JSONObject newSkill = actual.clone(skills);
				newSkill.put("Kosten", cost.getValue());
				skills.put(name, newSkill);
				skills.notifyListeners(null);
				if (cheaperSkills.containsKey(name)) {
					cheaperSkills.removeKey(name);
					cheaperSkills.notifyListeners(null);
				}
			}
			HeroUtil.applyEffect(hero, name, skill, actual);
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
		cost.getValueFactory().setValue(actualSkill.getCost());

		stage.show();
	}
}
