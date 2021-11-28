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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import de.hasait.tanks.app.common.model.GameConfig;
import de.hasait.tanks.app.common.model.PlayerConfig;
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;
import de.hasait.tanks.util.common.input.ConfiguredAction;
import de.hasait.tanks.util.common.input.ConfiguredActionFactory;
import de.hasait.tanks.util.common.input.GdxInputKeyPressedAction;

/**
 *
 */
public class MainMenuScreen extends Abstract2DScreen<TanksScreenContext> {

	private static final String PREFKEY__ROOM_NAME = "roomName";
	private static final String PREFKEY__PLAYER_CONFIG = "playerConfig";
	private static final String PREFKEY__NET_STACK = "netStack";
	private static final String PREFKEY__NET_OPTIONS = "netOptions";

	private final InputProcessor _inputProcessor = new InputAdapter() {

		@Override
		public boolean keyUp(final int keycode) {
			if (keycode == Input.Keys.ESCAPE) {
				Gdx.app.exit();
			}
			return super.keyUp(keycode);
		}


	};

	private final AtomicReference<ConfiguredActionFactory> _configuredActionFactory = new AtomicReference<>();

	private final TextField _roomNameField;
	private final TextField _netStackField;
	private final TextField _netOptionsField;
	private final List<TextField> _playerNameFields = new ArrayList<>();
	private final TextButton _connectButton;
	private boolean _connect;

	public MainMenuScreen(final TanksScreenContext pContext) {
		super(pContext, 800, 600);

		setBackgroundColor(new Color(0.0f, 0.0f, 0.2f, 1.0f));

		addInputProcessor(_inputProcessor);

		final Table layout = addLayout();
		layout.setFillParent(true);
		layout.defaults().pad(5.0f).align(Align.left).fill();

		final Label titleLabel = createLabel("Welcome to Tanks", 2.0f);
		layout.add(titleLabel).colspan(2).padBottom(20.0f);

		final Preferences preferences = obtainPreferences();

		layout.row();
		layout.add(createLabel("Room"));
		_roomNameField = createTextField(preferences.getString(PREFKEY__ROOM_NAME, "Default"));
		layout.add(_roomNameField);

		layout.row();
		layout.add(createLabel("Network"));
		_netStackField = createTextField(preferences.getString(PREFKEY__NET_STACK, "udp"));
		layout.add(_netStackField);
		_netOptionsField = createTextField(preferences.getString(PREFKEY__NET_OPTIONS, "localhost[7800]"));
		layout.add(_netOptionsField);

		for (int i = 0; i < 2; i++) {
			layout.row();
			layout.add(createLabel("Player " + (i + 1)));
			final TextField playerNameField = createTextField();
			layout.add(playerNameField);
			_playerNameFields.add(playerNameField);
			PlayerConfig playerConfig = null;
			final String preferencesString = preferences.getString(PREFKEY__PLAYER_CONFIG + i);
			if (!Util.isEmpty(preferencesString)) {
				try {
					playerConfig = (PlayerConfig) Util.serializeFromString(preferencesString);
					Gdx.app.log("PlayerConfig", "Read for player " + i);
				} catch (final RuntimeException pE) {
					Gdx.app.error("PlayerConfig", "Cannot read for player " + i, pE);
				}
			}
			if (playerConfig == null) {
				Gdx.app.log("PlayerConfig", "New for player " + i);
				playerConfig = new PlayerConfig();
				if (i == 0) {
					playerConfig.setName("Player " + (i + 1));
					setActionSet1(playerConfig);
				} else if (i == 1) {
					setActionSet2(playerConfig);
				}
			}
			playerNameField.setText(playerConfig.getName());
			playerNameField.setUserObject(playerConfig);


			layout.add(actionConfig("Fire", playerConfig::getFire, playerConfig::setFire));
			layout.row();
			layout.add(createLabel(Util.EMPTY));
			layout.add(actionConfig("Move Forward", playerConfig::getMoveForward, playerConfig::setMoveForward));
			layout.add(actionConfig("Move Backward", playerConfig::getMoveBackward, playerConfig::setMoveBackward));
			layout.row();
			layout.add(createLabel(Util.EMPTY));
			layout.add(actionConfig("Rotate Left", playerConfig::getRotateLeft, playerConfig::setRotateLeft));
			layout.add(actionConfig("Rotate Right", playerConfig::getRotateRight, playerConfig::setRotateRight));
			layout.row();
			layout.add(createLabel(Util.EMPTY));
			layout.add(actionConfig("Turret Left", playerConfig::getTurrentRotateLeft, playerConfig::setTurrentRotateLeft));
			layout.add(actionConfig("Turret Right", playerConfig::getTurrentRotateRight, playerConfig::setTurrentRotateRight));
		}

		layout.row();
		_connectButton = createTextButton("Connect");
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
		final ConfiguredActionFactory configuredActionFactory = _configuredActionFactory.get();
		if (configuredActionFactory != null) {
			drawText(0, ">>> Waiting for action <<<", AlignH.CENTER, AlignV.TOP);
			if (configuredActionFactory.isFinished()) {
				_configuredActionFactory.set(null);
			}
		}

		if (_connect) {
			_connect = false;

			final GameConfig config = new GameConfig();

			final String roomName = _roomNameField.getText();
			if (Util.isBlank(roomName)) {
				return;
			}

			config.setRoomName(roomName);

			final String netStack = _netStackField.getText();
			final String netOptions = _netOptionsField.getText();
			if ("udp".equals(netStack)) {

			} else if ("tcp".equals(netStack)) {
				config.putNetworkSystemProperty("jgroups.tcpping.initial_hosts", netOptions);
			} else {
				return;
			}

			final Preferences preferences = obtainPreferences();
			preferences.putString(PREFKEY__ROOM_NAME, roomName);
			preferences.putString(PREFKEY__NET_STACK, netStack);
			preferences.putString(PREFKEY__NET_OPTIONS, netOptions);

			config.setNetworkStack(netStack);
			config.setWishPiecesX(40);
			config.setWishPiecesY(24);

			for (int i = 0; i < _playerNameFields.size(); i++) {
				final TextField playerNameField = _playerNameFields.get(i);
				final String playerName = playerNameField.getText();
				if (Util.isBlank(playerName)) {
					continue;
				}

				final PlayerConfig playerConfig = (PlayerConfig) playerNameField.getUserObject();
				playerConfig.setName(playerName.trim());
				config.getPlayers().add(playerConfig);

				preferences.putString(PREFKEY__PLAYER_CONFIG + i, Util.serializeToString(playerConfig));
			}

			if (config.getPlayers().isEmpty()) {
				return;
			}

			preferences.flush();

			setScreen(new ConnectingScreen(getContext(), config));
		}
	}

	private Label actionConfig(final String pTitle, final Supplier<ConfiguredAction> pActionSupplier,
			final Consumer<ConfiguredAction> pActionConsumer) {
		final Label label = createLabel(pTitle);
		final Consumer<ConfiguredAction> actionConsumer = pAction -> {
			label.setText(pTitle + ": " + pAction);
			pActionConsumer.accept(pAction);
		};
		actionConsumer.accept(pActionSupplier.get());
		label.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent pEvent, final float pX, final float pY) {
				if (_configuredActionFactory.compareAndSet(null, new ConfiguredActionFactory())) {
					_configuredActionFactory.get().init(actionConsumer);
				}
			}
		});
		return label;
	}

	private Preferences obtainPreferences() {
		return Gdx.app.getPreferences("tanks-config");
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
