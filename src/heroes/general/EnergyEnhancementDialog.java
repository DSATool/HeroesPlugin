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

import dsa41basis.util.DSAUtil;
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class EnergyEnhancementDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label nameLabel;
	@FXML
	private Label enhanceLabel;
	@FXML
	private Label startLabel;
	@FXML
	private Label maxLabel;
	@FXML
	private ReactiveSpinner<Integer> target;
	@FXML
	private ReactiveSpinner<Integer> ses;
	@FXML
	private ReactiveSpinner<Integer> cost;

	public EnergyEnhancementDialog(final Window window, final Energy energy, final JSONObject hero, final int initialTarget) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("EnhancementDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Zukauf");
		stage.setScene(new Scene(root, 200, 170));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		enhanceLabel.setText(" zukaufen:");

		target.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(energy, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(energy, hero)));

		okButton.setOnAction(event -> {
			final int usedSes = Math.min(ses.getValue(), target.getValue() - energy.getMax());
			final JSONArray history = hero.getArr("Steigerungshistorie");
			final JSONObject historyEntry = new JSONObject(history);
			historyEntry.put("Typ", "Basiswert");
			historyEntry.put("Basiswert", energy.getName());
			historyEntry.put("Von", energy.getBought());
			historyEntry.put("Auf", target.getValue() - energy.getMax() + energy.getBought());
			if (usedSes > 0) {
				historyEntry.put("SEs", usedSes);
			}
			historyEntry.put("AP", cost.getValue());
			final LocalDate currentDate = LocalDate.now();
			historyEntry.put("Datum", currentDate.toString());
			history.add(historyEntry);

			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - cost.getValue());
			energy.setBought(target.getValue() - energy.getMax() + energy.getBought());
			energy.setSes(Math.max(energy.getSes() - usedSes, 0));

			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		nameLabel.setText(energy.getName());
		startLabel.setText(Integer.toString(energy.getMax()));
		maxLabel.setText(Integer.toString(energy.getMax() - energy.getBought() + energy.getBuyableMaximum()));
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(energy.getMax());
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMax(energy.getMax() - energy.getBought() + energy.getBuyableMaximum());
		target.getValueFactory().setValue(initialTarget);
		ses.getValueFactory().setValue(energy.getSes());

		stage.show();
	}

	private int getCalculatedCost(final Energy energy, final JSONObject hero) {
		final int SELevel = energy.getBought() + Math.min(target.getValue() - energy.getMax(), ses.getValue());
		return DSAUtil.getEnhancementCost(energy.getEnhancementCost() - 1, energy.getBought(), SELevel)
				+ DSAUtil.getEnhancementCost(energy.getEnhancementCost(), SELevel, target.getValue() - energy.getMax() + energy.getBought());
	}
}
