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
package heroes.fight;

import dsa41basis.util.HeroUtil;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class ArmorSetDialog {

	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;

	public ArmorSetDialog(final Window window, final JSONObject hero, final JSONObject armorSets, final JSONObject armorSet) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ArmorSetDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Rüstungskombination");
		stage.setScene(new Scene(root, 290, 55));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		name.setText(armorSet != null ? armorSets.keyOf(armorSet) : "Neue Rüstungskombination");

		okButton.setOnAction(event -> {
			final String newName = name.getText();
			if (armorSets.containsKey(newName) && !newName.equals(armorSets.keyOf(armorSet))) {
				final Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Name bereits vergeben");
				alert.setHeaderText("Rüstungskombinationen müssen eindeutig benannt sein.");
				alert.setContentText("Die Rüstungskombination konnte nicht gespeichert werden.");
				alert.getButtonTypes().setAll(ButtonType.OK);
			} else {
				JSONObject actualArmorSet = armorSet;
				if (armorSet == null) {
					actualArmorSet = new JSONObject(armorSets);
					armorSets.put(newName, actualArmorSet);
				} else {
					final String oldName = armorSets.keyOf(armorSet);
					final int index = Util.getIndex(armorSets, oldName);
					armorSets.removeKey(oldName);
					Util.insertAt(armorSets, newName, actualArmorSet, index);
					HeroUtil.foreachInventoryItem(hero, item -> item.containsKey("Kategorien") && item.getArr("Kategorien").contains("Rüstung"),
							(item, extraInventory) -> {
								final JSONArray sets = item.getArrOrDefault("Rüstungskombinationen", new JSONArray(null));
								if (sets.contains(oldName)) {
									sets.remove(oldName);
									sets.add(newName);
								}
							});
					if (oldName.equals(hero.getObj("Kampf").getStringOrDefault("Rüstung", null))) {
						final JSONObject fight = hero.getObj("Kampf");
						fight.put("Rüstung", newName);
						fight.notifyListeners(null);
					}
				}
				actualArmorSet.notifyListeners(null);
				stage.close();
			}
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}

}
