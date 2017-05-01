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


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;

/**
 *
 */
public class MainMenuScreen extends Abstract2DScreen<TanksScreenContext> {


	private final TextField _player1NameField;
	private final TextField _player2NameField;
	private final TextField _roomNameField;
	private final TextButton _connectButton;
	private String _player1Name;
	private String _player2Name;
	private String _roomName;
	private boolean _connect;

	public MainMenuScreen(final TanksScreenContext pContext) {
		super(pContext);

		setBackgroundColor(new Color(0.0f, 0.0f, 0.2f, 1.0f));

		final Label titleLabel = createLabel("Welcome to Tanks", 2.0f);
		_player1NameField = createTextField("Player1");
		_player2NameField = createTextField("");
		_roomNameField = createTextField("Room1");
		_connectButton = createTextButton("Connect");

		final Table layout = addLayout();
		layout.setFillParent(true);
		layout.defaults().pad(5.0f);

		layout.add(titleLabel).colspan(2).padBottom(20.0f);
		layout.row();
		layout.add(createLabel("Player 1"));
		layout.add(_player1NameField);
		layout.row();
		layout.add(createLabel("Room"));
		layout.add(_roomNameField);
		layout.row();
		layout.add(createLabel("Player 2"));
		layout.add(_player2NameField);
		layout.row();
		layout.add(_connectButton).colspan(2);

		_connectButton.addListener(pEvent -> {
			if (pEvent instanceof ChangeListener.ChangeEvent) {
				_connect = true;
			}
			return false;
		});
	}

	public String getPlayer1Name() {
		return _player1Name;
	}

	public String getPlayer2Name() {
		return _player2Name;
	}

	public String getRoomName() {
		return _roomName;
	}

	@Override
	protected void renderInternal(final float pDelta) {
		if (_connect) {
			_connect = false;
			_player1Name = _player1NameField.getText();
			_player2Name = _player2NameField.getText();
			_player2Name = Util.isBlank(_player2Name) ? null : _player2Name;
			_roomName = _roomNameField.getText();
			if (!Util.isBlank(_player1Name) && !Util.isBlank(_roomName)) {
				setScreen(new GameScreen(this));
			}
		}
	}

}
