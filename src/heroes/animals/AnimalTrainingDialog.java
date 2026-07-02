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

import java.util.Collection;
import java.util.Optional;

import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.gui.ThemedAlert;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveComboBox;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import heroes.talents.TalentRollDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class AnimalTrainingDialog {

	@FXML
	private VBox root;
	@FXML
	private ReactiveComboBox<String> education;
	@FXML
	private CheckBox wild;
	@FXML
	private ReactiveComboBox<String> specialization;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private ReactiveSpinner<Integer> requiredTaP;
	@FXML
	private Label currentTaPLabel;
	@FXML
	private ReactiveSpinner<Integer> newTaP;
	@FXML
	private Button completeEducationButton;
	@FXML
	private Button continueEducationButton;
	@FXML
	private Button cancelButton;

	private final JSONObject animal;
	private final JSONObject progress;

	public AnimalTrainingDialog(final Window window, final JSONObject animal, final int additionalTaP) {
		this.animal = animal;

		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("AnimalTrainingDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 260, 300, "Tierausbildung", window, true);

		final int taw = (int) HeroUtil.getTaW((JSONObject) animal.getParent().getParent(), "Abrichten", null);
		final int missingTalentPenalty = taw == Integer.MIN_VALUE ? 7 : Math.max(7 - taw, 0);

		final JSONObject biography = animal.getObj("Biografie");

		final JSONObject basicValues = animal.getObj("Basiswerte");
		progress = basicValues.getObjOrDefault("Ausbildung", new JSONObject(basicValues));

		final JSONObject educations = ResourceManager.getResource("data/Tierausbildungen").getObj("Allgemein");

		final JSONObject loyalties = ResourceManager.getResource("data/Tierloyalitaet");

		String species = biography.getString("Tierart");
		final String modifiedspecies = species + " (" + biography.getStringOrDefault("Geschlecht", "weiblich") + ")";
		if (!loyalties.containsKey(species) && loyalties.containsKey(modifiedspecies)) {
			species = modifiedspecies;
		}

		final Collection<String> validEducations = loyalties.containsKey(species)
				? loyalties.getObj(species).getArrOrDefault("Ausbildungen", new JSONArray(null)).getStrings() : educations.keySet();

		wild.setSelected(basicValues.getObj("Loyalität").getIntOrDefault("Erziehung", 0) != 0);

		wild.selectedProperty().addListener((_, _, newV) -> modifier.getValueFactory()
				.setValue(modifier.getValueFactory().getValue() + (newV ? missingTalentPenalty : -missingTalentPenalty)));

		specialization.getItems().add("Keine");
		final JSONArray specializations = HeroUtil.findTalent("Abrichten")._1.getArr("Spezialisierungen");
		if (specializations != null) {
			specialization.getItems().addAll(specializations.getStrings());
		}
		specialization.getSelectionModel().select(0);

		education.setCellFactory(_ -> new ComboBoxListCell<String>() {
			@Override
			public void updateItem(final String item, final boolean empty) {
				super.updateItem(item, empty);
				if (validEducations.contains(item)) {
					getStyleClass().remove("invalid");
				} else {
					getStyleClass().add("invalid");
				}
			}
		});

		education.getSelectionModel().selectedItemProperty().addListener((_, _, newV) -> {
			final JSONObject newEducation = educations.getObj(newV);
			modifier.getValueFactory().setValue(newEducation.getIntOrDefault("Erschwernis", 0) + (wild.isSelected() ? missingTalentPenalty : 0));
			requiredTaP.getValueFactory().setValue(newEducation.getIntOrDefault("TaP*", 1));
			progress.put("Ausbildung", newV);
			if (validEducations.contains(newV)) {
				education.getStyleClass().remove("invalid");
			} else {
				education.getStyleClass().add("invalid");
			}
		});
		education.getItems().setAll(educations.keySet());
		education.getSelectionModel().select(progress.containsKey("Ausbildung") ? progress.getString("Ausbildung") : educations.keySet().iterator().next());

		modifier.valueProperty().addListener((_, _, newV) -> {
			progress.put("Erschwernis", newV);
		});
		if (progress.containsKey("Erschwernis")) {
			modifier.getValueFactory().setValue(progress.getInt("Erschwernis"));
		}

		final int currentTaP = progress.getIntOrDefault("TaP*", 0);
		((IntegerSpinnerValueFactory) newTaP.getValueFactory()).setMin(-currentTaP);
		currentTaPLabel.setText(Integer.toString(currentTaP));

		requiredTaP.valueProperty().addListener((_, _, newV) -> {
			progress.put("benötigt", newV);
			updateCompletion(currentTaP);
		});
		if (progress.containsKey("benötigt")) {
			requiredTaP.getValueFactory().setValue(progress.getInt("benötigt"));
		}

		newTaP.valueProperty().addListener((_, _, _) -> updateCompletion(currentTaP));

		newTaP.getValueFactory().setValue(additionalTaP);

		updateCompletion(currentTaP);

		completeEducationButton.setOnAction(_ -> {
			final int tapSum = currentTaP + newTaP.getValue();
			if (tapSum >= requiredTaP.getValue() ||
					showAlert("Benötigte TaP* nicht erreicht", "Die für die gewählte Ausbildungsvariante benötigten TaP* sind noch nicht erreicht.",
							"Soll die Ausbildung trotzdem abgeschlossen werden?")) {
				final String educationName = education.getValue();
				final JSONObject chosenEducation = educations.getObj(educationName);
				biography.getArr("Ausbildung").add(educationName);
				HeroUtil.applyEffect(animal, educationName, chosenEducation, new JSONObject(null), true);
				basicValues.removeKey("Ausbildung");
				biography.notifyListeners(null);
				basicValues.notifyListeners(null);
				stage.close();
			}
		});

		continueEducationButton.setOnAction(_ -> {
			final int tapSum = currentTaP + newTaP.getValue();
			if (tapSum < requiredTaP.getValue() ||
					showAlert("Benötigte TaP* bereits erreicht", "Die für die gewählte Ausbildungsvariante benötigten TaP* sind bereits erreicht.",
							"Soll die Ausbildung trotzdem fortgesetzt werden?")) {
				progress.put("TaP*", tapSum);
				basicValues.put("Ausbildung", progress);
				basicValues.notifyListeners(null);
				stage.close();
			}
		});

		cancelButton.setOnAction(_ -> stage.close());

		stage.show();
	}

	private boolean showAlert(final String title, final String explanation, final String question) {
		final Alert alert = new ThemedAlert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(explanation);
		alert.setContentText(question);
		alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		final Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get().equals(ButtonType.OK);
	}

	@FXML
	private void talentRoll() {
		final Integer[] resultArray = new Integer[1];
		final String selectedSpecialization = specialization.getSelectionModel().getSelectedItem();
		final JSONObject hero = (JSONObject) animal.getParent().getParent();
		new TalentRollDialog(root.getScene().getWindow(), "Abrichten", null, new JSONObject[] { hero }, resultArray, modifier.getValue(),
				selectedSpecialization);
		final Integer result = resultArray[0];
		final int newTaPValue = switch (result) {
			case null -> 0;
			case Integer.MIN_VALUE -> -progress.getIntOrDefault("TaP*", 0);
			case Integer.MIN_VALUE + 1 -> -(DSAUtil.diceRoll(6) + DSAUtil.diceRoll(6));
			case Integer.MAX_VALUE - 1 -> (int) HeroUtil.getTaW(hero, "Abrichten", "Keine".equals(selectedSpecialization) ? null : selectedSpecialization);
			case Integer.MAX_VALUE -> requiredTaP.getValue() - progress.getIntOrDefault("TaP*", 0);
			default -> result;
		};
		newTaP.getValueFactory().setValue(newTaPValue);
	}

	private void updateCompletion(final int currentTaP) {
		final int totalTaP = currentTaP + newTaP.getValue();
		completeEducationButton.getStyleClass().removeAll("valid", "invalid");
		continueEducationButton.getStyleClass().removeAll("valid", "invalid");
		if (totalTaP >= requiredTaP.getValue()) {
			completeEducationButton.getStyleClass().add("valid");
			continueEducationButton.getStyleClass().add("invalid");
		} else {
			completeEducationButton.getStyleClass().add("invalid");
			continueEducationButton.getStyleClass().add("valid");
		}
	}
}
