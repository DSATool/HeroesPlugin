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

import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.ui.ReactiveComboBox;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import heroes.talents.TalentRollDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;
import jsonant.value.JSONValue;

public class AnimalSkillTrainingDialog {

	@FXML
	private VBox root;
	@FXML
	private Label nameLabel;
	@FXML
	private HBox descriptionBox;
	@FXML
	private ReactiveComboBox<String> description;
	@FXML
	private HBox variantBox;
	@FXML
	private TextField variant;
	@FXML
	private CheckBox wild;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private Button completeButton;
	@FXML
	private Button cancelButton;

	private final JSONObject animal;

	public AnimalSkillTrainingDialog(final Window window, final JSONObject animal, final String skillName, final JSONObject skill) {
		this.animal = animal;

		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("AnimalSkillTrainingDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final boolean needsDescription = skill.containsKey("Auswahl");
		final boolean needsVariant = skill.containsKey("Freitext");

		final Stage stage = GUIUtil.setupStage(root, 260, 175 + (needsDescription ? 28 : 0) + (needsVariant ? 28 : 0), "Fertigkeit beibringen", window, true);

		nameLabel.setText(skillName);

		if (needsDescription) {
			description.getItems().addAll(HeroUtil.getChoices(animal, skill.getString("Auswahl"), null));
			description.getSelectionModel().select(0);
		} else {
			descriptionBox.setVisible(false);
			descriptionBox.setManaged(false);
		}

		if (needsVariant) {
			variant.setText(skill.getString("Freitext"));
		} else {
			variantBox.setVisible(false);
			variantBox.setManaged(false);
		}

		final int taw = (int) HeroUtil.getTaW((JSONObject) animal.getParent().getParent(), "Abrichten", null);
		final int missingTalentPenalty = taw == Integer.MIN_VALUE ? 7 : Math.max(7 - taw, 0);

		modifier.getValueFactory().setValue(skill.getIntOrDefault("Erschwernis", 0));

		wild.selectedProperty().addListener((_, _, newV) -> modifier.getValueFactory()
				.setValue(modifier.getValueFactory().getValue() + (newV ? missingTalentPenalty : -missingTalentPenalty)));

		wild.setSelected(animal.getObj("Basiswerte").getObj("Loyalität").getIntOrDefault("Erziehung", 0) != 0);

		completeButton.setOnAction(_ -> {
			final JSONObject actualSkills = animal.getObj("Fertigkeiten");
			if (needsDescription || needsVariant) {
				final JSONArray actual = actualSkills.getArr(skillName);
				actual.add(createSkill(actual, needsDescription, needsVariant));
			} else {
				actualSkills.put(skillName, createSkill(actualSkills, false, false));
			}
			actualSkills.notifyListeners(null);
			stage.close();
		});
		completeButton.requestFocus();

		cancelButton.setOnAction(_ -> stage.close());

		stage.show();
	}

	private JSONObject createSkill(final JSONValue parent, final boolean needsDescription, final boolean needsVariant) {
		final JSONObject newSkill = new JSONObject(parent);
		if (needsDescription) {
			newSkill.put("Auswahl", description.getSelectionModel().getSelectedItem());
		}
		if (needsVariant) {
			newSkill.put("Freitext", variant.getText());
		}
		return newSkill;
	}

	@FXML
	private void talentRoll() {
		final JSONObject hero = (JSONObject) animal.getParent().getParent();
		new TalentRollDialog(root.getScene().getWindow(), "Abrichten", null, new JSONObject[] { hero }, new Integer[1], modifier.getValue(), null);
	}
}
