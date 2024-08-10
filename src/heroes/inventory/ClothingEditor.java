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
package heroes.inventory;

import dsa41basis.inventory.BooksEditor;
import dsa41basis.inventory.Clothing;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ClothingEditor {
	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private TextField notes;
	@FXML
	private Button okButton;
	@FXML
	private ReactiveSpinner<Double> weight;
	@FXML
	private ReactiveSpinner<Integer> ks;
	@FXML
	private Hyperlink books;
	@FXML
	private Button cancelButton;

	public ClothingEditor(final Window window, final Clothing clothing) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ClothingEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 310, 140));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		name.setText(clothing.getName());
		ks.getValueFactory().setValue(clothing.getKs());
		weight.getValueFactory().setValue(clothing.getWeight());
		notes.setText(clothing.getNotes());

		okButton.setOnAction(event -> {
			clothing.setName(name.getText());
			clothing.setKs(ks.getValue());
			clothing.setWeight(weight.getValue());
			clothing.setNotes(notes.getText());
			stage.close();
		});

		books.setOnAction(event -> new BooksEditor(stage, clothing));

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}
}
