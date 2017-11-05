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

import dsa41basis.fight.Armor;
import dsatool.resources.Settings;
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
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

public class ArmorEditor {
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
	private ReactiveSpinner<Double> zoners;
	@FXML
	private ReactiveSpinner<Double> zonebe;
	@FXML
	private ReactiveSpinner<Integer> head;
	@FXML
	private ReactiveSpinner<Integer> breast;
	@FXML
	private ReactiveSpinner<Integer> back;
	@FXML
	private ReactiveSpinner<Integer> belly;
	@FXML
	private ReactiveSpinner<Integer> larm;
	@FXML
	private ReactiveSpinner<Integer> rarm;
	@FXML
	private ReactiveSpinner<Integer> lleg;
	@FXML
	private ReactiveSpinner<Integer> rleg;
	@FXML
	private ReactiveSpinner<Integer> totalrs;
	@FXML
	private ReactiveSpinner<Integer> totalbe;
	@FXML
	private HBox zonersBox;
	@FXML
	private HBox zonebeBox;
	@FXML
	private HBox headBox;
	@FXML
	private HBox breastBox;
	@FXML
	private HBox backBox;
	@FXML
	private HBox bellyBox;
	@FXML
	private HBox larmBox;
	@FXML
	private HBox rarmBox;
	@FXML
	private HBox llegBox;
	@FXML
	private HBox rlegBox;
	@FXML
	private HBox totalrsBox;
	@FXML
	private HBox totalbeBox;
	@FXML
	private CheckBox additionalArmor;
	@FXML
	private Button cancelButton;

	public ArmorEditor(final Window window, final Armor item) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ArmorEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		name.setText(item.getName());
		head.getValueFactory().setValue(item.getHead());
		breast.getValueFactory().setValue(item.getBreast());
		back.getValueFactory().setValue(item.getBack());
		belly.getValueFactory().setValue(item.getBelly());
		larm.getValueFactory().setValue(item.getLarm());
		rarm.getValueFactory().setValue(item.getRarm());
		lleg.getValueFactory().setValue(item.getLleg());
		rleg.getValueFactory().setValue(item.getRleg());
		zoners.getValueFactory().setValue(item.getZoners());
		zonebe.getValueFactory().setValue(item.getZonebe());
		totalrs.getValueFactory().setValue((int) item.getTotalrs());
		totalbe.getValueFactory().setValue((int) item.getTotalbe());
		additionalArmor.setSelected(item.isAdditionalArmor());
		weight.getValueFactory().setValue(item.getWeight());
		notes.setText(item.getNotes());

		final String armorSetting = Settings.getSettingStringOrDefault("Zonenrüstung", "Kampf", "Rüstungsart");

		headBox.managedProperty().bindBidirectional(headBox.visibleProperty());
		breastBox.managedProperty().bindBidirectional(breastBox.visibleProperty());
		backBox.managedProperty().bindBidirectional(backBox.visibleProperty());
		bellyBox.managedProperty().bindBidirectional(bellyBox.visibleProperty());
		larmBox.managedProperty().bindBidirectional(larmBox.visibleProperty());
		rarmBox.managedProperty().bindBidirectional(rarmBox.visibleProperty());
		llegBox.managedProperty().bindBidirectional(llegBox.visibleProperty());
		rlegBox.managedProperty().bindBidirectional(rlegBox.visibleProperty());
		zonersBox.managedProperty().bindBidirectional(zonersBox.visibleProperty());
		zonebeBox.managedProperty().bindBidirectional(zonebeBox.visibleProperty());
		totalrsBox.managedProperty().bindBidirectional(totalrsBox.visibleProperty());
		totalbeBox.managedProperty().bindBidirectional(totalbeBox.visibleProperty());

		int height = 170;

		switch (armorSetting) {
		case "Gesamtrüstung":
			totalrsBox.setVisible(true);
			totalbeBox.setVisible(true);
			break;
		case "Zonengesamtrüstung":
			zonersBox.setVisible(true);
			zonebeBox.setVisible(true);
			break;
		default:
			headBox.setVisible(true);
			breastBox.setVisible(true);
			backBox.setVisible(true);
			bellyBox.setVisible(true);
			larmBox.setVisible(true);
			rarmBox.setVisible(true);
			llegBox.setVisible(true);
			rlegBox.setVisible(true);
			zonebeBox.setVisible(true);
			height = 375;
			break;
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 330, height));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		okButton.setOnAction(event -> {
			item.setName(name.getText());
			item.setWeight(weight.getValue());
			item.setNotes(notes.getText());

			switch (armorSetting) {
			case "Gesamtrüstung":
				item.setTotalrs(totalrs.getValue());
				item.setTotalbe(totalbe.getValue());
				break;
			case "Zonengesamtrüstung":
				item.setZoners(zoners.getValue());
				item.setZonebe(zonebe.getValue());
				break;
			default:
				item.setRs(head.getValue(), breast.getValue(), back.getValue(), belly.getValue(), larm.getValue(), rarm.getValue(), lleg.getValue(),
						rleg.getValue());
				item.setZonebe(zonebe.getValue());
				break;
			}

			item.setAdditionalArmor(additionalArmor.isSelected());
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}
}
