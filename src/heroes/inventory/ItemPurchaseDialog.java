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

import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.ui.ReactiveSpinner;
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

public class ItemPurchaseDialog {
	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private TextField notes;
	@FXML
	private Button okButton;
	@FXML
	private ReactiveSpinner<Double> cost;
	@FXML
	private Button cancelButton;

	public ItemPurchaseDialog(final Window window, final JSONObject hero, final JSONArray inventory, final JSONObject item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ItemPurchaseDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 300, 155, "Kaufen", window, true);

		name.setText(item.getStringOrDefault("Name", ""));
		notes.setText(item.getStringOrDefault("Anmerkungen", ""));
		cost.getValueFactory().setValue(item.getDoubleOrDefault("Preis", 0.0));

		okButton.setOnAction(_ -> {
			item.put("Name", name.getText());
			final String note = notes.getText();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			final int kreutzer = (int) (cost.getValue() * 100);
			item.put("Preis", kreutzer / 100.0);
			HeroUtil.addMoney(hero, -kreutzer);
			inventory.add(item);
			inventory.notifyListeners(null);
			stage.close();
		});

		cancelButton.setOnAction(_ -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}
}
