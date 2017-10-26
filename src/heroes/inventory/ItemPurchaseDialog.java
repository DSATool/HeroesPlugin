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
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
	private Button okButton;
	@FXML
	private ReactiveSpinner<Double> cost;
	@FXML
	private Button cancelButton;

	public ItemPurchaseDialog(final Window window, final JSONObject hero, final JSONObject item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ItemPurchaseDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Kaufen");
		stage.setScene(new Scene(root, 330, 85));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		name.setText(item.getString("Name"));
		cost.getValueFactory().setValue(item.getDoubleOrDefault("Preis", 0.0));

		okButton.setOnAction(event -> {
			item.put("Name", name.getText());
			final int kreutzer = (int) (cost.getValue() * 100);
			item.put("Preis", kreutzer / 100.0);
			HeroUtil.addMoney(hero, -kreutzer);
			final JSONArray items = hero.getObj("Besitz").getArr("Ausrüstung");
			items.add(item);
			items.notifyListeners(null);
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		stage.show();
	}
}
