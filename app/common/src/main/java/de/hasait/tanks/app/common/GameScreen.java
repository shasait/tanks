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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import de.hasait.tanks.app.common.model.Bullet;
import de.hasait.tanks.app.common.model.BulletState;
import de.hasait.tanks.app.common.model.State;
import de.hasait.tanks.app.common.model.Tank;
import de.hasait.tanks.app.common.model.TankState;
import de.hasait.tanks.app.common.msg.UpdateMsg;
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.Util;

/**
 *
 */
public class GameScreen extends Abstract2DScreen<TanksScreenContext> {

	private static final int VIEWPORT_W = 1280;
	private static final int VIEWPORT_H = 720;
	private static final Rectangle VIEWPORT_R = new Rectangle(0, 0, VIEWPORT_W, VIEWPORT_H);

	private static final int TANK_W = 32;
	private static final int TANK_H = 32;
	private static final int TURRET_W = TANK_W / 2;
	private static final int TURRET_H = TANK_H * 3 / 4;
	private static final int BULLET_W = TANK_W / 4;
	private static final int BULLET_H = TANK_H / 4;
	private static final int MAX_DAMAGE = 5;
	private static final long TIME_MILLIS_BETWEEN_SHOTS = 1000;
	private static final long RESPAWN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(10);

	private final MainMenuScreen _mainMenuScreen;

	private final JChannel _channel;

	private final State _state;
	private final Tank _localTank;
	private final AtomicInteger _tankDamageIncrement = new AtomicInteger();

	private final Map<Address, Set<String>> _channelMembers = new HashMap<>();
	private final InputProcessor _toggleBackgroundMusicInputProcessor = new InputAdapter() {

		@Override
		public boolean keyDown(final int keycode) {
			if (keycode == Keys.M) {
				toggleBackgroundMusic();
				return true;
			}
			return super.keyDown(keycode);
		}

	};

	private Texture _bulletImage;
	private Texture _tankImage;
	private Texture _turretImage;
	private Sound _shotSound;

	private long _lastShotTimeMillis;

	public GameScreen(final MainMenuScreen pMainMenuScreen) {
		super(pMainMenuScreen);

		_mainMenuScreen = pMainMenuScreen;

		_bulletImage = new Texture(Gdx.files.internal("Bullet.png"), true);
		_bulletImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
		_tankImage = new Texture(Gdx.files.internal("Tank.png"), true);
		_tankImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
		_turretImage = new Texture(Gdx.files.internal("GunTurret.png"), true);
		_turretImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);

		_shotSound = Gdx.audio.newSound(Gdx.files.internal("Shot.wav"));

		_state = new State();

		setBackgroundColor(new Color(0.4f, 0.4f, 0.1f, 1.0f));
		setBackgroundMusic("Music.mp3");
		setTextMargin(10.0f);

		addInputProcessor(_toggleBackgroundMusicInputProcessor);

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
					networkReceive(received, pMessage.getSrc());
				}

				@Override
				public void setState(final InputStream pInput) throws Exception {
					try (final ObjectInputStream ois = new ObjectInputStream(pInput)) {
						final State state = (State) ois.readObject();
						_state._tanks.putAll(state._tanks);
						_state._bullets.putAll(state._bullets);
					}
				}

				@Override
				public void viewAccepted(final View pView) {
					final List<Address> members = pView.getMembers();
					final Iterator<Map.Entry<Address, Set<String>>> entryI = _channelMembers.entrySet().iterator();
					while (entryI.hasNext()) {
						final Map.Entry<Address, Set<String>> entry = entryI.next();
						if (!members.contains(entry.getKey())) {
							entryI.remove();
							_state._tanks.keySet().removeAll(entry.getValue());
							_state._bullets.keySet().removeAll(entry.getValue());
						}
					}
					members.forEach(pAddress -> _channelMembers.computeIfAbsent(pAddress, pUnused -> new HashSet<>()));
				}
			});
			_channel.connect(_mainMenuScreen.getRoomName());
			_channel.getState(null, 0);
		} catch (Exception pE) {
			throw new RuntimeException(pE);
		}

		_localTank = new Tank(_mainMenuScreen.getPlayerName(), TANK_W, TANK_H, MathUtils.random() * VIEWPORT_W,
							  MathUtils.random() * VIEWPORT_H, MathUtils.random() * MathUtils.PI2
		);
		_state._tanks.put(_localTank.getUuid(), _localTank);
		networkSend(_localTank);
	}

	@Override
	public void dispose() {
		super.dispose();
		Gdx.input.setInputProcessor(null);
		_channel.close();
		_bulletImage.dispose();
		_tankImage.dispose();
		_shotSound.dispose();
	}

	@Override
	protected void renderInternal(final float pDelta) {
		final float delta = 100 * pDelta;

		paintFrame();

		final UpdateMsg updateMsg = new UpdateMsg();

		final TankState oldTankState = _localTank.getState();
		final TankState newTankState = oldTankState.clone();

		boolean tankDirty = processUserInput(newTankState, delta);

		final int tankDamageIncrement = _tankDamageIncrement.getAndSet(0);
		if (tankDamageIncrement > 0 && newTankState._respawnAtMillis == null) {
			tankDirty = true;
			newTankState._damage += tankDamageIncrement;
			if (newTankState._damage > MAX_DAMAGE) {
				newTankState._respawnAtMillis = getTimeMillis() + RESPAWN_TIME_MILLIS;
			}
		}
		if (newTankState._respawnAtMillis != null && newTankState._respawnAtMillis < getTimeMillis()) {
			tankDirty = true;
			newTankState._damage = 0;
			newTankState._respawnAtMillis = null;
			newTankState._centerX = MathUtils.random() * VIEWPORT_W;
			newTankState._centerY = MathUtils.random() * VIEWPORT_H;
			newTankState._rotation = MathUtils.random() * MathUtils.PI2;
		}

		for (final Bullet bullet : _state._bullets.values()) {
			if (!_localTank.isMyBullet(bullet)) {
				continue;
			}

			final BulletState oldBulletState = bullet.getState();
			final BulletState newBulletState = oldBulletState.clone();
			bullet.move(delta * 2, newBulletState);

			boolean removeBullet = false;

			if (!VIEWPORT_R.contains(newBulletState._centerX, newBulletState._centerY)) {
				removeBullet = true;
			} else {
				for (final Tank tank : _state._tanks.values()) {
					if (tank.isMyBullet(bullet)) {
						continue;
					}
					if (tank.contains(newBulletState._centerX, newBulletState._centerY)) {
						removeBullet = true;
						updateMsg._incrementDamage.add(tank.getUuid());
						tankDirty = true;
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

		if (tankDirty) {
			updateMsg._tanks.add(newTankState);
		}

		if (!updateMsg.isEmpty()) {
			networkSend(updateMsg);
		}
	}

	private TankState drawTankStatusText(final Tank pTank, final boolean pDamage) {
		final TankState state = pTank.getState();
		final StringBuilder sb = new StringBuilder();
		sb.append(pTank.getName());
		sb.append(": ");
		if (pDamage) {
			final int health = Math.max(0, MAX_DAMAGE - state._damage);
			final int damage = MAX_DAMAGE - health;
			sb.append(Util.repeat("X", damage));
			sb.append(Util.repeat("O", health));
		} else {
			sb.append(state._points);
		}
		if (state._respawnAtMillis != null) {
			sb.append(" (respawn in ");
			sb.append((Math.max(0, state._respawnAtMillis - getTimeMillis())) / 1000);
			sb.append("s)");
		}
		drawText(sb.toString());
		drawText("");
		return state;
	}

	private void networkReceive(final Object pReceived, final Address pMember) {
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
				if (_localTank.getUuid().equals(uuid)) {
					_tankDamageIncrement.incrementAndGet();
				}
			}
		}
		if (pReceived instanceof Tank) {
			final Tank tank = (Tank) pReceived;
			_state._tanks.putIfAbsent(tank.getUuid(), tank);
			_channelMembers.get(pMember).add(tank.getUuid());
		}
		if (pReceived instanceof Bullet) {
			final Bullet bullet = (Bullet) pReceived;
			_state._bullets.putIfAbsent(bullet.getUuid(), bullet);
			_channelMembers.get(pMember).add(bullet.getUuid());
		}
	}

	private void networkSend(final Object pObject) {
		try {
			_channel.send(new Message(null, pObject));
		} catch (final Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	private void paintFrame() {
		drawTankStatusText(_localTank, true);

		for (final Bullet bullet : _state._bullets.values()) {
			final BulletState state = bullet.getState();
			drawTexture(_bulletImage, state._centerX, state._centerY, BULLET_W, BULLET_H, state._rotation);
		}
		for (final Tank tank : _state._tanks.values()) {
			final TankState state = drawTankStatusText(tank, false);
			if (state._respawnAtMillis == null) {
				drawTexture(_tankImage, state._centerX, state._centerY, TANK_W, TANK_H, state._rotation);
				drawTexture(_turretImage, state._centerX, state._centerY, TURRET_W, TURRET_H, state._rotation + state._turretRotation);
			}
		}
	}

	private boolean processUserInput(final TankState pNewState, final float pChange) {
		boolean tankDirty = false;

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		if (Gdx.input.isKeyPressed(Keys.W)) {
			_localTank.move(pChange, pNewState);
			tankDirty = true;
		}
		if (Gdx.input.isKeyPressed(Keys.S)) {
			_localTank.move(-pChange, pNewState);
			tankDirty = true;
		}
		if (pNewState._centerX < 0) {
			pNewState._centerX = 0;
		}
		if (pNewState._centerY < 0) {
			pNewState._centerY = 0;
		}
		if (pNewState._centerX > VIEWPORT_W) {
			pNewState._centerX = VIEWPORT_W;
		}
		if (pNewState._centerY > VIEWPORT_H) {
			pNewState._centerY = VIEWPORT_H;
		}
		if (Gdx.input.isKeyPressed(Keys.A)) {
			pNewState._rotation += pChange;
			tankDirty = true;
		}
		if (Gdx.input.isKeyPressed(Keys.D)) {
			pNewState._rotation -= pChange;
			tankDirty = true;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			pNewState._turretRotation += pChange;
			tankDirty = true;
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			pNewState._turretRotation -= pChange;
			tankDirty = true;
		}

		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			final long timeMillis = getTimeMillis();
			if (pNewState._respawnAtMillis == null && timeMillis - _lastShotTimeMillis > TIME_MILLIS_BETWEEN_SHOTS) {
				_lastShotTimeMillis = timeMillis;
				spawnBullet(_localTank);
			}
		}

		return tankDirty;
	}

	private void spawnBullet(final Tank pTank) {
		_shotSound.play();
		final TankState state = pTank.getState();
		final Bullet bullet = new Bullet(pTank.getUuid(), state._centerX, state._centerY, state._rotation + state._turretRotation);
		_state._bullets.put(bullet.getUuid(), bullet);
		networkSend(bullet);
	}

}