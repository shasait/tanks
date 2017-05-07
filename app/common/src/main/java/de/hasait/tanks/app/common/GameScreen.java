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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import de.hasait.tanks.app.common.model.AbstractGameObject;
import de.hasait.tanks.app.common.model.AbstractState;
import de.hasait.tanks.app.common.model.DistributedWorld;
import de.hasait.tanks.app.common.model.LocalTank;
import de.hasait.tanks.app.common.model.Tank;
import de.hasait.tanks.app.common.model.TankState;
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;

/**
 *
 */
public class GameScreen extends Abstract2DScreen<TanksScreenContext> {

	private final InputProcessor _inputProcessor = new InputAdapter() {

		@Override
		public boolean keyUp(final int keycode) {
			if (keycode == Keys.M) {
				toggleBackgroundMusic();
				return true;
			}
			if (keycode == Keys.ESCAPE) {
				Gdx.app.exit();
			}
			return super.keyUp(keycode);
		}


	};

	private final TanksLogic.Callback _callback = new TanksLogic.Callback() {
		@Override
		public void onSpawnBullet() {
			_shotSound.play();
		}
	};

	private final DistributedWorld _world;
	private final TanksLogic _tanksLogic;

	private Texture _tankTexture;
	private Texture _turretTexture;
	private Texture _bulletTexture;
	private Texture _rockTexture;
	private Sound _shotSound;


	public GameScreen(final TanksScreenContext pContext, final DistributedWorld pWorld) {
		super(pContext, pWorld.getWorld().getWorldW(), pWorld.getWorld().getWorldH());

		_world = pWorld;
		addDisposable(_world);
		_tanksLogic = new TanksLogic(_world, _callback);

		_tankTexture = loadTexture("Tank.png");
		_turretTexture = loadTexture("Turret.png");
		_bulletTexture = loadTexture("Bullet.png");
		_rockTexture = loadTexture("Rock.png");

		_shotSound = Gdx.audio.newSound(Gdx.files.internal("Shot.wav"));
		addDisposable(_shotSound);

		setBackgroundColor(new Color(0.4f, 0.4f, 0.1f, 1.0f));
		setBackgroundMusic("Music.mp3");
		setTextMargin(10.0f);

		addInputProcessor(_inputProcessor);

		for (final LocalTank tank : _world.getWorld().getLocalLocalTanks()) {
			tank.getPlayerConfig().initActions(this);
		}
	}

	@Override
	protected void renderInternal(final float pDeltaTimeSeconds) {
		paintFrame();

		_tanksLogic.update(getTimeMillis(), pDeltaTimeSeconds);
	}

	private TankState drawTankStatusText(final Tank pTank, final boolean pDamageInsteadOfPointsVisible, final boolean pSpawnVisible) {
		final TankState state = pTank.getState();
		final StringBuilder sb = new StringBuilder();
		sb.append(pTank.getName());
		sb.append(": ");
		if (pDamageInsteadOfPointsVisible) {
			final int maxDamage = _tanksLogic.getRules()._maxDamage;
			final int health = Math.max(0, maxDamage - state._damage);
			final int damage = maxDamage - health;
			sb.append(Util.repeat("X", damage));
			sb.append(Util.repeat("O", health));
		} else {
			sb.append(state._points);
		}
		if (pSpawnVisible) {
			if (state._spawnAtMillis != null) {
				sb.append(" (spawn in ");
				sb.append((Math.max(0, state._spawnAtMillis - getTimeMillis())) / 1000);
				sb.append("s)");
			}
		}
		drawText(sb.toString());
		drawText("");
		return state;
	}

	private void paintFrame() {
		_tanksLogic.getLocalTanks().forEach(pTank -> drawTankStatusText(pTank, true, false));

		_tanksLogic.getObstacles().forEach(pObstacle -> paintGameObject(pObstacle, _rockTexture));
		_tanksLogic.getBullets().forEach(pBullet -> paintGameObject(pBullet, _bulletTexture));

		for (final Tank tank : _tanksLogic.getTanks()) {
			final TankState state = drawTankStatusText(tank, false, true);
			if (state._spawnAtMillis == null) {
				paintGameObject(tank, state, _tankTexture);
				drawTexture(_turretTexture, state._centerX, state._centerY, _world.getWorld().getTurretW(), _world.getWorld().getTurretH(),
							state._rotation + state._turretRotation
				);
			}
		}
	}

	private <S extends AbstractState<S>> void paintGameObject(final AbstractGameObject<S> pGameObject, final Texture pTexture) {
		paintGameObject(pGameObject, pGameObject.getState(), pTexture);
	}

	private <S extends AbstractState<S>> void paintGameObject(final AbstractGameObject<S> pGameObject, final S pState, final Texture pTexture) {
		drawTexture(pTexture, pState._centerX, pState._centerY, pGameObject.getWidth(), pGameObject.getHeight(), pState._rotation);
	}

}