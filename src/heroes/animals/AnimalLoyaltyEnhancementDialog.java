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

import dsa41basis.hero.Attribute;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.gui.ThemedAlert;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class AnimalLoyaltyEnhancementDialog {

	@FXML
	private VBox root;
	@FXML
	private CheckBox wild;
	@FXML
	private ReactiveComboBox<String> breaking;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private Label currentLoyaltyLabel;
	@FXML
	private ReactiveSpinner<Integer> finalLoyalty;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;

	private final JSONObject animal;
	private final Attribute loyalty;

	public AnimalLoyaltyEnhancementDialog(final Window window, final Attribute loyalty, final JSONObject animal) {
		this.animal = animal;
		this.loyalty = loyalty;

		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("AnimalLoyaltyEnhancementDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 260, 227, "Loyalität erhöhen", window, true);

		final int taw = (int) HeroUtil.getTaW((JSONObject) animal.getParent().getParent(), "Abrichten", null);
		final int missingTalentPenalty = taw == Integer.MIN_VALUE ? 7 : Math.max(7 - taw, 0);

		final int trainingLO = animal.getObj("Basiswerte").getObj("Loyalität").getIntOrDefault("Erziehung", 0);
		final boolean isWild = trainingLO != 0;
		final boolean canBeTrained = loyalty.getValue() >= trainingLO;

		final int targetLoyalty = loyalty.getValue() + 1;

		breaking.getItems().setAll("Erziehung", "Dressur");
		if (isWild && !canBeTrained) {
			breaking.getSelectionModel().select("Dressur");
		} else {
			breaking.getSelectionModel().select("Erziehung");
		}

		modifier.getValueFactory().setValue(targetLoyalty > 0 ? (targetLoyalty + 1) / 2 : -targetLoyalty + 1);

		wild.selectedProperty().addListener((_, _, newV) -> {
			modifier.getValueFactory().setValue(modifier.getValueFactory().getValue() + (newV ? missingTalentPenalty : -missingTalentPenalty));
			if (newV) {
				breaking.setDisable(false);
				breaking.getSelectionModel().select(canBeTrained ? "Erziehung" : "Dressur");
			} else {
				breaking.setDisable(true);
				breaking.getSelectionModel().select("Erziehung");
			}
		});

		wild.setSelected(isWild);

		currentLoyaltyLabel.setText(Integer.toString(loyalty.getValue()));

		final IntegerSpinnerValueFactory loyaltyValueFactory = (IntegerSpinnerValueFactory) finalLoyalty.getValueFactory();
		loyaltyValueFactory.setMin(loyalty.getValue() - 7);
		loyaltyValueFactory.setMax(targetLoyalty);
		loyaltyValueFactory.setValue(targetLoyalty);

		final boolean isBreaking = "Dressur".equals(breaking.getSelectionModel().getSelectedItem());

		okButton.setOnAction(_ -> {
			final int loyaltyChange = finalLoyalty.getValue() - loyalty.getValue();
			final Alert alert = new ThemedAlert(AlertType.INFORMATION);
			switch (loyaltyChange) {
				case 1:
					alert.setTitle("Abrichten-Probe erfolgreich");
					alert.setHeaderText("Die Abrichten-Probe war erfolgreich.");
					alert.setContentText("Die Loyalität steigt um 1.");
					break;
				case 0:
					alert.setTitle("Abrichten-Probe gescheitert");
					alert.setHeaderText("Die Abrichten-Probe ist gescheitert.");
					alert.setContentText(
							"Die Loyalität ändert sich nicht. Im nächsten Monat sind Abrichten-Proben zur Steigerung der Loyalität um " + (isBreaking ? 7 : 5)
									+ " Punkte erschwert.");
					break;
				case -1:
					alert.setTitle("Abrichten-Probe gescheitert");
					alert.setHeaderText("Die Abrichten-Probe ist um 10 oder mehr Punkte gescheitert.");
					alert.setContentText("Die Loyalität sinkt um 1. Im nächsten halben Jahr sind Abrichten-Proben zur Steigerung der Loyalität um "
							+ (isBreaking ? 7 : 5) + " Punkte erschwert.");
					if (isBreaking) {
						alert.setContentText(alert.getContentText()
								+ " Misslingt eine CH-Probe, wendet sich das Tier gegen den Abrichter und eine weitere eine weitere Dressur durch diesen Abrichter ist nicht mehr möglich.");
					}
					break;
				default:
					alert.setTitle("Patzer bei Abrichten-Probe");
					alert.setHeaderText("Es gab einen Patzer bei der Abrichten-Probe.");
					alert.setContentText("Die Loyalität sinkt um " + -loyaltyChange + " (W6+1). ");
					if (isBreaking) {
						alert.setContentText(alert.getContentText()
								+ "Misslingt eine CH-Probe, wendet sich das Tier gegen den Abrichter und eine weitere eine weitere Dressur durch diesen Abrichter ist nicht mehr möglich.");
					} else {
						alert.setContentText(alert.getContentText() + (finalLoyalty.getValue() < 0
								? "Da die Loyalität unter 0 fällt, ist eine weitere Erziehung durch diesen Abrichter nicht mehr möglich."
								: "Im nächsten halben Jahr sind Abrichten-Proben zur Steigerung der Loyalität um 5 Punkte erschwert."));
					}
					break;
			}
			alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
			final Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get().equals(ButtonType.OK)) {
				loyalty.setValue(finalLoyalty.getValue());
				stage.close();
			}
		});
		okButton.requestFocus();

		cancelButton.setOnAction(_ -> stage.close());

		stage.show();
	}

	@FXML
	private void talentRoll() {
		final Integer[] resultArray = new Integer[1];
		final JSONObject hero = (JSONObject) animal.getParent().getParent();
		new TalentRollDialog(root.getScene().getWindow(), "Abrichten", null, new JSONObject[] { hero }, resultArray, modifier.getValue(), null);
		final Integer result = resultArray[0];
		int newLoyalty;
		if (result != null && result <= Integer.MIN_VALUE + 1) {
			newLoyalty = loyalty.getValue() - 1 - DSAUtil.diceRoll(6);
		} else if (result != null && result <= -10) {
			newLoyalty = loyalty.getValue() - 1;
		} else if (result == null || result < 0) {
			newLoyalty = loyalty.getValue();
		} else {
			newLoyalty = loyalty.getValue() + 1;
		}
		finalLoyalty.getValueFactory().setValue(newLoyalty);
	}
}
