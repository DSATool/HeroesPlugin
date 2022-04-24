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

import dsa41basis.ui.hero.BasicValuesController.CharacterType;
import dsatool.util.ErrorLogger;
import heroes.ui.HeroTabController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

	public AnimalsController(final TabPane tabPane) {
		super(tabPane);
	}

	public void addAnimal(final ActionEvent event) {
		final String type = switch (((Button) event.getSource()).getText().charAt(0)) {
			case 'V' -> "Vertrautentier";
			case 'R' -> "Reittier";
			default -> "Tier";
		};
		final JSONObject animal = new JSONObject(animals);
		final JSONObject biography = new JSONObject(animal);
		biography.put("Name", type);
		biography.put("Typ", type);
		animal.put("Biografie", biography);
		animals.add(animal);
		animals.notifyListeners(null);
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
	protected void registerListeners() {
		animals.addLocalListener(animalsListener);
	}

	@Override
	protected void unregisterListeners() {
		animals.removeListener(animalsListener);
		controllers.clear();
	}

	@Override
	protected void update() {
		final ObservableList<Node> items = animalsBox.getChildren();
		items.remove(0, items.size() - 1);
		for (final AnimalController controller : controllers) {
			controller.unregisterListeners();
		}
		controllers.clear();
		animals = hero.getArr("Tiere");
		for (int i = 0; i < animals.size(); ++i) {
			final JSONObject animal = animals.getObj(i);
			final AnimalController newController = switch (animal.getObj("Biografie").getStringOrDefault("Typ", "Tier")) {
				case "Reittier" -> new AnimalController(animal, CharacterType.HORSE);
				case "Vertrautentier" -> new AnimalController(animal, CharacterType.MAGIC_ANIMAL);
				default -> new AnimalController(animal, CharacterType.ANIMAL);
			};
			items.add(i, newController.getControl());
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
