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

package de.hasait.tanks.app.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 *
 */
public class MainMenuScreen implements Screen {

	private final Tanks _game;

	private OrthographicCamera _camera;

	public MainMenuScreen(final Tanks pGame) {
		_game = pGame;

		_camera = new OrthographicCamera();
		_camera.setToOrtho(false, 800, 480);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void render(final float pDelta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		_camera.update();
		_game._batch.setProjectionMatrix(_camera.combined);

		_game._batch.begin();
		_game._font.draw(_game._batch, "Welcome to Tanks", 300, 250);
		_game._font.draw(_game._batch, "Tap anywhere to begin!", 290, 200);
		_game._batch.end();

		if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.ENTER)) {
			_game._playerName = "Player";
			_game._roomName = "Room1";
			_game.setScreen(new GameScreen(_game));
			dispose();
		}
	}

	@Override
	public void resize(final int pWidth, final int pHeight) {
	}

	@Override
	public void resume() {
	}

	@Override
	public void show() {
	}

}
