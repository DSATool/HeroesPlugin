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
import dsa41basis.inventory.InventoryItem;
import dsatool.gui.GUIUtil;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ItemEditor {
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
	private ReactiveSpinner<Double> value;
	@FXML
	private Hyperlink books;
	@FXML
	private Button cancelButton;

	public ItemEditor(final Window window, final InventoryItem item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ItemEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 310, 183, "Bearbeiten", window, true);

		name.setText(item.getName());
		value.getValueFactory().setValue(item.getValue());
		weight.getValueFactory().setValue(item.getWeight());
		notes.setText(item.getNotes());

		okButton.setOnAction(_ -> {
			item.setName(name.getText());
			item.setValue(value.getValue());
			item.setWeight(weight.getValue());
			item.setNotes(notes.getText());
			stage.close();
		});

		books.setOnAction(_ -> new BooksEditor(stage, item));

		cancelButton.setOnAction(_ -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}
}
