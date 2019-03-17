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

import dsa41basis.hero.DerivedValue;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DerivedValueEditor {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private ReactiveSpinner<Integer> ses;
	@FXML
	private Button cancelButton;
	@FXML
	private HBox seBox;

	public DerivedValueEditor(final Window window, final DerivedValue value, final boolean needsSEs) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("DerivedValueEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		if (!needsSEs) {
			root.getChildren().remove(seBox);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 330, needsSEs ? 82 : 56));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		ses.getValueFactory().setValue(value.getSes());
		modifier.getValueFactory().setValue(value.getModifier());

		okButton.setOnAction(event -> {
			value.setSes(ses.getValue());
			value.setModifier(modifier.getValue());
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		stage.show();
	}
}
