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

import dsatool.gui.GUIUtil;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class InventoryDialog {

	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;

	public InventoryDialog(final Window window, final JSONArray inventories, final JSONObject inventory) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("InventoryDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 290, 95, "Inventar", window, true);

		name.setText(inventory != null ? inventory.getStringOrDefault("Name", "Neues Inventar") : "Neues Inventar");

		okButton.setOnAction(event -> {
			JSONObject actualInventory = inventory;
			if (inventory == null) {
				actualInventory = new JSONObject(inventories);
				actualInventory.put("Ausrüstung", new JSONArray(actualInventory));
				inventories.add(actualInventory);
			}
			actualInventory.put("Name", name.getText());
			actualInventory.notifyListeners(null);
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}

}
