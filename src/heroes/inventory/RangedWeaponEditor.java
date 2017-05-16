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

import org.controlsfx.control.CheckComboBox;

import dsa41basis.fight.RangedWeapon;
import dsatool.resources.ResourceManager;
import dsatool.util.ErrorLogger;
import dsatool.util.ReactiveSpinner;
import dsatool.util.Tuple3;
import dsatool.util.Tuple5;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class RangedWeaponEditor {
	@FXML
	private VBox root;
	@FXML
	private TextField name;
	@FXML
	private TextField type;
	@FXML
	private TextField notes;
	@FXML
	private Button okButton;
	@FXML
	private CheckBox noBullet;
	@FXML
	private CheckBox noBulletweight;
	@FXML
	private ReactiveSpinner<Integer> weight;
	@FXML
	private ComboBox<String> bulletType;
	@FXML
	private ReactiveSpinner<Integer> weightBullet;
	@FXML
	private CheckBox tpStamina;
	@FXML
	private CheckBox tpWound;
	@FXML
	private ReactiveSpinner<Integer> tpNumDice;
	@FXML
	private ReactiveSpinner<Integer> tpTypeDice;
	@FXML
	private ReactiveSpinner<Integer> tpAdditional;
	@FXML
	private ReactiveSpinner<Integer> load;
	@FXML
	private ReactiveSpinner<Integer> distanceVeryClose;
	@FXML
	private ReactiveSpinner<Integer> distanceClose;
	@FXML
	private ReactiveSpinner<Integer> distanceMedium;
	@FXML
	private ReactiveSpinner<Integer> distanceFar;
	@FXML
	private ReactiveSpinner<Integer> distanceVeryFar;
	@FXML
	private ReactiveSpinner<Integer> distanceTPVeryClose;
	@FXML
	private ReactiveSpinner<Integer> distanceTPClose;
	@FXML
	private ReactiveSpinner<Integer> distanceTPMedium;
	@FXML
	private ReactiveSpinner<Integer> distanceTPFar;
	@FXML
	private ReactiveSpinner<Integer> distanceTPVeryFar;
	@FXML
	private Button cancelButton;
	@FXML
	private CheckComboBox<String> talents;

	public RangedWeaponEditor(final Window window, final RangedWeapon weapon) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("RangedWeaponEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 440, 330));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		name.setText(weapon.getName());
		type.setText(weapon.getItemType());
		final JSONObject rangedTalents = ResourceManager.getResource("data/Talente").getObj("Fernkampftalente");
		for (final String talent : rangedTalents.keySet()) {
			talents.getItems().add(talent);
		}
		for (final String talent : weapon.getTalents()) {
			talents.getCheckModel().check(talent);
		}
		final Tuple3<Integer, Integer, Integer> tpValues = weapon.getTpRaw();
		tpNumDice.getValueFactory().setValue(tpValues._1);
		tpTypeDice.getValueFactory().setValue(tpValues._2);
		tpAdditional.getValueFactory().setValue(tpValues._3);
		final String tp = weapon.getTp();
		tpStamina.setSelected(tp.contains("A"));
		tpWound.setSelected(tp.contains("*"));
		final Tuple5<Integer, Integer, Integer, Integer, Integer> distances = weapon.getDistanceRaw();
		distanceVeryClose.getValueFactory().setValue(distances._1);
		distanceClose.getValueFactory().setValue(distances._2);
		distanceMedium.getValueFactory().setValue(distances._3);
		distanceFar.getValueFactory().setValue(distances._4);
		distanceVeryFar.getValueFactory().setValue(distances._5);
		final Tuple5<Integer, Integer, Integer, Integer, Integer> distanceTp = weapon.getDistancetpRaw();
		distanceTPVeryClose.getValueFactory().setValue(distanceTp._1);
		distanceTPClose.getValueFactory().setValue(distanceTp._2);
		distanceTPMedium.getValueFactory().setValue(distanceTp._3);
		distanceTPFar.getValueFactory().setValue(distanceTp._4);
		distanceTPVeryFar.getValueFactory().setValue(distanceTp._5);
		weight.getValueFactory().setValue((int) weapon.getWeight());
		bulletType.setItems(FXCollections.observableArrayList("Pfeile", "Bolzen"));
		final String ammunitionType = weapon.getAmmunitionType();
		noBullet.setSelected(ammunitionType != null);
		if (ammunitionType == null) {
			bulletType.setDisable(true);
		} else {
			bulletType.setValue(ammunitionType);
		}
		final double bulletWeight = weapon.getBulletweight();
		noBulletweight.setSelected(bulletWeight != Double.NEGATIVE_INFINITY);
		if (bulletWeight == Double.NEGATIVE_INFINITY) {
			weightBullet.setDisable(true);
		} else {
			weightBullet.getValueFactory().setValue((int) bulletWeight);
		}
		load.getValueFactory().setValue(weapon.getLoad());
		notes.setText(weapon.getNotes());

		bulletType.disableProperty().bind(noBullet.selectedProperty().not());
		weightBullet.disableProperty().bind(noBulletweight.selectedProperty().not());

		okButton.setOnAction(event -> {
			weapon.setName(name.getText());
			weapon.setItemType(type.getText());
			weapon.setTalents(talents.getCheckModel().getCheckedItems());
			weapon.setTp(tpTypeDice.getValue(), tpNumDice.getValue(), tpAdditional.getValue(), tpWound.isSelected(), tpStamina.isSelected());
			weapon.setDistances(distanceVeryClose.getValue(), distanceClose.getValue(), distanceMedium.getValue(), distanceFar.getValue(),
					distanceVeryFar.getValue());
			weapon.setDistanceTPs(distanceTPVeryClose.getValue(), distanceTPClose.getValue(), distanceTPMedium.getValue(), distanceTPFar.getValue(),
					distanceTPVeryFar.getValue());
			weapon.setWeight(weight.getValue());
			weapon.setAmmunitionType(noBullet.isSelected() ? bulletType.getValue() : null);
			weapon.setBulletweight(noBulletweight.isSelected() ? weightBullet.getValue() : Double.NEGATIVE_INFINITY);
			weapon.setLoad(load.getValue());
			weapon.setNotes(notes.getText());
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		stage.show();
	}
}
