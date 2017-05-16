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
package heroes.animals;

import java.util.ArrayList;
import java.util.List;

import dsatool.util.ErrorLogger;
import heroes.animals.AnimalController.AnimalType;
import heroes.ui.HeroTabController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import jsonant.event.JSONListener;
import jsonant.value.JSONArray;
import jsonant.value.JSONObject;

public class AnimalsController extends HeroTabController {
	@FXML
	private Node pane;
	@FXML
	private VBox animalsBox;

	private JSONArray animals;
	private final List<AnimalController> controllers = new ArrayList<>();

	private final JSONListener animalsListener = o -> {
		update();
	};

	public AnimalsController(TabPane tabPane) {
		super(tabPane);
	}

	public void addAnimal(String type) {
		final JSONObject animal = new JSONObject(animals);
		final JSONObject biography = new JSONObject(animal);
		biography.put("Name", type);
		biography.put("Typ", type);
		animal.put("Biografie", biography);
		animals.add(animal);
		animals.notifyListeners(null);
	}

	@Override
	protected void changeEditable() {
		for (final AnimalController controller : controllers) {
			controller.changeEditable();
		}
	}

	@Override
	protected Node getControl() {
		return pane;
	}

	@Override
	protected String getText() {
		return "Tiere";
	}

	@Override
	public void init() {
		final FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setController(this);

		try {
			fxmlLoader.load(getClass().getResource("Animals.fxml").openStream());
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}

		super.init();
	}

	@Override
	protected void update() {
		final ObservableList<Node> items = animalsBox.getChildren();
		items.remove(0, items.size() - 1);
		controllers.clear();
		if (animals != null) {
			animals.removeListener(animalsListener);
		}
		animals = hero.getArr("Tiere");
		animals.addLocalListener(animalsListener);
		for (int i = 0; i < animals.size(); ++i) {
			final JSONObject animal = animals.getObj(i);
			AnimalController newController;
			switch (animal.getObj("Biografie").getStringOrDefault("Typ", "Tier")) {
			case "Reittier":
				newController = new AnimalController(hero, animal, AnimalType.HORSE);
				break;
			case "Vertrautentier":
				newController = new AnimalController(hero, animal, AnimalType.MAGIC);
				break;
			case "Tier":
			default:
				newController = new AnimalController(hero, animal, AnimalType.ANIMAL);
				break;
			}
			items.add(i, newController.getControl());
			newController.changeEditable();
			controllers.add(newController);
		}

		if (controllers.size() > 1) {
			for (final Node item : items) {
				if (item instanceof TitledPane) {
					((TitledPane) item).setExpanded(false);
				}
			}
		}
	}

}
