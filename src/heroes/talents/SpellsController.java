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

public class SpellsController extends HeroTabController {
	@FXML
	private ScrollPane pane;
	@FXML
	private VBox talentsBox;

	private final TabPane tabPane;
	private TalentGroupController spellController;
	private final JSONListener listener;

	public SpellsController(TabPane tabPane) {
		super(tabPane);
		this.tabPane = tabPane;
		listener = o -> setTab();
	}

	@Override
	protected void changeEditable() {
		spellController.changeEditable();
	}

	private void createPane() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Talents.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		final JSONObject talents = ResourceManager.getResource("data/Zauber");
		spellController = new TalentGroupController(pane, "Zauber", null, talents);
		talentsBox.getChildren().add(spellController.getControl());
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Zauber";
	}

	@Override
	public void init() {
		createPane();
		setTab();
		super.init();
	}

	@Override
	public void setHero(JSONObject hero) {
		if (this.hero != null) {
			this.hero.getObj("Vorteile").removeListener(listener);
		}
		super.setHero(hero);
		setTab();
	}

	private void setTab() {
		if (HeroUtil.isMagical(hero)) {
			if (!tabPane.getTabs().contains(tab)) {
				tab.setContent(getControl());
				tab.setClosable(false);
				tabPane.getTabs().add(4, tab);
			}
		} else {
			tabPane.getTabs().remove(tab);
		}
	}

	@Override
	protected void update() {
		setTab();
		if (spellController != null) {
			spellController.setHero(hero);
		}
		hero.getObj("Vorteile").addListener(listener);
	}

}
