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

import java.util.function.Function;

import com.badlogic.gdx.Screen;

/**
 *
 */
public class Game<C extends AbstractScreenContext> extends com.badlogic.gdx.Game {

	private final Function<Game<C>, C> _contextFactory;
	private final Function<C, Screen> _initialScreenFactory;

	public Game(final Function<Game<C>, C> pContextFactory, final Function<C, Screen> pInitialScreenFactory) {
		super();

		_contextFactory = pContextFactory;
		_initialScreenFactory = pInitialScreenFactory;
	}


	@Override
	public final void create() {
		final C context = _contextFactory.apply(this);
		final Screen initialScreen = _initialScreenFactory.apply(context);
		setScreen(initialScreen);
	}

}
