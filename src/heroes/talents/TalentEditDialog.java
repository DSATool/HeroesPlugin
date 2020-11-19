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
import dsa41basis.util.HeroUtil;
import dsatool.util.ErrorLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class TalentEditDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label nameLabel;
	@FXML
	private ComboBox<String> variant;

	public TalentEditDialog(final Window window, final Talent actualTalent) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("TalentEditDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Talent bearbeiten");
		stage.setScene(new Scene(root, 250, 75));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		okButton.setOnAction(event -> {
			actualTalent.setVariant(variant.getValue());
			stage.close();
		});

		cancelButton.setOnAction(e -> stage.close());

		final JSONObject talent = actualTalent.getTalent();

		nameLabel.setText(actualTalent.getName());
		variant.setItems(FXCollections
				.observableArrayList(HeroUtil.getChoices(null, talent.getStringOrDefault("Auswahl", talent.getStringOrDefault("Freitext", null)), null)));
		variant.setEditable(talent.containsKey("Freitext"));
		variant.getSelectionModel().select(actualTalent.getVariant());

		stage.show();
	}
}
