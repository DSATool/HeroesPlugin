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

import dsa41basis.inventory.InventoryItem;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class HorseArmorEditor {
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
	private ReactiveSpinner<Integer> head;
	@FXML
	private ReactiveSpinner<Integer> breast;
	@FXML
	private ReactiveSpinner<Integer> back;
	@FXML
	private ReactiveSpinner<Integer> neck;
	@FXML
	private ReactiveSpinner<Integer> legs;
	@FXML
	private ReactiveSpinner<Integer> be;
	@FXML
	private HBox headBox;
	@FXML
	private HBox breastBox;
	@FXML
	private HBox backBox;
	@FXML
	private HBox neckBox;
	@FXML
	private HBox legsBox;
	@FXML
	private HBox beBox;
	@FXML
	private CheckBox horseArmor;
	@FXML
	private Button cancelButton;

	public HorseArmorEditor(final Window window, final InventoryItem item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("HorseArmorEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 335, 170));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);
		stage.setHeight(170);

		horseArmor.selectedProperty().addListener((o, oldV, newV) -> {
			headBox.setVisible(newV);
			neckBox.setVisible(newV);
			breastBox.setVisible(newV);
			backBox.setVisible(newV);
			legsBox.setVisible(newV);
			beBox.setVisible(newV);
			stage.setHeight(newV ? 330 : 170);
		});

		final JSONObject actual = item.getItem();
		final JSONObject armor = actual.containsKey("Pferderüstung") ? actual.getObj("Pferderüstung") : actual;
		final JSONObject rs = armor.getObjOrDefault("Rüstungsschutz", actual.getObj("Rüstungsschutz"));

		name.setText(item.getName());
		horseArmor.setSelected(actual.getArr("Kategorien").contains("Pferderüstung"));
		head.getValueFactory().setValue(rs.getIntOrDefault("Kopf", 0));
		neck.getValueFactory().setValue(rs.getIntOrDefault("Hals", 0));
		breast.getValueFactory().setValue(rs.getIntOrDefault("Brust", 0));
		back.getValueFactory().setValue(rs.getIntOrDefault("Kruppe", 0));
		legs.getValueFactory().setValue(rs.getIntOrDefault("Läufe", 0));
		be.getValueFactory().setValue(armor.getIntOrDefault("Behinderung", actual.getIntOrDefault("Behinderung", 0)));
		weight.getValueFactory().setValue(item.getWeight());
		notes.setText(item.getNotes());

		headBox.managedProperty().bindBidirectional(headBox.visibleProperty());
		neckBox.managedProperty().bindBidirectional(neckBox.visibleProperty());
		breastBox.managedProperty().bindBidirectional(breastBox.visibleProperty());
		backBox.managedProperty().bindBidirectional(backBox.visibleProperty());
		legsBox.managedProperty().bindBidirectional(legsBox.visibleProperty());
		beBox.managedProperty().bindBidirectional(beBox.visibleProperty());

		okButton.setOnAction(event -> {
			item.setName(name.getText());
			item.setWeight(weight.getValue());
			item.setNotes(notes.getText());
			if (horseArmor.isSelected()) {
				rs.put("Kopf", head.getValue());
				rs.put("Hals", neck.getValue());
				rs.put("Brust", breast.getValue());
				rs.put("Kruppe", back.getValue());
				rs.put("Läufe", legs.getValue());
				armor.put("Behinderung", be.getValue());
				if (!actual.getArr("Kategorien").contains("Pferderüstung")) {
					actual.getArr("Kategorien").add("Pferderüstung");
				}
			} else {
				actual.getArr("Kategorien").remove("Pferderüstung");
			}
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		stage.show();
	}
}
