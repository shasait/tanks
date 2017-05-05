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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

import de.hasait.tanks.util.common.AbstractConfiguredAction;

/**
 *
 */
public class GdxInputKeyPressedAction extends AbstractConfiguredAction {

	private final int _keycode;

	private final InputProcessor _inputProcessor = new InputAdapter() {

		@Override
		public boolean keyDown(final int pKeycode) {
			if (_keycode == pKeycode) {
				updateState(1.0f);
				return true;
			}
			return super.keyDown(pKeycode);
		}

		@Override
		public boolean keyUp(final int pKeycode) {
			if (_keycode == pKeycode) {
				updateState(0.0f);
				return true;
			}
			return super.keyUp(pKeycode);
		}

	};

	public GdxInputKeyPressedAction(final int pKeycode) {
		_keycode = pKeycode;
	}

	@Override
	public String toString() {
		return Input.Keys.toString(_keycode);
	}

	@Override
	protected void disposeInternal() {
		// nop
	}

	@Override
	protected void initInternal() {
		addInputProcessor(_inputProcessor);
	}

	private Object writeReplace() throws ObjectStreamException {
		final GdxInputKeyPressedActionSerialized serialized = new GdxInputKeyPressedActionSerialized();
		serialized._keycode = _keycode;
		return serialized;
	}

}
