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

	public ArmorSetDialog(final Window window, final JSONObject hero, final JSONArray armorSets, final JSONObject armorSet) {
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

		name.setText(armorSet != null ? armorSet.getStringOrDefault("Name", nextFreeName(armorSets)) : nextFreeName(armorSets));

		okButton.setOnAction(event -> {
			final String newName = name.getText();

			if (armorSet == null) {
				final JSONObject actualArmorSet = new JSONObject(armorSets);
				actualArmorSet.put("Name", newName);
				armorSets.add(actualArmorSet);
				actualArmorSet.notifyListeners(null);
				stage.close();
			} else if (!newName.equals(armorSet.getStringOrDefault("Name", null))) {
				if (isNameUsed(armorSets, newName)) {
					final Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Name bereits vergeben");
					alert.setHeaderText("Rüstungskombinationen müssen eindeutig benannt sein.");
					alert.setContentText("Die Rüstungskombination konnte nicht gespeichert werden.");
					alert.getButtonTypes().setAll(ButtonType.OK);
				} else {
					final String oldName = armorSet.getString("Name");
					armorSet.put("Name", newName);
					HeroUtil.foreachInventoryItem(hero, item -> item.containsKey("Kategorien") && item.getArr("Kategorien").contains("Rüstung"),
							(item, extraInventory) -> {
								final JSONArray sets = item.getArrOrDefault("Rüstungskombinationen", new JSONArray(null));
								if (sets.contains(oldName)) {
									sets.remove(oldName);
									sets.add(newName);
								}
							});
					armorSet.notifyListeners(null);
					stage.close();
				}
			}
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}

	private boolean isNameUsed(final JSONArray armorSets, final String name) {
		for (int i = 0; i < armorSets.size(); ++i) {
			if (name.equals(armorSets.getObj(i).getStringOrDefault("Name", null))) return true;
		}
		return false;
	}

	private String nextFreeName(final JSONArray armorSets) {
		if (!isNameUsed(armorSets, "Neue Rüstungskombination")) return "Neue Rüstungskombination";
		for (int i = 1; true; ++i) {
			if (!isNameUsed(armorSets, "Neue Rüstungskombination " + i)) return "Neue Rüstungskombination" + i;
		}
	}

}
