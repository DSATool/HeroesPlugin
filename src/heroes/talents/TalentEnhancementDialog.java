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

import java.time.LocalDate;

import dsa41basis.hero.Spell;
import dsa41basis.hero.Talent;
import dsa41basis.util.DSAUtil;
import dsa41basis.util.HeroUtil;
import dsatool.resources.Settings;
import dsatool.ui.ReactiveSpinner;
import dsatool.util.ErrorLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class TalentEnhancementDialog {
	@FXML
	private VBox root;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label nameLabel;
	@FXML
	private Label startLabel;
	@FXML
	private ReactiveSpinner<Integer> target;
	@FXML
	private ReactiveSpinner<Integer> ses;
	@FXML
	private ComboBox<String> method;
	@FXML
	private ReactiveSpinner<Integer> ap;
	@FXML
	private HBox costBox;
	@FXML
	private ReactiveSpinner<Double> cost;

	int startValue;
	final boolean basis;

	public TalentEnhancementDialog(final Window window, final Talent talent, final JSONObject hero, final int initialTarget) {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("TalentEnhancementDialog.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final boolean includeCost = Settings.getSettingBoolOrDefault(true, "Steigerung", "Lehrmeisterkosten");

		final Stage stage = new Stage();
		stage.setTitle("Talent steigern");
		stage.setScene(new Scene(root, 250, includeCost ? 202 : 175));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(window);

		if (!includeCost) {
			costBox.setVisible(false);
			costBox.setManaged(false);
		}

		basis = talent.getTalent().getBoolOrDefault("Basis", false);

		target.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(talent, hero)));
		ses.valueProperty().addListener((o, oldV, newV) -> ap.getValueFactory().setValue(getCalculatedAP(talent, hero)));
		method.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
			ap.getValueFactory().setValue(getCalculatedAP(talent, hero));
		});
		ap.valueProperty().addListener((o, oldV, newV) -> {
			if ("Lehrmeister".equals(method.getValue())) {
				cost.getValueFactory().setValue(talent instanceof Spell ? newV * 5 : newV * 7 / 10.0);
			} else {
				cost.getValueFactory().setValue(0.0);
			}
		});

		okButton.setOnAction(event -> {
			final int usedSes = Math.min(ses.getValue(), target.getValue() - startValue);
			final JSONArray history = hero.getArr("Historie");
			final JSONObject historyEntry = new JSONObject(history);
			if (talent instanceof Spell) {
				historyEntry.put("Typ", "Zauber");
				historyEntry.put("Zauber", talent.getName());
				historyEntry.put("Repräsentation", ((Spell) talent).getRepresentation());
			} else {
				historyEntry.put("Typ", "Talent");
				historyEntry.put("Talent", talent.getName());
			}
			if (talent.getTalent().containsKey("Auswahl")) {
				historyEntry.put("Auswahl", talent.getActual().getString("Auswahl"));
			}
			if (talent.getTalent().containsKey("Freitext")) {
				historyEntry.put("Freitext", talent.getActual().getString("Freitext"));
			}
			if (talent.getValue() != Integer.MIN_VALUE) {
				historyEntry.put("Von", talent.getValue() < 0 && !basis ? talent.getValue() + 1 : talent.getValue());
			}
			if (target.getValue() != -1 || basis) {
				historyEntry.put("Auf", target.getValue() < 0 && !basis ? target.getValue() + 1 : target.getValue());
			}
			if (usedSes > 0) {
				historyEntry.put("SEs", usedSes);
			}
			historyEntry.put("Methode", method.getValue());
			historyEntry.put("AP", ap.getValue());

			if (includeCost && cost.getValue() != 0) {
				historyEntry.put("Kosten", cost.getValue());
				HeroUtil.addMoney(hero, (int) (cost.getValue() * -100));
			}

			final LocalDate currentDate = LocalDate.now();
			historyEntry.put("Datum", currentDate.toString());
			history.add(historyEntry);
			history.notifyListeners(null);

			final JSONObject bio = hero.getObj("Biografie");
			bio.put("Abenteuerpunkte-Guthaben", bio.getIntOrDefault("Abenteuerpunkte-Guthaben", 0) - ap.getValue());
			bio.notifyListeners(null);
			final int targetValue = target.getValue();
			if (targetValue < 0 && !basis) {
				if (targetValue == -1) {
					talent.setValue(Integer.MIN_VALUE);
				} else {
					talent.setValue(targetValue + 1);
				}
			} else {
				talent.setValue(targetValue);
			}
			talent.setSes(Math.max(talent.getSes() - usedSes, 0));

			stage.close();
		});

		cancelButton.setOnAction(event -> {
			stage.close();
		});

		startValue = talent.getValue();
		if (startValue == Integer.MIN_VALUE) {
			startValue = -1;
		} else if (startValue < 0 && !basis) {
			--startValue;
		}

		nameLabel.setText(talent.getName());
		startLabel.setText(startValue < 0 && !basis ? startValue == -1 ? "n.a." : Integer.toString(startValue + 1) : Integer.toString(startValue));

		target.getValueFactory().setConverter(new StringConverter<Integer>() {
			@Override
			public Integer fromString(final String string) {
				if ("n.a.".equals(string))
					return -1;
				else {
					int result = Integer.parseInt(string);
					if (result < 0 && !basis) {
						--result;
					}
					return result;
				}
			}

			@Override
			public String toString(final Integer target) {
				if (target < 0 && !basis) {
					if (target == -1)
						return "n.a.";
					else
						return Integer.toString(target + 1);
				} else
					return Integer.toString(target);
			}
		});

		((IntegerSpinnerValueFactory) target.getValueFactory()).setMin(startValue + 1);
		if (initialTarget < 0 && !basis) {
			target.getValueFactory().setValue(initialTarget - 1);
		} else {
			target.getValueFactory().setValue(initialTarget);
		}
		ses.getValueFactory().setValue(talent.getSes());
		method.setItems(FXCollections.observableArrayList("Lehrmeister", "Gegenseitiges Lehren", "Selbststudium"));
		method.getSelectionModel().select(Settings.getSettingStringOrDefault("Gegenseitiges Lehren", "Steigerung", "Lernmethode"));

		stage.show();
	}

	private int getCalculatedAP(final Talent talent, final JSONObject hero) {
		final int SELevel = startValue + Math.min(target.getValue() - startValue, ses.getValue());
		final int seCost = DSAUtil.getEnhancementCost(talent, hero, "Lehrmeister", startValue, SELevel, false);
		final int normalCost = DSAUtil.getEnhancementCost(talent, hero, method.getSelectionModel().getSelectedItem(), SELevel, target.getValue(), false);
		return seCost + normalCost;
	}
}
