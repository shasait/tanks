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

import java.util.Collection;
import java.util.Optional;

import com.badlogic.gdx.math.MathUtils;

import de.hasait.tanks.app.common.model.Bullet;
import de.hasait.tanks.app.common.model.BulletState;
import de.hasait.tanks.app.common.model.DistributedModel;
import de.hasait.tanks.app.common.model.LocalTank;
import de.hasait.tanks.app.common.model.Rules;
import de.hasait.tanks.app.common.model.Tank;
import de.hasait.tanks.app.common.model.TankState;
import de.hasait.tanks.app.common.msg.UpdateMsg;

/**
 *
 */
public class TanksLogic {

	private final DistributedModel _model;
	private final Callback _callback;

	public TanksLogic(final DistributedModel pModel, final Callback pCallback) {
		super();

		_model = pModel;
		_callback = pCallback;
	}

	public Collection<Bullet> getBullets() {
		return _model.getModel().getBullets();
	}

	public Collection<LocalTank> getLocalLocalTanks() {
		return _model.getModel().getLocalLocalTanks();
	}

	public Collection<Tank> getLocalTanks() {
		return _model.getModel().getLocalTanks();
	}

	public Rules getRules() {
		return _model.getModel().getRules();
	}

	public Collection<Tank> getTanks() {
		return _model.getModel().getTanks();
	}

	public void update(final long pTimeMillis, final float pDeltaTimeSeconds) {
		for (final LocalTank localTank : getLocalLocalTanks()) {
			final Optional<Tank> optionalTank = _model.getModel().getTank(localTank.getTankUuid());
			if (!optionalTank.isPresent()) {
				// not yet received via network
				continue;
			}
			final UpdateContext updateContext = new UpdateContext(pTimeMillis, pDeltaTimeSeconds, localTank, optionalTank.get());

			updateTank(updateContext);
			handleTankDamage(updateContext);
			handleTankRespawn(updateContext);
			updateBullets(updateContext);


			final UpdateMsg updateMsg = updateContext._updateMsg;
			if (updateContext._tankDirty) {
				updateMsg._tanks.add(updateContext._newTankState);
			}

			if (!updateMsg.isEmpty()) {
				_model.networkSend(updateMsg);
			}
		}
	}

	private void handleTankDamage(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;

		final int tankDamageIncrement = localTank.getAndResetDamageIncrement();
		if (tankDamageIncrement > 0 && newTankState._respawnAtMillis == null) {
			pUpdateContext._tankDirty = true;
			newTankState._damage += tankDamageIncrement;
			if (newTankState._damage >= getRules()._maxDamage) {
				newTankState._respawnAtMillis = pUpdateContext._timeMillis + getRules()._respawnTimeMillis;
			}
		}
	}

	private void handleTankRespawn(final UpdateContext pUpdateContext) {
		final TankState newTankState = pUpdateContext._newTankState;

		if (newTankState._respawnAtMillis != null && newTankState._respawnAtMillis < pUpdateContext._timeMillis) {
			pUpdateContext._tankDirty = true;
			newTankState._damage = 0;
			newTankState._respawnAtMillis = null;
			newTankState._centerX = MathUtils.random() * _model.getModel().getWorldW();
			newTankState._centerY = MathUtils.random() * _model.getModel().getWorldH();
			newTankState._rotation = MathUtils.random() * 360.0f;
		}
	}

	private void updateBullets(final UpdateContext pUpdateContext) {
		final TankState newTankState = pUpdateContext._newTankState;
		final UpdateMsg updateMsg = pUpdateContext._updateMsg;
		final float speed = pUpdateContext._deltaTimeSeconds * _model.getModel().getBulletSpeed();

		for (final Bullet bullet : getBullets()) {
			if (!pUpdateContext._tank.isMyBullet(bullet)) {
				continue;
			}

			final BulletState oldBulletState = bullet.getState();
			final BulletState newBulletState = oldBulletState.clone();
			bullet.move(speed, newBulletState);

			boolean removeBullet = false;

			if (!_model.getModel().worldContains(newBulletState._centerX, newBulletState._centerY)) {
				removeBullet = true;
			} else {
				for (final Tank tank : getTanks()) {
					if (tank.isMyBullet(bullet)) {
						continue;
					}
					if (tank.contains(newBulletState._centerX, newBulletState._centerY)) {
						removeBullet = true;
						updateMsg._incrementDamage.add(tank.getUuid());
						pUpdateContext._tankDirty = true;
						newTankState._points++;
					}
				}
			}

			if (removeBullet) {
				updateMsg._removedBullets.add(newBulletState._uuid);
			} else {
				updateMsg._bullets.add(newBulletState);
			}
		}
	}

	private void updateTank(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;
		final float speed = pUpdateContext._deltaTimeSeconds * _model.getModel().getTankSpeed();

		final TankActions tankActions = new TankActions();
		localTank.fillActions(tankActions);


		float moveSpeed = 0.0f;
		if (tankActions._moveForward) {
			moveSpeed += speed;
		}
		if (tankActions._moveBackward) {
			moveSpeed -= speed;
		}
		if (moveSpeed != 0.0f) {


			pUpdateContext._tank.move(moveSpeed, newTankState);
			pUpdateContext._tankDirty = true;
		}
		if (newTankState._centerX < 0) {
			newTankState._centerX = 0;
		}
		if (newTankState._centerY < 0) {
			newTankState._centerY = 0;
		}
		if (newTankState._centerX > _model.getModel().getWorldW()) {
			newTankState._centerX = _model.getModel().getWorldW();
		}
		if (newTankState._centerY > _model.getModel().getWorldH()) {
			newTankState._centerY = _model.getModel().getWorldH();
		}
		if (tankActions._rotateLeft && !tankActions._rotateRight) {
			newTankState._rotation += speed;
			pUpdateContext._tankDirty = true;
		}
		if (tankActions._rotateRight && !tankActions._rotateLeft) {
			newTankState._rotation -= speed;
			pUpdateContext._tankDirty = true;
		}
		if (tankActions._turrentRotateLeft && !tankActions._turrentRotateRight) {
			newTankState._turretRotation += speed;
			pUpdateContext._tankDirty = true;
		}
		if (tankActions._turrentRotateRight && !tankActions._turrentRotateLeft) {
			newTankState._turretRotation -= speed;
			pUpdateContext._tankDirty = true;
		}

		if (tankActions._fire) {
			if (newTankState._respawnAtMillis == null
					&& pUpdateContext._timeMillis - localTank.getLastShotTimeMillis() > getRules()._timeMillisBetweenShots) {
				localTank.setLastShotTimeMillis(pUpdateContext._timeMillis);
				_model.spawnBullet(pUpdateContext._tank);
				_callback.onSpawnBullet();
			}
		}
	}

	public interface Callback {

		void onSpawnBullet();

	}

	private static class UpdateContext {
		private final long _timeMillis;
		private final float _deltaTimeSeconds;

		private final LocalTank _localTank;
		private final Tank _tank;

		private final UpdateMsg _updateMsg;

		private final TankState _newTankState;

		private boolean _tankDirty;

		public UpdateContext(final long pTimeMillis, final float pDeltaTimeSeconds, final LocalTank pLocalTank, final Tank pTank) {
			super();

			_timeMillis = pTimeMillis;
			_deltaTimeSeconds = pDeltaTimeSeconds;

			_localTank = pLocalTank;
			_tank = pTank;

			_updateMsg = new UpdateMsg();

			final TankState oldTankState = _tank.getState();
			_newTankState = oldTankState.clone();
			_tankDirty = false;
		}
	}

}
