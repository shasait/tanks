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


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.hasait.tanks.app.common.model.GameConfig;
import de.hasait.tanks.app.common.model.PlayerConfig;
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;

/**
 *
 */
public class MainMenuScreen extends Abstract2DScreen<TanksScreenContext> {

	private final TextField _roomNameField;
	private final List<TextField> _playerNameFields = new ArrayList<>();
	private final TextButton _connectButton;
	private boolean _connect;

	public MainMenuScreen(final TanksScreenContext pContext) {
		super(pContext, 800, 600);

		setBackgroundColor(new Color(0.0f, 0.0f, 0.2f, 1.0f));

		final Label titleLabel = createLabel("Welcome to Tanks", 2.0f);
		_roomNameField = createTextField("Default");
		for (int i = 0; i < 2; i++) {
			_playerNameFields.add(createTextField());
		}
		_connectButton = createTextButton("Connect");

		final Table layout = addLayout();
		layout.setFillParent(true);
		layout.defaults().pad(5.0f);

		layout.add(titleLabel).colspan(2).padBottom(20.0f);
		layout.row();
		layout.add(createLabel("Room"));
		layout.add(_roomNameField);
		layout.row();
		for (int i = 0; i < _playerNameFields.size(); i++) {
			layout.add(createLabel("Player " + (i + 1)));
			layout.add(_playerNameFields.get(i));
			layout.row();
		}
		layout.add(_connectButton).colspan(2);

		_connectButton.addListener(pEvent -> {
			if (pEvent instanceof ChangeListener.ChangeEvent) {
				_connect = true;
			}
			return false;
		});
	}

	@Override
	protected void renderInternal(final float pDelta) {
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		if (_connect) {
			_connect = false;

			final String roomName = _roomNameField.getText();
			if (Util.isBlank(roomName)) {
				return;
			}

			final GameConfig config = new GameConfig(roomName, 40, 24);
			for (int i = 0; i < _playerNameFields.size(); i++) {
				final String playerName = _playerNameFields.get(i).getText();
				if (Util.isBlank(playerName)) {
					continue;
				}
				final PlayerConfig playerConfig = new PlayerConfig(playerName.trim());
				// TODO make actions configurable
				if (i == 0) {
					setActionSet1(playerConfig);
				}
				if (i == 1) {
					setActionSet2(playerConfig);
				}

				config.getPlayers().add(playerConfig);
			}

			if (config.getPlayers().isEmpty()) {
				return;
			}

			setScreen(new ConnectingScreen(getContext(), config));
		}
	}

	private void setActionSet1(final PlayerConfig pPlayerConfig) {
		pPlayerConfig.setMoveForward(new GdxInputKeyPressedAction(Input.Keys.W));
		pPlayerConfig.setMoveBackward(new GdxInputKeyPressedAction(Input.Keys.S));
		pPlayerConfig.setRotateLeft(new GdxInputKeyPressedAction(Input.Keys.A));
		pPlayerConfig.setRotateRight(new GdxInputKeyPressedAction(Input.Keys.D));
		pPlayerConfig.setTurrentRotateLeft(new GdxInputKeyPressedAction(Input.Keys.Q));
		pPlayerConfig.setTurrentRotateRight(new GdxInputKeyPressedAction(Input.Keys.E));
		pPlayerConfig.setFire(new GdxInputKeyPressedAction(Input.Keys.SPACE));
	}

	private void setActionSet2(final PlayerConfig pPlayerConfig) {
		pPlayerConfig.setMoveForward(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_5));
		pPlayerConfig.setMoveBackward(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_2));
		pPlayerConfig.setRotateLeft(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_1));
		pPlayerConfig.setRotateRight(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_3));
		pPlayerConfig.setTurrentRotateLeft(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_4));
		pPlayerConfig.setTurrentRotateRight(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_6));
		pPlayerConfig.setFire(new GdxInputKeyPressedAction(Input.Keys.NUMPAD_0));
	}

}
