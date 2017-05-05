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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

/**
 *
 */
public class ConfiguredActionFactory {

	private final AtomicBoolean _initialized = new AtomicBoolean();
	private final AtomicBoolean _finished = new AtomicBoolean();
	private final AtomicBoolean _actionSet = new AtomicBoolean();

	private final ControllerListener _controllerListener = new ControllerAdapter() {

		@Override
		public boolean axisMoved(final Controller pController, final int pAxisIndex, final float pValue) {
			if (pValue < 0.4f && pValue > -0.4f) {
				return false;
			}
			updateAction(new ControllerAxisMoveAction(pController.getName(), pAxisIndex, pValue > 0.0f));
			return true;
		}

		@Override
		public boolean buttonUp(final Controller pController, final int pButtonIndex) {
			updateAction(new ControllerButtonAction(pController.getName(), pButtonIndex));
			return true;
		}

	};

	private final InputProcessor _inputProcessor = new InputAdapter() {
		@Override
		public boolean keyUp(final int pKeycode) {
			if (pKeycode == Input.Keys.ESCAPE) {
				updateAction(null);
			}
			updateAction(new GdxInputKeyPressedAction(pKeycode));
			return true;
		}
	};

	private Consumer<ConfiguredAction> _actionConsumer;

	private InputProcessor _previousInputProcessor;

	public void init(final Consumer<ConfiguredAction> pActionConsumer) {
		if (!_initialized.compareAndSet(false, true)) {
			throw new IllegalStateException("Already initialized");
		}

		_previousInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(_inputProcessor);

		_actionConsumer = pActionConsumer;
		Controllers.addListener(_controllerListener);
	}

	public boolean isFinished() {
		return _finished.get();
	}

	private void updateAction(final ConfiguredAction pAction) {
		if (_actionSet.compareAndSet(false, true)) {
			Controllers.removeListener(_controllerListener);
			Gdx.input.setInputProcessor(_previousInputProcessor);
			if (pAction != null) {
				_actionConsumer.accept(pAction);
			}
			_finished.set(true);
		}
	}

}
