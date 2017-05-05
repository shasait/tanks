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

package de.hasait.tanks.util.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

import de.hasait.tanks.util.common.input.ConfiguredAction;

/**
 *
 */
public abstract class AbstractConfiguredAction implements ConfiguredAction {

	private final AtomicBoolean _initialized = new AtomicBoolean();

	private final AtomicReference<Float> _state = new AtomicReference<>(0.0f);

	private final List<InputProcessor> _inputProcessors = new CopyOnWriteArrayList<>();

	private InputMultiplexer _inputMultiplexer;


	@Override
	public final void dispose() {
		if (!_initialized.compareAndSet(true, false)) {
			throw new IllegalStateException("Not initialized");
		}

		_inputProcessors.forEach(_inputMultiplexer::removeProcessor);

		disposeInternal();
		_inputMultiplexer = null;
	}

	@Override
	public final float getState() {
		return _state.get();
	}

	@Override
	public final void init(final Abstract2DScreen<?> pScreen) {
		if (!_initialized.compareAndSet(false, true)) {
			throw new IllegalStateException("Already initialized");
		}

		_inputMultiplexer = pScreen.getInputMultiplexer();
		pScreen.addDisposable(this);

		initInternal();
	}

	protected final void addInputProcessor(final InputProcessor pInputProcessor) {
		_inputProcessors.add(pInputProcessor);
		_inputMultiplexer.addProcessor(pInputProcessor);
	}

	protected abstract void disposeInternal();

	protected abstract void initInternal();

	protected final void updateState(final float pState) {
		_state.set(pState);
	}

}
