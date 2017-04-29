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
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
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

/**
 *
 */
public class GameScreen implements Screen {

	private static final int VIEWPORT_W = 1280;
	private static final int VIEWPORT_H = 720;
	private static final Rectangle VIEWPORT_R = new Rectangle(0, 0, VIEWPORT_W, VIEWPORT_H);
	private static final int TEXT_MARGIN = 10;
	private static final int TEXT_LINE_H = 30;

	private static final int TANK_W = 32;
	private static final int TANK_H = 32;
	private static final int TURRET_W = TANK_W / 2;
	private static final int TURRET_H = TANK_H * 3 / 4;
	private static final int BULLET_W = TANK_W / 4;
	private static final int BULLET_H = TANK_H / 4;

	private final Tanks _game;

	private final JChannel _channel;

	private final State _state;
	private final Tank _localTank;
	private final AtomicInteger _tankDamageIncrement = new AtomicInteger();

	private final Map<Address, Set<String>> _channelMembers = new HashMap<>();
	private final InputProcessor _inputProcessor = new InputAdapter() {

		@Override
		public boolean keyDown(final int keycode) {
			if (keycode == Keys.M) {
				toggleMusic();
				return true;
			}
			return super.keyDown(keycode);
		}

	};

	private int _textLine;
	private Texture _bulletImage;
	private Texture _tankImage;
	private Texture _turretImage;
	private Sound _shotSound;
	private Music _music;
	private OrthographicCamera _camera;

	public GameScreen(final Tanks pGame) {
		_game = pGame;

		_bulletImage = new Texture(Gdx.files.internal("Bullet.png"), true);
		_bulletImage.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
		_tankImage = new Texture(Gdx.files.internal("Tank.png"), true);
		_tankImage.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
		_turretImage = new Texture(Gdx.files.internal("GunTurret.png"), true);
		_turretImage.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

		_shotSound = Gdx.audio.newSound(Gdx.files.internal("Shot.wav"));
		_music = Gdx.audio.newMusic(Gdx.files.internal("Music.mp3"));
		_music.setLooping(true);

		_camera = new OrthographicCamera();
		_camera.setToOrtho(false, VIEWPORT_W, VIEWPORT_H);

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
			_channel.connect(_game._roomName);
			_channel.getState(null, 0);
		} catch (Exception pE) {
			throw new RuntimeException(pE);
		}

		_localTank = new Tank(_game._playerName, TANK_W, TANK_H, VIEWPORT_W / 2, TANK_H);
		_state._tanks.put(_localTank.getUuid(), _localTank);
		networkSend(_localTank);
	}

	@Override
	public void dispose() {
		Gdx.input.setInputProcessor(null);
		_channel.close();
		_bulletImage.dispose();
		_tankImage.dispose();
		_shotSound.dispose();
		_music.dispose();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void render(final float pDelta) {
		paintFrame();

		final float change = 100 * Gdx.graphics.getDeltaTime();

		final UpdateMsg updateMsg = new UpdateMsg();

		final TankState oldTankState = _localTank.getState();
		final TankState newTankState = oldTankState.clone();

		boolean tankDirty = processUserInput(newTankState, change);

		final int tankDamageIncrement = _tankDamageIncrement.getAndSet(0);
		if (tankDamageIncrement > 0) {
			tankDirty = true;
			newTankState._damage += tankDamageIncrement;
		}

		final Iterator<Bullet> bulletI = _state._bullets.values().iterator();
		while (bulletI.hasNext()) {
			final Bullet bullet = bulletI.next();
			if (!_localTank.isMyBullet(bullet)) {
				continue;
			}

			final BulletState oldBulletState = bullet.getState();
			final BulletState newBulletState = oldBulletState.clone();
			bullet.move(change * 2, newBulletState);

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

	@Override
	public void resize(final int pWidth, final int pHeight) {
	}

	@Override
	public void resume() {
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(_inputProcessor);
		_music.play();
	}

	private void draw(final Texture pTexture, final float pCX, final float pCY, final int pW, final int pH, final float pR) {
		_game._batch.draw(pTexture, pCX - pW / 2, pCY - pH / 2, pW / 2, pH / 2, pW, pH, 1, 1, pR, 0, 0, pTexture.getWidth(),
						  pTexture.getHeight(), false, false
		);
	}

	private void drawText(final String pText) {
		_game._font.draw(_game._batch, pText, TEXT_MARGIN, VIEWPORT_H - TEXT_MARGIN - _textLine * TEXT_LINE_H);
		_textLine++;
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
		Gdx.gl.glClearColor(0.4f, 0.4f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		_camera.update();
		_game._batch.setProjectionMatrix(_camera.combined);
		_game._batch.begin();

		_textLine = 0;

		drawText(_localTank.getName());

		final TankState localTankState = _localTank.getState();
		drawText("Points: " + localTankState._points);
		drawText("Damage: " + localTankState._damage);

		for (final Bullet bullet : _state._bullets.values()) {
			final BulletState state = bullet.getState();
			draw(_bulletImage, state._centerX, state._centerY, BULLET_W, BULLET_H, state._rotation);
		}
		for (final Tank tank : _state._tanks.values()) {
			final TankState state = tank.getState();
			draw(_tankImage, state._centerX, state._centerY, TANK_W, TANK_H, state._rotation);
			draw(_turretImage, state._centerX, state._centerY, TURRET_W, TURRET_H, state._rotation + state._turretRotation);
		}

		_game._batch.end();
	}

	private boolean processUserInput(final TankState pNewState, final float pChange) {
		boolean tankDirty = false;

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		if (Gdx.input.isTouched()) {
			final Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			_camera.unproject(touchPos);
			//_tank.setCenterIfInRectangle(touchPos.x, _tank.getCenterY(), VIEWPORT_R);
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
			final long nanoTime = TimeUtils.nanoTime();
			if (nanoTime - _localTank.getLastShotTime() > 1000000000) {
				_localTank.setLastShotTime(nanoTime);
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

	private void toggleMusic() {
		if (_music.isPlaying()) {
			_music.stop();
		} else {
			_music.play();
		}
	}

}