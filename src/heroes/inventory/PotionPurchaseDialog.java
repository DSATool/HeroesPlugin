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
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class PotionPurchaseDialog {
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
	private ComboBox<String> quality;
	@FXML
	private Button cancelButton;

	private boolean costChanged = false;

	public PotionPurchaseDialog(final Window window, final JSONObject hero, final JSONObject item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("PotionPurchaseDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Kaufen");
		stage.setScene(new Scene(root, 290, 140));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		name.setText(item.getStringOrDefault("Name", ""));
		quality.setValue(item.getStringOrDefault("Qualität", "C"));
		notes.setText(item.getStringOrDefault("Anmerkungen", ""));
		cost.getValueFactory().setValue(item.getDoubleOrDefault("Preis", 0.0));

		quality.valueProperty().addListener((o, oldV, newV) -> {
			if (!costChanged) {
				cost.getValueFactory().setValue(cost.getValue() / getPriceFactor(oldV) * getPriceFactor(newV));
				costChanged = false;
			}
		});
		cost.valueProperty().addListener((o, oldV, newV) -> costChanged = true);

		okButton.setOnAction(event -> {
			item.put("Name", name.getText());
			final String potionQuality = quality.getValue();
			if ("".equals(potionQuality)) {
				item.removeKey("Qualität");
			} else {
				item.put("Qualität", potionQuality);
			}
			final String note = notes.getText();
			if ("".equals(note)) {
				item.removeKey("Anmerkungen");
			} else {
				item.put("Anmerkungen", note);
			}
			final int kreutzer = (int) (cost.getValue() * 100);
			item.put("Preis", kreutzer / 100.0);
			HeroUtil.addMoney(hero, -kreutzer);
			final JSONArray items = hero.getObj("Besitz").getArr("Ausrüstung");
			items.add(item);
			items.notifyListeners(null);
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}

	private Double getPriceFactor(final String quality) {
		return switch (quality) {
			case "A" -> 0.6;
			case "B" -> 0.8;
			case "C" -> 1.0;
			case "D" -> 1.5;
			case "E" -> 2.0;
			case "F" -> 2.5;
			default -> 1.0;
		};
	}
}
