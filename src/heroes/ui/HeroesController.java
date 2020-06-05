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
package heroes.ui;

import java.util.ArrayList;
import java.util.List;

import dsa41basis.ui.hero.HeroSelector;
import dsatool.util.ErrorLogger;
import heroes.animals.AnimalsController;
import heroes.fight.FightController;
import heroes.general.GeneralController;
import heroes.inventory.InventoryController;
import heroes.notes.NotesController;
import heroes.pros_cons_skills.ProsAndConsController;
import heroes.pros_cons_skills.SpecialSkillsController;
import heroes.talents.SpellsController;
import heroes.talents.TalentsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

public class HeroesController extends HeroSelector {
	public static List<Class<? extends HeroTabController>> tabControllers = new ArrayList<>(List.of(GeneralController.class, ProsAndConsController.class,
			SpecialSkillsController.class, TalentsController.class, SpellsController.class, FightController.class, InventoryController.class,
			AnimalsController.class, NotesController.class));

	@FXML
	private StackPane stackPane;
	@FXML
	private TabPane tabPane;

	public HeroesController() {
		super(true);

		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("HeroesController.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		stackPane.getStylesheets().add(getClass().getResource("heroes.css").toExternalForm());

		setContent(stackPane);

		try {
			for (final Class<? extends HeroTabController> controller : tabControllers) {
				controllers.add(controller.getDeclaredConstructor(TabPane.class).newInstance(tabPane));
			}
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		load();
	}

	@Override
	public void load() {
		super.load();

		final HeroTabController firstPage = (HeroTabController) controllers.get(0);
		firstPage.init();
		firstPage.update();
		tabPane.getTabs().get(0).setContent(firstPage.getControl());
	}

	@FXML
	private void toggleEditable() {
		HeroTabController.isEditable.set(!HeroTabController.isEditable.get());
	}
}
