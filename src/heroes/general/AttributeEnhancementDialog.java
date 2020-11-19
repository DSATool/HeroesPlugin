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
import dsa41basis.util.DSAUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
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

public class AttributeEnhancementDialog {
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
	private Label maxLabel;
	@FXML
	private ReactiveSpinner<Integer> target;
	@FXML
	private ReactiveSpinner<Integer> ses;
	@FXML
	private ReactiveSpinner<Integer> ap;

	private final boolean isMiserable;

	public AttributeEnhancementDialog(final Window window, final Attribute attribute, final JSONObject hero, final int initialTarget) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("EnhancementDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Eigenschaft steigern");
		stage.setScene(new Scene(root, 200, 170));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		final JSONObject attributes = ResourceManager.getResource("data/Eigenschaften");
		isMiserable = hero.getObj("Nachteile").containsKey(attributes.getObj(attribute.getName()).getString("Miserable Eigenschaft"));

		target.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(attribute, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(attribute, hero)));

		okButton.setOnAction(event -> {
			final int usedSes = Math.min(target.getValue() - attribute.getValue(), ses.getValue());
			final JSONArray history = hero.getArr("Historie");
			final JSONObject historyEntry = new JSONObject(history);
			historyEntry.put("Typ", "Eigenschaft");
			historyEntry.put("Eigenschaft", attribute.getName());
			historyEntry.put("Von", attribute.getValue());
			historyEntry.put("Auf", target.getValue());
			if (usedSes > 0) {
				historyEntry.put("SEs", usedSes);
			}
			historyEntry.put("AP", ap.getValue());
			final LocalDate currentDate = LocalDate.now();
			historyEntry.put("Datum", currentDate.toString());
			history.add(historyEntry);

			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - ap.getValue());
			attribute.setValue(target.getValue());
			attribute.setSes(Math.max(attribute.getSes() - usedSes, 0));

			stage.close();
		});

		cancelButton.setOnAction(e -> stage.close());

		nameLabel.setText(attributes.getObj(attribute.getName()).getString("Name"));
		startLabel.setText(Integer.toString(attribute.getValue()));
		maxLabel.setText(Integer.toString(attribute.getMaximum()));
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(attribute.getValue());
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMax(attribute.getMaximum());
		target.getValueFactory().setValue(initialTarget);
		ses.getValueFactory().setValue(attribute.getSes());

		stage.show();
	}

	private int getCalculatedAP(final Attribute attribute, final JSONObject hero) {
		final int SELevel = attribute.getValue() + Math.min(target.getValue() - attribute.getValue(), ses.getValue());
		return (DSAUtil.getEnhancementCost(7, attribute.getValue(), SELevel) + DSAUtil.getEnhancementCost(8, SELevel, target.getValue()))
				* (isMiserable ? 2 : 1);
	}
}
