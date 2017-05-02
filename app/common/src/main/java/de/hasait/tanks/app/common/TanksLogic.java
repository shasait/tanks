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

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import de.hasait.tanks.app.common.model.Bullet;
import de.hasait.tanks.app.common.model.BulletState;
import de.hasait.tanks.app.common.model.Rules;
import de.hasait.tanks.app.common.model.State;
import de.hasait.tanks.app.common.model.Tank;
import de.hasait.tanks.app.common.model.TankState;
import de.hasait.tanks.app.common.msg.UpdateMsg;

/**
 *
 */
public class TanksLogic implements Disposable {

	private final TanksScreenContext _context;

	private final Callback _callback;

	private final JChannel _channel;
	private final Set<Address> _channelMembers = new HashSet<>();

	private final State _state;

	private final Map<String, LocalTank> _localTanks = new HashMap<>();

	public TanksLogic(final TanksScreenContext pContext, final String pRoomName, final Callback pCallback) {
		super();

		_context = pContext;
		_callback = pCallback;

		_state = new State();

		try {
			_channel = new JChannel();
			_channel.setReceiver(new ReceiverAdapter() {
				@Override
				public void getState(final OutputStream pOutput) throws Exception {
					try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(pOutput, 1024))) {
						oos.writeObject(_state);
					}
				}

				@Override
				public void receive(final Message pMessage) {
					final Object received = pMessage.getObject();
					networkReceive(received);
				}

				@Override
				public void setState(final InputStream pInput) throws Exception {
					try (final ObjectInputStream ois = new ObjectInputStream(pInput)) {
						final State state = (State) ois.readObject();
						state._tanks.values().forEach(TanksLogic.this::networkReceive);
						state._bullets.values().forEach(TanksLogic.this::networkReceive);
						networkReceive(state._rules.get());
					}
				}

				@Override
				public void viewAccepted(final View pView) {
					final List<Address> members = pView.getMembers();
					final Iterator<Address> entryI = _channelMembers.iterator();
					while (entryI.hasNext()) {
						final Address address = entryI.next();
						if (!members.contains(address)) {
							final String addressString = address.toString();
							entryI.remove();
							_state._tanks.entrySet().removeIf(pEntry -> pEntry.getValue().getOwnerAddress().equals(addressString));
							_state._bullets.entrySet().removeIf(pEntry -> pEntry.getValue().getOwnerAddress().equals(addressString));
						}
					}
					_channelMembers.addAll(members);
				}
			});
			_channel.connect(pRoomName);
			_channel.getState(null, 0);
		} catch (Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	@Override
	public void dispose() {
		_channel.close();
	}

	public Collection<Bullet> getBullets() {
		return _state._bullets.values();
	}

	public Stream<Tank> getLocalTanks() {
		return _localTanks.values().stream().map(pLocalTank -> pLocalTank._tank);
	}

	public Rules getRules() {
		return _state._rules.get();
	}

	public Collection<Tank> getTanks() {
		return _state._tanks.values();
	}

	public void spawnTank(final String pPlayerName, final Consumer<TankActions> pTankActionsFiller) {
		final Tank tank = new Tank(_channel.getAddressAsString(), pPlayerName, _context.getTankW(), _context.getTankH(),
								   TimeUtils.millis() + getRules()._respawnTimeMillis
		);
		final LocalTank localTank = new LocalTank(tank, pTankActionsFiller);
		_localTanks.put(tank.getUuid(), localTank);
		_state._tanks.put(tank.getUuid(), tank);
		networkSend(tank);
	}

	public void update(final long pTimeMillis, final float pDeltaTimeSeconds) {
		for (final LocalTank localTank : _localTanks.values()) {
			final UpdateContext updateContext = new UpdateContext(pTimeMillis, pDeltaTimeSeconds, localTank);

			updateTank(updateContext);
			handleTankDamage(updateContext);
			handleTankRespawn(updateContext);
			updateBullets(updateContext);


			final UpdateMsg updateMsg = updateContext._updateMsg;
			if (updateContext._tankDirty) {
				updateMsg._tanks.add(updateContext._newTankState);
			}

			if (!updateMsg.isEmpty()) {
				networkSend(updateMsg);
			}
		}
	}

	private void handleTankDamage(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;

		final int tankDamageIncrement = localTank._tankDamageIncrement.getAndSet(0);
		if (tankDamageIncrement > 0 && newTankState._respawnAtMillis == null) {
			pUpdateContext._tankDirty = true;
			newTankState._damage += tankDamageIncrement;
			if (newTankState._damage >= getRules()._maxDamage) {
				newTankState._respawnAtMillis = pUpdateContext._timeMillis + getRules()._respawnTimeMillis;
			}
		}
	}

	private void handleTankRespawn(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;

		if (newTankState._respawnAtMillis != null && newTankState._respawnAtMillis < pUpdateContext._timeMillis) {
			pUpdateContext._tankDirty = true;
			newTankState._damage = 0;
			newTankState._respawnAtMillis = null;
			newTankState._centerX = MathUtils.random() * _context.getViewportW();
			newTankState._centerY = MathUtils.random() * _context.getViewportH();
			newTankState._rotation = MathUtils.random() * MathUtils.PI2;
		}
	}

	private void networkReceive(final Object pReceived) {
		if (pReceived instanceof UpdateMsg) {
			final UpdateMsg dirty = (UpdateMsg) pReceived;
			for (final TankState tankState : dirty._tanks) {
				_state.apply(tankState);
			}
			for (final BulletState bulletState : dirty._bullets) {
				_state.apply(bulletState);
			}
			for (final String uuid : dirty._removedBullets) {
				_state._bullets.remove(uuid);
			}
			for (final String uuid : dirty._incrementDamage) {
				final LocalTank localTank = _localTanks.get(uuid);
				if (localTank != null) {
					localTank._tankDamageIncrement.incrementAndGet();
				}
			}
		}
		if (pReceived instanceof Tank) {
			final Tank tank = (Tank) pReceived;
			_state._tanks.putIfAbsent(tank.getUuid(), tank);
		}
		if (pReceived instanceof Bullet) {
			final Bullet bullet = (Bullet) pReceived;
			_state._bullets.putIfAbsent(bullet.getUuid(), bullet);
		}
		if (pReceived instanceof Rules) {
			final Rules rules = (Rules) pReceived;
			_state._rules.set(rules);
		}
	}

	private void networkSend(final Object pObject) {
		try {
			_channel.send(new Message(null, pObject));
		} catch (final Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	private void spawnBullet(final Tank pTank) {
		final TankState state = pTank.getState();
		final Bullet bullet = new Bullet(_channel.getAddressAsString(), pTank.getUuid(), state._centerX, state._centerY,
										 state._rotation + state._turretRotation
		);
		_state._bullets.put(bullet.getUuid(), bullet);
		networkSend(bullet);
	}

	private void updateBullets(final UpdateContext pUpdateContext) {
		final LocalTank localTank = pUpdateContext._localTank;
		final TankState newTankState = pUpdateContext._newTankState;
		final UpdateMsg updateMsg = pUpdateContext._updateMsg;
		final float speed = pUpdateContext._deltaTimeSeconds * _context.getBulletSpeed();

		for (final Bullet bullet : _state._bullets.values()) {
			if (!localTank._tank.isMyBullet(bullet)) {
				continue;
			}

			final BulletState oldBulletState = bullet.getState();
			final BulletState newBulletState = oldBulletState.clone();
			bullet.move(speed, newBulletState);

			boolean removeBullet = false;

			if (!_context.viewportContains(newBulletState._centerX, newBulletState._centerY)) {
				removeBullet = true;
			} else {
				for (final Tank tank : _state._tanks.values()) {
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
		final float speed = pUpdateContext._deltaTimeSeconds * _context.getTankSpeed();

		final TankActions tankActions = new TankActions();
		localTank._tankActionsFiller.accept(tankActions);


		float moveSpeed = 0.0f;
		if (tankActions._moveForward) {
			moveSpeed += speed;
		}
		if (tankActions._moveBackward) {
			moveSpeed -= speed;
		}
		if (moveSpeed != 0.0f) {

			localTank._tank.move(moveSpeed, newTankState);
			pUpdateContext._tankDirty = true;
		}
		if (newTankState._centerX < 0) {
			newTankState._centerX = 0;
		}
		if (newTankState._centerY < 0) {
			newTankState._centerY = 0;
		}
		if (newTankState._centerX > _context.getViewportW()) {
			newTankState._centerX = _context.getViewportW();
		}
		if (newTankState._centerY > _context.getViewportH()) {
			newTankState._centerY = _context.getViewportH();
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
					&& pUpdateContext._timeMillis - localTank._lastShotTimeMillis > getRules()._timeMillisBetweenShots) {
				localTank._lastShotTimeMillis = pUpdateContext._timeMillis;
				spawnBullet(localTank._tank);
				_callback.onSpawnBullet();
			}
		}
	}

	public interface Callback {

		void onSpawnBullet();

	}

	private static class LocalTank {
		private final Tank _tank;
		private final Consumer<TankActions> _tankActionsFiller;
		private final AtomicInteger _tankDamageIncrement = new AtomicInteger();
		private long _lastShotTimeMillis;

		private LocalTank(final Tank pTank, final Consumer<TankActions> pTankActionsFiller) {
			super();

			_tank = pTank;
			_tankActionsFiller = pTankActionsFiller;
		}
	}

	private static class UpdateContext {
		private final long _timeMillis;
		private final float _deltaTimeSeconds;

		private final LocalTank _localTank;

		private final UpdateMsg _updateMsg;

		private final TankState _newTankState;

		private boolean _tankDirty;

		public UpdateContext(final long pTimeMillis, final float pDeltaTimeSeconds, final LocalTank pLocalTank) {
			super();

			_timeMillis = pTimeMillis;
			_deltaTimeSeconds = pDeltaTimeSeconds;

			_localTank = pLocalTank;
			_updateMsg = new UpdateMsg();

			final TankState oldTankState = _localTank._tank.getState();
			_newTankState = oldTankState.clone();
			_tankDirty = false;
		}
	}

}
