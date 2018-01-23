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

public class QuirkReductionDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label nameLabel;
	@FXML
	private Label startLabel;
	@FXML
	private ReactiveSpinner<Integer> target;
	@FXML
	private ReactiveSpinner<Integer> ses;
	@FXML
	private ReactiveSpinner<Integer> ap;

	public QuirkReductionDialog(final Window window, final ProOrCon quirk, final JSONObject hero, final int initialTarget) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("QuirkReductionDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Schlechte Eigenschaft senken");
		stage.setScene(new Scene(root, 250, 148));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		target.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(quirk, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(quirk, hero)));

		okButton.setOnAction(event -> {
			final JSONArray history = hero.getArr("Historie");
			final JSONObject historyEntry = new JSONObject(history);
			historyEntry.put("Typ", "Schlechte Eigenschaft");
			historyEntry.put("Schlechte Eigenschaft", quirk.getName());
			final JSONObject con = quirk.getProOrCon();
			if (con.containsKey("Auswahl")) {
				historyEntry.put("Auswahl", quirk.getActual().getString("Auswahl"));
			}
			if (con.containsKey("Freitext")) {
				historyEntry.put("Freitext", quirk.getActual().getString("Freitext"));
			}
			historyEntry.put("Von", quirk.getValue());
			historyEntry.put("Auf", target.getValue());
			final int usedSes = Math.min(quirk.getValue() - target.getValue(), ses.getValue());
			if (usedSes > 0) {
				historyEntry.put("SEs", usedSes);
			}
			historyEntry.put("AP", ap.getValue());
			final LocalDate currentDate = LocalDate.now();
			historyEntry.put("Datum", currentDate.toString());
			history.add(historyEntry);
			history.notifyListeners(null);

			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - ap.getValue());
			if (target.getValue() == 0) {
				final JSONObject cons = hero.getObj("Nachteile");
				if (con.containsKey("Auswahl") || con.containsKey("Freitext")) {
					cons.getArr(quirk.getName()).remove(quirk.getActual());
				} else {
					cons.removeKey(quirk.getName());
				}
				cons.notifyListeners(null);
			} else {
				quirk.setValue(target.getValue());
			}

			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		nameLabel.setText(quirk.getName());
		startLabel.setText(Integer.toString(quirk.getValue()));
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(0);
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMax(quirk.getValue() - 1);
		target.getValueFactory().setValue(initialTarget);

		stage.show();
	}

	private int getCalculatedAP(final ProOrCon quirk, final JSONObject hero) {
		final int SELevel = quirk.getValue() - Math.min(ses.getValue(), quirk.getValue() - target.getValue());
		return (int) (((quirk.getValue() - SELevel) * 50 + (SELevel - target.getValue()) * 75) * quirk.getProOrCon().getDoubleOrDefault("Kosten", 1.0));
	}
}
