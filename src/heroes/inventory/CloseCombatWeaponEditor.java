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

import dsa41basis.fight.CloseCombatWeapon;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple;
import dsatool.util.Tuple3;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class CloseCombatWeaponEditor {
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
	private ReactiveSpinner<Double> weight;
	@FXML
	private ReactiveSpinner<Integer> ini;
	@FXML
	private ReactiveSpinner<Integer> at;
	@FXML
	private ReactiveSpinner<Integer> bf;
	@FXML
	private CheckBox noTpkk;
	@FXML
	private CheckBox noBf;
	@FXML
	private CheckBox tpStamina;
	@FXML
	private CheckBox tpWound;
	@FXML
	private ReactiveSpinner<Integer> pa;
	@FXML
	private ReactiveSpinner<Integer> tpNumDice;
	@FXML
	private ReactiveSpinner<Integer> tpTypeDice;
	@FXML
	private ReactiveSpinner<Integer> tpAdditional;
	@FXML
	private ReactiveSpinner<Integer> tpkkThreshold;
	@FXML
	private ReactiveSpinner<Integer> tpkkStep;
	@FXML
	private ReactiveSpinner<Integer> length;
	@FXML
	private CheckBox improvisational;
	@FXML
	private CheckBox twohanded;
	@FXML
	private CheckBox privileged;
	@FXML
	private CheckBox dkH;
	@FXML
	private CheckBox dkN;
	@FXML
	private CheckBox dkS;
	@FXML
	private CheckBox dkP;
	@FXML
	private Button cancelButton;
	@FXML
	private CheckComboBox<String> talents;

	public CloseCombatWeaponEditor(final Window window, final CloseCombatWeapon weapon) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("CloseCombatWeaponEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 290, 367));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		name.setText(weapon.getName());
		type.setText(weapon.getItemType());
		final JSONObject rangedTalents = ResourceManager.getResource("data/Talente").getObj("Nahkampftalente");
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
		final Tuple<Integer, Integer> tpkkValues = weapon.getTpkkRaw();
		if (tpkkValues._1 == Integer.MIN_VALUE || tpkkValues._2 == Integer.MIN_VALUE) {
			noTpkk.setSelected(false);
			tpkkThreshold.setDisable(true);
			tpkkStep.setDisable(true);
		} else {
			noTpkk.setSelected(true);
			tpkkThreshold.getValueFactory().setValue(tpkkValues._1);
			tpkkStep.getValueFactory().setValue(tpkkValues._2);
		}
		weight.getValueFactory().setValue(weapon.getWeight());
		length.getValueFactory().setValue(weapon.getLength());
		noBf.setSelected(weapon.getBf() != Integer.MIN_VALUE);
		if (weapon.getBf() == Integer.MIN_VALUE) {
			bf.setDisable(true);
		} else {
			bf.getValueFactory().setValue(weapon.getBf());
		}
		ini.getValueFactory().setValue(weapon.getIni());
		at.getValueFactory().setValue(weapon.getWMraw()._1);
		pa.getValueFactory().setValue(weapon.getWMraw()._2);
		final String special = weapon.getSpecial();
		improvisational.setSelected(special.contains("i"));
		twohanded.setSelected(special.contains("t"));
		privileged.setSelected(special.contains("p"));
		final String dk = weapon.getDk();
		dkH.setSelected(dk.contains("H"));
		dkN.setSelected(dk.contains("N"));
		dkS.setSelected(dk.contains("S"));
		dkP.setSelected(dk.contains("P"));
		notes.setText(weapon.getNotes());

		tpkkThreshold.disableProperty().bind(noTpkk.selectedProperty().not());
		tpkkStep.disableProperty().bind(noTpkk.selectedProperty().not());
		bf.disableProperty().bind(noBf.selectedProperty().not());

		okButton.setOnAction(event -> {
			weapon.setName(name.getText());
			weapon.setItemType(type.getText());
			weapon.setTalents(talents.getCheckModel().getCheckedItems());
			weapon.setTp(tpTypeDice.getValue(), tpNumDice.getValue(), tpAdditional.getValue(), tpWound.isSelected(), tpStamina.isSelected());
			weapon.setTPKK(noTpkk.isSelected() ? tpkkThreshold.getValue() : Integer.MIN_VALUE, noTpkk.isSelected() ? tpkkStep.getValue() : Integer.MIN_VALUE);
			weapon.setWeight(weight.getValue());
			weapon.setLength(length.getValue());
			weapon.setBf(noBf.isSelected() ? bf.getValue() : Integer.MIN_VALUE);
			weapon.setIni(ini.getValue());
			weapon.setWM(at.getValue(), pa.getValue());
			weapon.setSpecial(improvisational.isSelected(), twohanded.isSelected(), privileged.isSelected());
			weapon.setDK((dkH.isSelected() ? "H" : "") + (dkN.isSelected() ? "N" : "") + (dkS.isSelected() ? "S" : "") + (dkP.isSelected() ? "P" : ""));
			weapon.setNotes(notes.getText());
			stage.close();
		});

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}
}
