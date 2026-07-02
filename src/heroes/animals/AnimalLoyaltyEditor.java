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

import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveComboBox;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class AnimalLoyaltyEditor {
	@FXML
	private VBox root;
	@FXML
	private VBox animalBox;
	@FXML
	private TextField name;
	@FXML
	private ReactiveComboBox<String> species;
	@FXML
	private TextField race;
	@FXML
	private Button okButton;
	@FXML
	private HBox modifierBox;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private ReactiveSpinner<Integer> start;
	@FXML
	private ReactiveSpinner<Integer> training;
	@FXML
	private ReactiveSpinner<Integer> free;
	@FXML
	private ReactiveSpinner<Integer> education;
	@FXML
	private ReactiveSpinner<Integer> max;
	@FXML
	private Button cancelButton;

	public AnimalLoyaltyEditor(final Window window, final JSONObject animal, final boolean creation) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("AnimalLoyaltyEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 210, creation ? 295 : 240, creation ? "Tier erstellen" : "Bearbeiten", window, true);

		final JSONObject animalLoyalties = ResourceManager.getResource("data/Tierloyalitaet");

		final JSONObject biography = animal.getObj("Biografie");
		final JSONObject loyalty = animal.getObj("Basiswerte").getObj("Loyalität");

		if (creation) {
			modifierBox.setVisible(false);
			modifierBox.setManaged(false);
			name.setText(biography.getString("Name"));
			species.getItems().setAll(animalLoyalties.keySet());
			species.valueProperty().addListener((_, _, newV) -> {
				if (animalLoyalties.containsKey(newV)) {
					updateLoyalties(animalLoyalties.getObj(newV).getObj("Loyalität"));
				}
			});
			species.getSelectionModel().select(0);
		} else {
			animalBox.setVisible(false);
			animalBox.setManaged(false);
			modifier.getValueFactory().setValue(loyalty.getIntOrDefault("Modifikator", 0));
			updateLoyalties(loyalty);
		}

		okButton.setOnAction(_ -> {
			if (creation) {
				biography.put("Name", name.getText());
				biography.put("Tierart", species.getValue());
				if (!race.getText().isBlank()) {
					biography.put("Rasse", race.getText());
				}
				loyalty.put("Wert", start.getValue());
			} else {
				loyalty.put("Modifikator", modifier.getValue());
			}
			loyalty.put("Start", start.getValue());
			loyalty.put("Erziehung", training.getValue());
			loyalty.put("Frei", free.getValue());
			loyalty.put("Schulung", education.getValue());
			loyalty.put("Maximum", max.getValue());
			loyalty.notifyListeners(null);
			stage.close();
		});

		cancelButton.setOnAction(_ -> stage.close());

		if (creation) {
			stage.showAndWait();
		} else {
			stage.show();
		}
	}

	private void updateLoyalties(final JSONObject loyalty) {
		start.getValueFactory().setValue(loyalty.getIntOrDefault("Start", 0));
		training.getValueFactory().setValue(loyalty.getIntOrDefault("Erziehung", 0));
		free.getValueFactory().setValue(loyalty.getIntOrDefault("Frei", 0));
		education.getValueFactory().setValue(loyalty.getIntOrDefault("Schulung", 0));
		max.getValueFactory().setValue(loyalty.getIntOrDefault("Maximum", 0));
	}
}
