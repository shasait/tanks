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

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/**
 *
 */
public abstract class AbstractScreenContext implements Disposable {

	private final Game<?> _game;

	private final int _viewportW, _viewportH;
	private final Rectangle _viewportR;

	private final SpriteBatch _batch;
	private final BitmapFont _font;

	protected AbstractScreenContext(final Game<?> pGame, final int pViewportW, final int pViewportH) {
		super();

		_game = pGame;

		_viewportW = pViewportW;
		_viewportH = pViewportH;
		_viewportR = new Rectangle(0, 0, _viewportW, _viewportH);

		_batch = new SpriteBatch();
		_font = new BitmapFont();
	}

	@Override
	public void dispose() {
		_font.dispose();
		_batch.dispose();
	}

	public final int getViewportH() {
		return _viewportH;
	}

	public final int getViewportW() {
		return _viewportW;
	}

	public final boolean viewportContains(final float pX, final float pY) {
		return _viewportR.contains(pX, pY);
	}

	SpriteBatch getBatch() {
		return _batch;
	}

	BitmapFont getFont() {
		return _font;
	}

	Game<?> getGame() {
		return _game;
	}

}