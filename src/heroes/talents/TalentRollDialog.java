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
package heroes.talents;

import java.util.HashMap;
import java.util.Map;

import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.gui.GUIUtil;
import dsatool.resources.ResourceManager;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import dsatool.util.Tuple;
import dsatool.util.Tuple3;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class TalentRollDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private ReactiveSpinner<Integer> modification;
	@FXML
	private CheckBox additionalTime;
	@FXML
	private ComboBox<String> specialization;
	@FXML
	private ComboBox<String> attribute1;
	@FXML
	private ComboBox<String> attribute2;
	@FXML
	private ComboBox<String> attribute3;

	private final String talentName;
	private final String representation;
	private final Map<JSONObject, HBox> heroBoxes = new HashMap<>();

	public TalentRollDialog(final Window window, final String talentName, final String representation, final JSONObject[] heroes) {
		this.talentName = talentName;
		this.representation = representation;
		init(window, heroes).show();
	}

	public TalentRollDialog(final Window window, final String talentName, final String representation, final JSONObject[] heroes, final Integer[] results,
			final int modification, final String specialization) {
		this.talentName = talentName;
		this.representation = representation;

		final Stage stage = init(window, heroes);

		this.modification.getValueFactory().setValue(modification);
		if (specialization != null) {
			this.specialization.getSelectionModel().select(specialization);
		}

		stage.showAndWait();

		for (int i = 0; i < heroes.length; ++i) {
			results[i] = updateInterpretation(heroes[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public Stage init(final Window window, final JSONObject[] heroes) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("TalentRollDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final Stage stage = GUIUtil.setupStage(root, 450, 130 + heroes.length * 28, "Talentprobe: " + talentName, window, true);

		okButton.setOnAction(_ -> stage.close());
		okButton.setDefaultButton(true);

		final Tuple<JSONObject, JSONObject> talent;
		if (representation != null) {
			final JSONObject spells = ResourceManager.getResource("data/Zauber");
			talent = new Tuple<>(spells.getObj(talentName), spells);
		} else {
			final Tuple<JSONObject, String> talentAndGroup = HeroUtil.findTalent(talentName);
			JSONObject talentGroup = ResourceManager.getResource("data/Talentgruppen").getObj(talentAndGroup._2);
			if ("Sprachen und Schriften".equals(talentAndGroup._2)) {
				talentGroup = talentGroup.getObj(talentAndGroup._1.getBoolOrDefault("Schrift", false) ? "Schriften" : "Sprachen");
			}
			talent = new Tuple<>(talentAndGroup._1, talentGroup);
		}

		final JSONObject attributes = ResourceManager.getResource("data/Eigenschaften");
		for (final String attribute : attributes.keySet()) {
			attribute1.getItems().add(attribute);
			attribute2.getItems().add(attribute);
			attribute3.getItems().add(attribute);
		}
		attribute1.getItems().add("XX");
		attribute2.getItems().add("XX");
		attribute3.getItems().add("XX");

		final JSONArray challenge = talent._1.getArrOrDefault("Probe", talent._2.getArr("Probe"));
		attribute1.setValue(challenge.getString(0));
		attribute2.setValue(challenge.getString(1));
		attribute3.setValue(challenge.getString(2));

		specialization.getItems().add("Keine");
		final JSONArray specializations = talent._1.getArr("Spezialisierungen");
		if (specializations != null) {
			specialization.getItems().addAll(specializations.getStrings());
		}

		specialization.getSelectionModel().select(0);

		for (final JSONObject hero : heroes) {
			try {
				final FXMLLoader heroLoader = new FXMLLoader();
				final HBox heroBox = heroLoader.load(getClass().getResource("TalentRoll.fxml").openStream());
				final Tuple3<Integer, Integer, Integer> roll = DSAUtil.talentRoll();
				final String name = hero.getObj("Biografie").getStringOrDefault("Vorname", "Unbenannt");
				final SelectionModel<String> selection = specialization.getSelectionModel();
				final int taw = (int) (HeroUtil.getTaW(hero, talentName, selection.getSelectedIndex() == 0 ? null : selection.getSelectedItem()) + 0.5);
				final String tawString = taw == Integer.MIN_VALUE ? "-" : Integer.toString(taw);
				final String heroText = name + " (" + tawString + ")";
				((Label) heroBox.getChildren().get(0)).setText(heroText);
				for (int i = 1; i <= 3; ++i) {
					final ReactiveSpinner<Integer> attribute = (ReactiveSpinner<Integer>) heroBox.getChildren().get(i);
					attribute.getValueFactory().setValue((Integer) roll.get(i));
					attribute.valueProperty().addListener((_, _, _) -> updateInterpretation(hero));
				}
				root.getChildren().add(root.getChildren().size() - 1, heroBox);
				heroBoxes.put(hero, heroBox);
				updateInterpretation(hero);
			} catch (final Exception e) {
				ErrorLogger.logError(e);
			}
		}

		final ChangeListener<? super Object> updateListener = (_, _, _) -> {
			for (final JSONObject hero : heroes) {
				updateInterpretation(hero);
			}
		};

		specialization.getSelectionModel().selectedIndexProperty().addListener(updateListener);

		modification.valueProperty().addListener(updateListener);
		additionalTime.selectedProperty().addListener(updateListener);

		attribute1.valueProperty().addListener(updateListener);
		attribute2.valueProperty().addListener(updateListener);
		attribute3.valueProperty().addListener(updateListener);

		return stage;
	}

	private Integer updateInterpretation(final JSONObject hero) {
		final HBox heroBox = heroBoxes.get(hero);
		final Label interpretationLabel = (Label) heroBox.getChildren().get(4);
		final int[] roll = new int[3];
		int ones = 0;
		int twenties = 0;
		for (int i = 1; i <= 3; ++i) {
			@SuppressWarnings("unchecked")
			final ReactiveSpinner<Integer> attribute = (ReactiveSpinner<Integer>) heroBox.getChildren().get(i);
			final int currentRoll = attribute.getValue();
			if (currentRoll == 1) {
				++ones;
			} else if (currentRoll == 20) {
				++twenties;
			}
			roll[i - 1] = currentRoll;
		}
		final SelectionModel<String> selection = specialization.getSelectionModel();
		Integer interpretation;
		if (representation != null) {
			final int zfw = HeroUtil.getZfW(hero, talentName, representation, selection.getSelectedIndex() == 0 ? null : selection.getSelectedItem());
			int mod = -modification.getValue();
			if (additionalTime.isSelected()) {
				if (representation == "Mag") {
					mod += 4;
				} else {
					mod += 3;
				}
			}
			interpretation = HeroUtil.interpretTalentRoll(hero, new String[] { attribute1.getValue(), attribute2.getValue(), attribute3.getValue() }, zfw,
					new Tuple3<>(roll[0], roll[1], roll[2]), mod);
		} else {
			double taw = HeroUtil.getTaW(hero, talentName, selection.getSelectedIndex() == 0 ? null : selection.getSelectedItem());
			if (additionalTime.isSelected() && (int) taw != Integer.MIN_VALUE) {
				taw *= 1.5;
			}
			interpretation = HeroUtil.interpretTalentRoll(hero, new String[] { attribute1.getValue(), attribute2.getValue(), attribute3.getValue() },
					(int) (taw + 0.5), new Tuple3<>(roll[0], roll[1], roll[2]), -modification.getValue());
		}
		interpretationLabel.setStyle("");
		if (interpretation == null) {
			interpretationLabel.setText("—");
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-invalid;");
		} else if (ones >= 2) {
			if (ones == 3) {
				interpretation = Integer.MAX_VALUE;
			} else {
				interpretation = Integer.MAX_VALUE - 1;
			}
			interpretationLabel.setText("*" + Integer.toString(interpretation) + "*");
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-valid; -fx-font-weight: bold;");
		} else if (twenties >= 2) {
			if (twenties == 3) {
				interpretation = Integer.MIN_VALUE;
			} else {
				interpretation = Integer.MIN_VALUE + 1;
			}
			interpretationLabel.setText("Patzer");
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-invalid; -fx-font-weight: bold;");
		} else if (interpretation == 0) {
			interpretationLabel.setText("1");
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-warning;");
		} else if (interpretation < 0) {
			interpretationLabel.setText(Integer.toString(interpretation));
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-invalid;");
		} else {
			interpretationLabel.setText(Integer.toString(interpretation));
			interpretationLabel.setStyle("-fx-text-fill: -fx-color-valid;");
		}
		return interpretation;
	}
}
