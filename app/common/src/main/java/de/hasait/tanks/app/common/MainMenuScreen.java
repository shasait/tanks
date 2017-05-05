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

	private final AtomicReference<ConfiguredActionFactory> _configuredActionFactory = new AtomicReference<>();

	private final TextField _roomNameField;
	private final List<TextField> _playerNameFields = new ArrayList<>();
	private final TextButton _connectButton;
	private boolean _connect;

	public MainMenuScreen(final TanksScreenContext pContext) {
		super(pContext, 800, 600);

		setBackgroundColor(new Color(0.0f, 0.0f, 0.2f, 1.0f));

		final Table layout = addLayout();
		layout.setFillParent(true);
		layout.defaults().pad(5.0f).align(Align.left).fill();

		final Label titleLabel = createLabel("Welcome to Tanks", 2.0f);
		layout.add(titleLabel).colspan(2).padBottom(20.0f);

		layout.row();
		layout.add(createLabel("Room"));
		_roomNameField = createTextField("Default");
		layout.add(_roomNameField);

		for (int i = 0; i < 2; i++) {
			layout.row();
			layout.add(createLabel("Player " + (i + 1)));
			final TextField playerNameField = createTextField();
			if (i == 0) {
				playerNameField.setText("Player " + (i + 1));
			}
			layout.add(playerNameField);
			_playerNameFields.add(playerNameField);
			final PlayerConfig playerConfig = new PlayerConfig();
			playerNameField.setUserObject(playerConfig);

			if (i == 0) {
				setActionSet1(playerConfig);
			}
			if (i == 1) {
				setActionSet2(playerConfig);
			}

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
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		final ConfiguredActionFactory configuredActionFactory = _configuredActionFactory.get();
		if (configuredActionFactory != null && configuredActionFactory.isFinished()) {
			_configuredActionFactory.set(null);
		}

		if (_connect) {
			_connect = false;

			final String roomName = _roomNameField.getText();
			if (Util.isBlank(roomName)) {
				return;
			}

			final GameConfig config = new GameConfig(roomName, 40, 24);
			for (int i = 0; i < _playerNameFields.size(); i++) {
				final TextField playerNameField = _playerNameFields.get(i);
				final String playerName = playerNameField.getText();
				if (Util.isBlank(playerName)) {
					continue;
				}
				final PlayerConfig playerConfig = (PlayerConfig) playerNameField.getUserObject();
				playerConfig.setName(playerName.trim());
				config.getPlayers().add(playerConfig);
			}

			if (config.getPlayers().isEmpty()) {
				return;
			}

			setScreen(new ConnectingScreen(getContext(), config));
		}
	}

	private Label actionConfig(final String pTitle, final Supplier<ConfiguredAction> pActionSupplier, final Consumer<ConfiguredAction> pActionConsumer) {
		final Label label = createLabel(pTitle);
		final Consumer<ConfiguredAction> actionConsumer = new Consumer<ConfiguredAction>() {
			@Override
			public void accept(final ConfiguredAction pAction) {
				label.setText(pTitle + ": " + pAction);
				pActionConsumer.accept(pAction);
			}
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
