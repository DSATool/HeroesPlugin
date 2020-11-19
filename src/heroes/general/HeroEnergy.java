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
package heroes.general;

import dsatool.ui.Colorable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import jsonant.value.JSONObject;

public class HeroEnergy extends dsa41basis.hero.Energy implements Colorable {

	private final Color color;
	private final StringProperty text = new SimpleStringProperty();

	public HeroEnergy(final String name, final JSONObject derivation, final JSONObject hero, final Color color) {
		super(name, derivation, hero);

		text.bind(Bindings.when(current.isEqualTo(max)).then(max.asString()).otherwise(current.asString().concat('/').concat(max)));

		this.color = color;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public final String getText() {
		return text.get();
	}

	@Override
	public final ReadOnlyStringProperty textProperty() {
		return text;
	}
}
