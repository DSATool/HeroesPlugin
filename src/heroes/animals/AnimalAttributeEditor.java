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

import dsa41basis.hero.Attribute;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AnimalAttributeEditor {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private ReactiveSpinner<Integer> modifier;
	@FXML
	private Node startBox;
	@FXML
	private ReactiveSpinner<Integer> start;
	@FXML
	private Button cancelButton;

	public AnimalAttributeEditor(final Window window, final Attribute attribute, final boolean needsStart) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("AnimalAttributeEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 330, needsStart ? 82 : 56));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		if (!needsStart) {
			root.getChildren().remove(startBox);
		}

		modifier.getValueFactory().setValue(attribute.getModifier());
		start.getValueFactory().setValue(attribute.getStart());

		okButton.setOnAction(event -> {
			attribute.setModifier(modifier.getValue());
			attribute.setStart(needsStart ? start.getValue() : 0);
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		stage.show();
	}
}
