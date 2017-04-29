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


import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.jgroups.JChannel;

/**
 *
 */
public class Tanks extends Game {

	public SpriteBatch _batch;
	public BitmapFont _font;

	public String _playerName;
	public String _roomName;

	public void create() {
		_batch = new SpriteBatch();
		_font = new BitmapFont();

		setScreen(new MainMenuScreen(this));
	}

	public void dispose() {
		_batch.dispose();
		_font.dispose();
	}

	public void render() {
		super.render();
	}

}
