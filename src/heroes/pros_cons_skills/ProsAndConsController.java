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

public class ProsAndConsController extends HeroTabController {
	@FXML
	private VBox box;
	@FXML
	private ScrollPane pane;
	@FXML
	private CheckBox showAll;

	private ProsOrConsController pros;
	private ProsOrConsController cons;

	public ProsAndConsController(final TabPane tabPane) {
		super(tabPane);
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Vor-/Nachteile";
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

		pros = new ProsOrConsController(pane, "Vorteile", "Vorteil", true, ResourceManager.getResource("data/Vorteile"), "Vorteile",
				showAll.selectedProperty());
		box.getChildren().add(pros.getControl());
		cons = new ProsOrConsController(pane, "Nachteile", "Nachteil", false, ResourceManager.getResource("data/Nachteile"), "Nachteile",
				showAll.selectedProperty());
		box.getChildren().add(cons.getControl());

		super.init();
	}

	@Override
	protected void update() {
		pros.setHero(hero);
		cons.setHero(hero);
	}
}
