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


import java.util.concurrent.Future;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.hasait.tanks.app.common.model.DistributedModel;
import de.hasait.tanks.app.common.model.GameConfig;
import de.hasait.tanks.app.common.model.PlayerConfig;
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;

/**
 *
 */
public class ConnectingScreen extends Abstract2DScreen<TanksScreenContext> {

	private final GameConfig _config;

	private final DistributedModel _model;
	private Future<?> _connect;

	public ConnectingScreen(final TanksScreenContext pContext, final GameConfig pConfig) {
		super(pContext, 800, 600);

		_config = pConfig;
		_model = new DistributedModel();

		setBackgroundColor(new Color(0.0f, 0.2f, 0.0f, 1.0f));

		final Label titleLabel = createLabel("Connecting...", 2.0f);

		final Table layout = addLayout();
		layout.setFillParent(true);

		layout.add(titleLabel);
	}

	@Override
	protected void renderInternal(final float pDelta) {
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		if (_connect == null) {
			_connect = Util.EXECUTOR_SERVICE
					.submit(() -> _model.connect(_config.getRoomName(), _config.getWishPiecesX(), _config.getWishPiecesY()));
		} else if (_connect.isDone() && _model.hasModel()) {
			for (final PlayerConfig playerConfig : _config.getPlayers()) {
				_model.createTank(playerConfig);
			}
			setScreen(new GameScreen(getContext(), _model));
		}
	}

}
