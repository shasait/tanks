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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;

import de.hasait.tanks.app.common.model.AbstractGameObject;
import de.hasait.tanks.app.common.model.Bullet;
import de.hasait.tanks.app.common.model.BulletState;
import de.hasait.tanks.app.common.model.DistributedWorld;
import de.hasait.tanks.app.common.model.LocalTank;
import de.hasait.tanks.app.common.model.Obstacle;
import de.hasait.tanks.app.common.model.Rules;
import de.hasait.tanks.app.common.model.Tank;
import de.hasait.tanks.app.common.model.TankState;
import de.hasait.tanks.app.common.msg.UpdateMsg;

/**
 *
 */
public class TanksLogic {

	private final DistributedWorld _world;
	private final Callback _callback;

	public TanksLogic(final DistributedWorld pWorld, final Callback pCallback) {
		super();

		_world = pWorld;
		_callback = pCallback;
	}

	public Collection<Bullet> getBullets() {
		return _world.getWorld().getBullets();
	}

	public Collection<LocalTank> getLocalLocalTanks() {
		return _world.getWorld().getLocalLocalTanks();
	}

	public Collection<Tank> getLocalTanks() {
		return _world.getWorld().getLocalTanks();
	}

	public Collection<Obstacle> getObstacles() {
		return _world.getWorld().getObstacles();
	}

	public Rules getRules() {
		return _world.getWorld().getRules();
	}

	public Collection<Tank> getTanks() {
		return _world.getWorld().getTanks();
	}

	public void update(final long pTimeMillis, final float pDeltaTimeSeconds) {
		for (final LocalTank localTank : getLocalLocalTanks()) {
			final Optional<Tank> optionalTank = _world.getWorld().getTank(localTank.getTankUuid());
			if (!optionalTank.isPresent()) {
				// not yet received via network
				continue;
			}
			final UpdateContext updateContext = new UpdateContext(pTimeMillis, pDeltaTimeSeconds, localTank, optionalTank.get());

			updateTank(updateContext);
			handleTankDamage(updateContext);
			handleTankReSpawn(updateContext);
			updateBullets(updateContext);


			final UpdateMsg updateMsg = updateContext._updateMsg;
			if (updateContext._tankDirty) {
				updateMsg._tanks.add(updateContext._newTankState);
			}

			if (!updateMsg.isEmpty()) {
				_world.networkSend(updateMsg);
			}
		}
	}

	private Map<AbstractGameObject<?>, Float> determineIntersections(final Tank pTank, final TankState pTankState) {
		final Polygon bounds = pTankState == null ? pTank.getBounds() : pTank.createBounds(pTankState);
		final float whDist2 = pTank.getWhDist2();

		final Map<AbstractGameObject<?>, Float> intersections = new HashMap<>();
		for (final Tank otherTank : getTanks()) {
			if (pTank == otherTank) {
				continue;
			}
			final Float distanceIfIntersects = otherTank.intersects(bounds, whDist2);
			if (distanceIfIntersects != null) {
				intersections.put(otherTank, distanceIfIntersects);
			}
		}
		for (final Obstacle obstacle : getObstacles()) {
			final Float distanceIfIntersects = obstacle.intersects(bounds, whDist2);
			if (distanceIfIntersects != null) {
				intersections.put(obstacle, distanceIfIntersects);
			}
		}
		return intersections;
	}

	private void handleTankDamage(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;

		final int tankDamageIncrement = localTank.getAndResetDamageIncrement();
		if (tankDamageIncrement > 0 && newTankState._spawnAtMillis == null) {
			pUpdateContext._tankDirty = true;
			newTankState._damage += tankDamageIncrement;
			if (newTankState._damage >= getRules()._maxDamage) {
				newTankState._spawnAtMillis = pUpdateContext._timeMillis + getRules()._respawnTimeMillis;
			}
		}
	}

	private void handleTankReSpawn(final UpdateContext pUpdateContext) {
		final TankState newTankState = pUpdateContext._newTankState;

		if (newTankState._spawnAtMillis != null && newTankState._spawnAtMillis < pUpdateContext._timeMillis) {
			newTankState._centerX = MathUtils.random() * _world.getWorld().getWorldW();
			newTankState._centerY = MathUtils.random() * _world.getWorld().getWorldH();
			newTankState._rotation = MathUtils.random() * 360.0f;
			if (determineIntersections(pUpdateContext._tank, newTankState).isEmpty()) {
				pUpdateContext._tankDirty = true;
				newTankState._damage = 0;
				newTankState._spawnAtMillis = null;
			}
		}
	}

	private void updateBullets(final UpdateContext pUpdateContext) {
		final TankState newTankState = pUpdateContext._newTankState;
		final UpdateMsg updateMsg = pUpdateContext._updateMsg;
		final float speed = pUpdateContext._deltaTimeSeconds * _world.getWorld().getBulletSpeed();

		for (final Bullet bullet : getBullets()) {
			if (!pUpdateContext._tank.isMyBullet(bullet)) {
				continue;
			}

			final BulletState oldBulletState = bullet.getState();
			final BulletState newBulletState = oldBulletState.clone();
			bullet.move(speed, newBulletState);

			boolean removeBullet = false;

			if (!_world.getWorld().worldContains(newBulletState._centerX, newBulletState._centerY)) {
				removeBullet = true;
			} else {
				for (final Tank tank : getTanks()) {
					if (tank.isMyBullet(bullet) || tank.getState()._spawnAtMillis != null) {
						continue;
					}
					if (tank.contains(newBulletState._centerX, newBulletState._centerY)) {
						removeBullet = true;
						updateMsg._incrementDamage.add(tank.getUuid());
						pUpdateContext._tankDirty = true;
						newTankState._points++;
					}
				}
				for (final Obstacle obstacle : getObstacles()) {
					if (obstacle.contains(newBulletState._centerX, newBulletState._centerY)) {
						removeBullet = true;
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
		final Tank tank = pUpdateContext._tank;
		final TankState newTankState = pUpdateContext._newTankState;
		final float speed = pUpdateContext._deltaTimeSeconds * _world.getWorld().getTankSpeed();

		final TankActions tankActions = new TankActions();
		localTank.getPlayerConfig().fillActions(tankActions);


		float moveSpeed = 0.0f;
		if (tankActions._moveForward != 0.0f) {
			moveSpeed += tankActions._moveForward * speed;
		}
		if (tankActions._moveBackward != 0.0f) {
			moveSpeed -= tankActions._moveBackward * speed;
		}
		if (moveSpeed != 0.0f) {
			final Map<AbstractGameObject<?>, Float> beforeIntersections = determineIntersections(tank, null);
			tank.move(moveSpeed, newTankState);
			final Map<AbstractGameObject<?>, Float> afterIntersections = determineIntersections(tank, newTankState);
			boolean validMove = true;
			for (final Map.Entry<AbstractGameObject<?>, Float> afterEntry : afterIntersections.entrySet()) {
				final Float afterDistance = afterEntry.getValue();
				final Float beforeDistance = beforeIntersections.get(afterEntry.getKey());
				if (beforeDistance == null || afterDistance < beforeDistance) {
					validMove = false;
					break;
				}
			}

			if (!validMove) {
				newTankState._centerX = pUpdateContext._oldTankState._centerX;
				newTankState._centerY = pUpdateContext._oldTankState._centerY;
			} else {
				pUpdateContext._tankDirty = true;
			}
		}
		if (newTankState._centerX < 0) {
			newTankState._centerX = 0;
		}
		if (newTankState._centerY < 0) {
			newTankState._centerY = 0;
		}
		if (newTankState._centerX > _world.getWorld().getWorldW()) {
			newTankState._centerX = _world.getWorld().getWorldW();
		}
		if (newTankState._centerY > _world.getWorld().getWorldH()) {
			newTankState._centerY = _world.getWorld().getWorldH();
		}

		float rotationSpeed = 0.0f;
		if (tankActions._rotateLeft != 0.0f) {
			rotationSpeed += tankActions._rotateLeft * speed;
		}
		if (tankActions._rotateRight != 0.0f) {
			rotationSpeed -= tankActions._rotateRight * speed;
		}
		if (rotationSpeed != 0.0f) {
			newTankState._rotation += rotationSpeed;
			pUpdateContext._tankDirty = true;
		}

		float turretRotationSpeed = 0.0f;
		if (tankActions._turrentRotateLeft != 0.0f) {
			turretRotationSpeed += tankActions._turrentRotateLeft * speed;
		}
		if (tankActions._turrentRotateRight != 0.0f) {
			turretRotationSpeed -= tankActions._turrentRotateRight * speed;
		}
		if (turretRotationSpeed != 0.0f) {
			newTankState._turretRotation += turretRotationSpeed;
			pUpdateContext._tankDirty = true;
		}

		if (tankActions._fire != 0.0f) {
			if (newTankState._spawnAtMillis == null
					&& pUpdateContext._timeMillis - localTank.getLastShotTimeMillis() > getRules()._timeMillisBetweenShots) {
				localTank.setLastShotTimeMillis(pUpdateContext._timeMillis);
				_world.createBullet(tank);
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

		private final TankState _oldTankState;
		private final TankState _newTankState;

		private boolean _tankDirty;

		public UpdateContext(final long pTimeMillis, final float pDeltaTimeSeconds, final LocalTank pLocalTank, final Tank pTank) {
			super();

			_timeMillis = pTimeMillis;
			_deltaTimeSeconds = pDeltaTimeSeconds;

			_localTank = pLocalTank;
			_tank = pTank;

			_updateMsg = new UpdateMsg();

			_oldTankState = _tank.getState();
			_newTankState = _oldTankState.clone();
			_tankDirty = false;
		}
	}

}
