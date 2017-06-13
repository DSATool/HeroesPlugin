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
package heroes.talents;

import dsa41basis.hero.Talent;
import dsa41basis.util.DSAUtil;
import dsatool.resources.Settings;
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class TalentEnhancementDialog {
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
	private ComboBox<String> method;
	@FXML
	private ReactiveSpinner<Integer> cost;

	int startValue;

	public TalentEnhancementDialog(final Window window, final Talent talent, final JSONObject hero, final int initialTarget) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("TalentEnhancementDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Talent steigern");
		stage.setScene(new Scene(root, 250, 175));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		target.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(talent, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(talent, hero)));
		method.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> cost.getValueFactory().setValue(getCalculatedCost(talent, hero)));

		okButton.setOnAction(event -> {
			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - cost.getValue());
			talent.setValue(target.getValue());
			talent.setSes(Math.max(talent.getSes() - Math.min(ses.getValue(), target.getValue() - talent.getValue()), 0));
			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		startValue = talent.getValue();
		if (startValue == Integer.MIN_VALUE) {
			startValue = -1;
		}

		nameLabel.setText(talent.getName());
		startLabel.setText(talent.getValue() == Integer.MIN_VALUE ? "n.a." : Integer.toString(startValue));
		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(talent.getValue() == Integer.MIN_VALUE ? 0 : startValue);
		target.getValueFactory().setValue(initialTarget);
		ses.getValueFactory().setValue(talent.getSes());
		method.setItems(FXCollections.observableArrayList("Lehrmeister", "Gegenseitiges Lehren", "Selbststudium"));
		method.getSelectionModel().select(Settings.getSettingStringOrDefault("Gegenseitiges Lehren", "Steigerung", "Lernmethode"));

		stage.show();
	}

	private int getCalculatedCost(final Talent talent, final JSONObject hero) {
		final int SELevel = startValue + Math.min(target.getValue() - startValue, ses.getValue());
		final int seCost = DSAUtil.getEnhancementCost(talent, hero, "Lehrmeister", startValue, SELevel, false);
		final int normalCost = DSAUtil.getEnhancementCost(talent, hero, method.getSelectionModel().getSelectedItem(), SELevel, target.getValue(), false);
		return seCost + normalCost;
	}
}
