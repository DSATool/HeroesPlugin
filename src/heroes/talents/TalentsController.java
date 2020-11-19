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

import java.util.ArrayList;
import java.util.List;

import dsa41basis.util.HeroUtil;
import dsatool.resources.ResourceManager;
import dsatool.util.ErrorLogger;
import heroes.ui.HeroTabController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import jsonant.event.JSONListener;
import jsonant.value.JSONObject;

public class TalentsController extends HeroTabController {
	@FXML
	private ScrollPane pane;
	@FXML
	private VBox talentsBox;

	private final List<TalentGroupController> talentControllers = new ArrayList<>();

	private Node ritualKnowledge;
	private Node liturgyKnowledge;

	private final JSONListener listener = o -> updateVisibility();

	public TalentsController(final TabPane tabPane) {
		super(tabPane);
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Talente";
	}

	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Talents.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject talentGroups = ResourceManager.getResource("data/Talentgruppen");
		final JSONObject talents = ResourceManager.getResource("data/Talente");
		for (final String talentGroup : talents.keySet()) {
			if ("Meta-Talente".equals(talentGroup)) {
				continue;
			}
			final TalentGroupController groupController = new TalentGroupController(pane, talentGroup, talentGroups.getObj(talentGroup),
					talents.getObj(talentGroup));
			talentControllers.add(groupController);
			switch (talentGroup) {
				case "Ritualkenntnis" -> ritualKnowledge = groupController.getControl();
				case "Liturgiekenntnis" -> liturgyKnowledge = groupController.getControl();
			}
			talentsBox.getChildren().add(groupController.getControl());
		}

		super.init();
	}

	@Override
	protected void registerListeners() {
		hero.getObj("Vorteile").addListener(listener);
		hero.getObj("Sonderfertigkeiten").addListener(listener);
		for (final TalentGroupController controller : talentControllers) {
			controller.registerListeners();
		}
	}

	@Override
	protected void unregisterListeners() {
		hero.getObj("Vorteile").removeListener(listener);
		hero.getObj("Sonderfertigkeiten").removeListener(listener);
		for (final TalentGroupController controller : talentControllers) {
			controller.unregisterListeners();
		}
	}

	@Override
	protected void update() {
		for (final TalentGroupController controller : talentControllers) {
			controller.setHero(hero);
		}
		if (pane != null) {
			updateVisibility();
		}
	}

	private void updateVisibility() {
		if (HeroUtil.isMagical(hero)) {
			if (!talentsBox.getChildren().contains(ritualKnowledge)) {
				talentsBox.getChildren().add(ritualKnowledge);
			}
		} else {
			talentsBox.getChildren().remove(ritualKnowledge);
		}
		if (HeroUtil.isClerical(hero, false)) {
			if (!talentsBox.getChildren().contains(liturgyKnowledge)) {
				talentsBox.getChildren().add(liturgyKnowledge);
			}
		} else {
			talentsBox.getChildren().remove(liturgyKnowledge);
		}
	}

}
