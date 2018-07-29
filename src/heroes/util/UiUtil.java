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
package heroes.util;

import javafx.scene.control.TableCell;
import javafx.util.Callback;

public class UiUtil {

	@SuppressWarnings("rawtypes")
	public static final Callback integerCellFactory = col -> {
		return new TableCell() {
			@Override
			protected void updateItem(final Object item, final boolean empty) {
				if (empty || item.equals(Integer.MIN_VALUE) || item.equals(Double.NEGATIVE_INFINITY)) {
					setText("");
				} else {
					if (item instanceof Integer || item instanceof Long) {
						setText(item.toString());
					} else if (item instanceof Double) {
						setText(Integer.toString(((Double) item).intValue()));
					}
				}
			}
		};
	};

	@SuppressWarnings("rawtypes")
	public static final Callback signedIntegerCellFactory = col -> {
		return new TableCell() {
			@Override
			protected void updateItem(final Object item, final boolean empty) {
				if (empty || item.equals(Integer.MIN_VALUE)) {
					setText("");
				} else {
					setText(((Integer) item > 0 ? "+" : "") + item.toString());
				}
			}
		};
	};

	private UiUtil() {}

}
