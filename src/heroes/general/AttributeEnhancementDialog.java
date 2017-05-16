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

import dsa41basis.hero.Attribute;
import dsa41basis.util.DSAUtil;
import dsatool.resources.ResourceManager;
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
	private ReactiveSpinner<Integer> cost;

	private final boolean isMiserable;

	public AttributeEnhancementDialog(final Window window, Attribute attribute, JSONObject hero, int initialTarget) {
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
		stage.initOwner(window);

		final JSONObject attributes = ResourceManager.getResource("data/Eigenschaften");
		isMiserable = hero.getObj("Nachteile").containsKey(attributes.getObj(attribute.getName()).getString("Miserable Eigenschaft"));

		target.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(attribute, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(attribute, hero)));

		okButton.setOnAction(event -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - cost.getValue());
			attribute.setValue(target.getValue());
			attribute.setSes(Math.max(attribute.getSes() - Math.min(ses.getValue(), target.getValue() - attribute.getValue()), 0));
			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		nameLabel.setText(attributes.getObj(attribute.getName()).getString("Name"));
		startLabel.setText(Integer.toString(attribute.getValue()));
		maxLabel.setText(Integer.toString(attribute.getMaximum()));
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(attribute.getValue());
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMax(attribute.getMaximum());
		target.getValueFactory().setValue(initialTarget);
		ses.getValueFactory().setValue(attribute.getSes());

		stage.show();
	}

	private int getCalculatedCost(Attribute attribute, JSONObject hero) {
		final int SELevel = attribute.getValue() + Math.min(target.getValue() - attribute.getValue(), ses.getValue());
		return (DSAUtil.getEnhancementCost(7, attribute.getValue(), SELevel) + DSAUtil.getEnhancementCost(8, SELevel, target.getValue()))
				* (isMiserable ? 2 : 1);
	}
}
