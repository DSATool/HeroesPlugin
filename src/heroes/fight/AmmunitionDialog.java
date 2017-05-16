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

import dsa41basis.fight.RangedWeapon;
import dsatool.resources.ResourceManager;
import dsatool.util.ReactiveSpinner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class AmmunitionDialog {

	public AmmunitionDialog(Window window, RangedWeapon weapon) {
		final VBox root = new VBox(2);
		root.setPadding(new Insets(5, 5, 5, 5));

		final JSONObject ammunition = weapon.getAmmunitionTypes().clone(null);

		final JSONObject ammunitionTypes = ResourceManager.getResource("data/Geschosstypen");
		for (final String name : ammunitionTypes.keySet()) {
			final HBox row = new HBox(2);
			final Label nameLabel = new Label(name);
			nameLabel.setMaxWidth(Double.POSITIVE_INFINITY);
			final ReactiveSpinner<Integer> amount = new ReactiveSpinner<>(0, 999, ammunition.getObj(name).getIntOrDefault("Aktuell", 0));
			amount.setPrefWidth(70);
			amount.setEditable(true);
			amount.valueProperty().addListener((o, oldV, newV) -> ammunition.getObj(name).put("Aktuell", newV));
			row.getChildren().addAll(nameLabel, amount);
			HBox.setHgrow(nameLabel, Priority.ALWAYS);
			root.getChildren().add(row);
		}

		final Stage stage = new Stage();
		stage.setTitle("Munition für " + weapon.getName());
		stage.setScene(new Scene(root, 250, 35 + 27 * ammunitionTypes.size()));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		final HBox buttonRow = new HBox(2);
		buttonRow.setAlignment(Pos.BOTTOM_RIGHT);
		final Button okButton = new Button("Ok");
		okButton.setPrefWidth(75);
		okButton.setOnAction(e -> {
			weapon.setAmmunition(ammunition);
			stage.close();
		});
		final Button cancelButton = new Button("Abbrechen");
		cancelButton.setPrefWidth(75);
		cancelButton.setOnAction(e -> {
			stage.close();
		});
		buttonRow.getChildren().addAll(okButton, cancelButton);
		root.getChildren().add(buttonRow);

		stage.show();
	}

}
