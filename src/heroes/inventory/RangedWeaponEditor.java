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
import dsa41basis.inventory.BooksEditor;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple3;
import dsatool.util.Tuple5;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONObject;

public class RangedWeaponEditor {
	@FXML
	private Parent root;
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
	private ReactiveSpinner<Double> weight;
	@FXML
	private ReactiveSpinner<Integer> at;
	@FXML
	private ComboBox<String> bulletType;
	@FXML
	private VBox bulletBox;
	@FXML
	private ReactiveSpinner<Double> weightBullet;
	@FXML
	private Node ammunitionBox;
	@FXML
	private ReactiveSpinner<Integer> amount;
	@FXML
	private CheckBox tpStamina;
	@FXML
	private CheckBox tpWound;
	@FXML
	private CheckBox tpMagic;
	@FXML
	private CheckBox tpCleric;
	@FXML
	private CheckBox tpCold;
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
	private Hyperlink books;
	@FXML
	private Button cancelButton;
	@FXML
	private CheckComboBox<String> talents;

	private final Stage stage;
	private final JSONObject ammunitionTypes;

	public RangedWeaponEditor(final Window window, final RangedWeapon weapon) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("RangedWeaponEditor.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		stage = new Stage();
		stage.setTitle("Bearbeiten");
		stage.setScene(new Scene(root, 440, 438));
		stage.setHeight(438);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(false);
		stage.initOwner(window);

		name.setText(weapon.getName());
		type.setText(weapon.getItemType());
		final JSONObject rangedTalents = ResourceManager.getResource("data/Talente").getObj("Fernkampftalente");
		talents.getItems().addAll(rangedTalents.keySet());
		for (final String talent : weapon.getTalents()) {
			talents.getCheckModel().check(talent);
		}
		final Tuple3<Integer, Integer, Integer> tpValues = weapon.getTpRaw();
		tpNumDice.getValueFactory().setValue(tpValues._1);
		tpTypeDice.getValueFactory().setValue(tpValues._2);
		tpAdditional.getValueFactory().setValue(tpValues._3);
		final String tp = weapon.getTp();
		tpMagic.setSelected(tp.contains("m"));
		tpCleric.setSelected(tp.contains("g"));
		tpCold.setSelected(tp.contains("k"));
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
		weight.getValueFactory().setValue(weapon.getWeight());
		at.getValueFactory().setValue(weapon.getWMraw());

		bulletType.setItems(FXCollections.observableArrayList("Pfeile", "Bolzen"));

		ammunitionTypes = ResourceManager.getResource("data/Geschosstypen");
		final String ammunitionType = weapon.getAmmunitionType();
		noBullet.setSelected(ammunitionType != null);
		if (ammunitionType == null) {
			bulletType.setDisable(true);
		} else {
			bulletType.setValue(ammunitionType);
		}
		setAmmunitionVisible("Pfeile".equals(ammunitionType) || "Bolzen".equals(ammunitionType));

		final double bulletWeight = weapon.getBulletweight();
		weightBullet.getValueFactory().setValue(bulletWeight == Double.NEGATIVE_INFINITY ? 0 : bulletWeight);

		final JSONObject ammunition = weapon.getAmmunitionTypes().clone(null);
		for (final String type : ammunitionTypes.keySet()) {
			final Label label = new Label(type + ":");
			label.setMaxWidth(Double.POSITIVE_INFINITY);
			HBox.setHgrow(label, Priority.ALWAYS);
			final ReactiveSpinner<Integer> spinner = new ReactiveSpinner<>(0, 9999, ammunition.getObj(type).getIntOrDefault("Gesamt", 0));
			spinner.valueProperty().addListener((o, oldV, newV) -> {
				if (newV == 0) {
					ammunition.removeKey(type);
				} else {
					final JSONObject currentType = ammunition.getObj(type);
					if (!currentType.containsKey("Aktuell") || currentType.getInt("Aktuell").equals(currentType.getIntOrDefault("Gesamt", oldV))) {
						currentType.put("Aktuell", newV);
					}
					currentType.put("Gesamt", newV);
				}
			});
			bulletBox.getChildren().add(new HBox(2, label, spinner));
		}

		amount.getValueFactory().setValue(weapon.getAmmunitionMax());
		load.getValueFactory().setValue(weapon.getLoad());
		notes.setText(weapon.getNotes());

		weightBullet.disableProperty().bind(noBullet.selectedProperty().not());

		noBullet.selectedProperty().addListener((o, oldV, newV) -> {
			bulletType.setDisable(!newV);
			setAmmunitionVisible(newV && ("Pfeile".equals(bulletType.getValue()) || "Bolzen".equals(bulletType.getValue())));
		});

		bulletType.valueProperty().addListener((o, oldV, newV) -> setAmmunitionVisible("Pfeile".equals(newV) || "Bolzen".equals(newV)));

		okButton.setOnAction(event -> {
			weapon.setName(name.getText());
			weapon.setItemType(type.getText());
			weapon.setTalents(talents.getCheckModel().getCheckedItems());
			weapon.setTp(tpTypeDice.getValue(), tpNumDice.getValue(), tpAdditional.getValue(), tpWound.isSelected(), tpStamina.isSelected(),
					tpMagic.isSelected(), tpCleric.isSelected(), tpCold.isSelected());
			weapon.setDistances(distanceVeryClose.getValue(), distanceClose.getValue(), distanceMedium.getValue(), distanceFar.getValue(),
					distanceVeryFar.getValue());
			weapon.setDistanceTPs(distanceTPVeryClose.getValue(), distanceTPClose.getValue(), distanceTPMedium.getValue(), distanceTPFar.getValue(),
					distanceTPVeryFar.getValue());
			weapon.setWeight(weight.getValue());
			weapon.setWM(at.getValue());
			weapon.setAmmunitionType(noBullet.isSelected() ? bulletType.getValue() : null);
			weapon.setBulletweight(noBullet.isSelected() ? weightBullet.getValue() : Double.NEGATIVE_INFINITY);
			if (bulletBox.isVisible()) {
				weapon.setAmmunition(ammunition);
				weapon.setMaxAmmunition(0);
			} else {
				weapon.setAmmunition(null);
				weapon.setMaxAmmunition(amount.getValue());
			}
			weapon.setLoad(load.getValue());
			weapon.setNotes(notes.getText());
			stage.close();
		});

		books.setOnAction(event -> new BooksEditor(stage, weapon));

		cancelButton.setOnAction(event -> stage.close());

		okButton.setDefaultButton(true);
		cancelButton.setCancelButton(true);

		stage.show();
	}

	private void setAmmunitionVisible(final boolean bulletTypes) {
		if (bulletTypes) {
			stage.setHeight(438 + (ammunitionTypes.size() - 1) * 25);
			bulletBox.setVisible(true);
			bulletBox.setManaged(true);
			ammunitionBox.setVisible(false);
			ammunitionBox.setManaged(false);
		} else {
			stage.setHeight(420);
			bulletBox.setVisible(false);
			bulletBox.setManaged(false);
			ammunitionBox.setVisible(true);
			ammunitionBox.setManaged(true);
		}
	}
}
