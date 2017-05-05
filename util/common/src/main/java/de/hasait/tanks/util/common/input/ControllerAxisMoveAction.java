/*
 * Copyright (C) 2017 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.tanks.util.common.input;

import java.io.ObjectStreamException;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

import de.hasait.tanks.util.common.AbstractConfiguredAction;

/**
 *
 */
public class ControllerAxisMoveAction extends AbstractConfiguredAction {

	private final String _controllerName;
	private final int _axisIndex;
	private final boolean _positive;

	private final ControllerListener _listener = new ControllerAdapter() {

		@Override
		public boolean axisMoved(final Controller pController, final int pAxisIndex, final float pValue) {
			if (_controllerName.equals(pController.getName()) && _axisIndex == pAxisIndex) {
				if (_positive && pValue > 0.1f) {
					updateState(pValue);
					return true;
				}
				if (!_positive && pValue < -0.1f) {
					updateState(-pValue);
					return true;
				}
			}

			return super.axisMoved(pController, pAxisIndex, pValue);
		}

	};

	public ControllerAxisMoveAction(final String pControllerName, final int pAxisIndex, final boolean pPositive) {
		super();

		_controllerName = pControllerName;
		_axisIndex = pAxisIndex;
		_positive = pPositive;
	}

	@Override
	public String toString() {
		return (_positive ? "+" : "-") + "Axis " + _axisIndex;
	}

	@Override
	protected void disposeInternal() {
		Controllers.removeListener(_listener);
	}

	@Override
	protected void initInternal() {
		Controllers.addListener(_listener);
	}

	private Object writeReplace() throws ObjectStreamException {
		final ControllerAxisMoveActionSerialized serialized = new ControllerAxisMoveActionSerialized();
		serialized._controllerName = _controllerName;
		serialized._axisIndex = _axisIndex;
		serialized._positive = _positive;
		return serialized;
	}

}
