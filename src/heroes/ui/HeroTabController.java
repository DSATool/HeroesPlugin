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

import dsa41basis.ui.hero.HeroController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import jsonant.value.JSONObject;

public abstract class HeroTabController implements HeroController {

	public static BooleanProperty isEditable = new SimpleBooleanProperty(false);
	protected JSONObject hero;

	protected Tab tab;

	private boolean stopInit = true;

	public HeroTabController(TabPane pane) {
		tab = new Tab(getText());
		tab.setClosable(false);
		tab.setOnSelectionChanged(e -> {
			if (tab.isSelected() && getControl() == null && !stopInit) {
				init();
				update();
				changeEditable();
				tab.setContent(getControl());
			}
		});
		pane.getTabs().add(tab);
		stopInit = false;
	}

	protected abstract void changeEditable();

	protected abstract Node getControl();

	protected abstract String getText();

	protected void init() {
		isEditable.addListener((o, oldV, newV) -> changeEditable());
	}

	@Override
	public void setHero(JSONObject hero) {
		this.hero = hero;
		if (getControl() != null) {
			update();
		}
	}

	protected abstract void update();
}
