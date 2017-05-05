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
public class ControllerButtonAction extends AbstractConfiguredAction {

	private final String _controllerName;
	private final int _buttonIndex;

	private final ControllerListener _listener = new ControllerAdapter() {

		@Override
		public boolean buttonDown(final Controller pController, final int pButtonIndex) {
			if (_controllerName.equals(pController.getName()) && _buttonIndex == pButtonIndex) {
				updateState(1.0f);
				return true;
			}
			return super.buttonDown(pController, pButtonIndex);
		}

		@Override
		public boolean buttonUp(final Controller pController, final int pButtonIndex) {
			if (_controllerName.equals(pController.getName()) && _buttonIndex == pButtonIndex) {
				updateState(0.0f);
				return true;
			}
			return super.buttonUp(pController, pButtonIndex);
		}

	};

	public ControllerButtonAction(final String pControllerName, final int pButtonIndex) {
		super();

		_controllerName = pControllerName;
		_buttonIndex = pButtonIndex;
	}

	@Override
	public String toString() {
		return  "Button " + _buttonIndex;
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
		final ControllerButtonActionSerialized serialized = new ControllerButtonActionSerialized();
		serialized._controllerName = _controllerName;
		serialized._buttonIndex = _buttonIndex;
		return serialized;
	}

}
