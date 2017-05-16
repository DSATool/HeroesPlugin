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
package heroes.pros_cons_skills;

import java.util.ArrayList;
import java.util.List;

import dsatool.resources.ResourceManager;
import dsatool.util.ErrorLogger;
import heroes.ui.HeroTabController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import jsonant.value.JSONObject;

public class SpecialSkillsController extends HeroTabController {
	@FXML
	private VBox box;
	@FXML
	private ScrollPane pane;
	@FXML
	private CheckBox showAll;

	private final List<ProsOrConsController> skillControllers = new ArrayList<>();

	public SpecialSkillsController(TabPane tabPane) {
		super(tabPane);
	}

	@Override
	protected void changeEditable() {
		for (final ProsOrConsController controller : skillControllers) {
			controller.changeEditable();
		}
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Sonderfertigkeiten";
	}

	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("ProsAndCons.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject specialSkills = ResourceManager.getResource("data/Sonderfertigkeiten");
		for (final String skillGroup : specialSkills.keySet()) {
			final ProsOrConsController groupController = new ProsOrConsController(pane, skillGroup, "Sonderfertikgeit", true, false,
					specialSkills.getObj(skillGroup), "Sonderfertigkeiten", showAll.selectedProperty());
			skillControllers.add(groupController);
			box.getChildren().add(groupController.getControl());
		}
		final JSONObject rituals = ResourceManager.getResource("data/Rituale");
		for (final String skillGroup : rituals.keySet()) {
			final ProsOrConsController groupController = new ProsOrConsController(pane, skillGroup, "Sonderfertikgeit", true, false, rituals.getObj(skillGroup),
					"Sonderfertigkeiten", showAll.selectedProperty());
			skillControllers.add(groupController);
			box.getChildren().add(groupController.getControl());
		}
		ProsOrConsController groupController = new ProsOrConsController(pane, "Liturgien", "Sonderfertigkeit", false, false,
				ResourceManager.getResource("data/Liturgien"), "Sonderfertigkeiten", showAll.selectedProperty());
		skillControllers.add(groupController);
		box.getChildren().add(groupController.getControl());
		groupController = new ProsOrConsController(pane, "Schamanen-Rituale", "Sonderfertigkeit", false, false,
				ResourceManager.getResource("data/Schamanenrituale"), "Sonderfertigkeiten", showAll.selectedProperty());
		skillControllers.add(groupController);
		box.getChildren().add(groupController.getControl());
		groupController = new CheaperSkillsController(pane);
		skillControllers.add(groupController);
		box.getChildren().add(groupController.getControl());

		super.init();
	}

	@Override
	protected void update() {
		for (final ProsOrConsController controller : skillControllers) {
			controller.setHero(hero);
		}
	}
}