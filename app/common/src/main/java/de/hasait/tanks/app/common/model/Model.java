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

package de.hasait.tanks.app.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.badlogic.gdx.math.Rectangle;

/**
 *
 */
public class Model {

	private final int _piecesX, _piecesY;
	private final int _worldW, _worldH;
	private final Rectangle _worldR;
	private final int _tankW, _tankH;
	private final int _turretW, _turretH;
	private final int _bulletW, _bulletH;
	private final float _tankSpeed;
	private final float _bulletSpeed;

	private final Map<String, Tank> _tanks = new ConcurrentHashMap<>();
	private final Map<String, Bullet> _bullets = new ConcurrentHashMap<>();

	private final AtomicReference<Rules> _rules = new AtomicReference<>(new Rules());

	private final Map<String, LocalTank> _localLocalTanks = new HashMap<>();
	private final Map<String, Tank> _localTanks = new HashMap<>();

	public Model(final int pPiecesX, final int pPiecesY) {
		super();

		_piecesX = pPiecesX;
		_piecesY = pPiecesY;
		_tankW = 32;
		_tankH = _tankW;
		_turretW = _tankW / 2;
		_turretH = _tankH * 3 / 4;
		_bulletW = _tankW / 4;
		_bulletH = _tankH / 3;
		_worldW = _piecesX * _tankW;
		_worldH = _piecesY * _tankH;
		_worldR = new Rectangle(0, 0, _worldW, _worldH);
		_tankSpeed = (float) (Math.sqrt(_worldW * _worldW + _worldH * _worldH) / 15.0);
		_bulletSpeed = _tankSpeed * 2.0f;
	}

	public void addBullet(final Bullet pBullet) {
		_bullets.putIfAbsent(pBullet.getUuid(), pBullet);
	}

	public void addLocalTank(final LocalTank pLocalTank) {
		final String tankUuid = pLocalTank.getTankUuid();
		_localLocalTanks.put(tankUuid, pLocalTank);
		final Tank tank = _tanks.get(tankUuid);
		if (tank != null) {
			_localTanks.putIfAbsent(tankUuid, tank);
		}
	}

	public void addTank(final Tank pTank) {
		final String tankUuid = pTank.getUuid();
		final boolean newTank = _tanks.putIfAbsent(tankUuid, pTank) == null;
		if (newTank) {
			if (_localLocalTanks.containsKey(tankUuid)) {
				_localTanks.put(tankUuid, pTank);
			}
		}
	}

	public void apply(final TankState pTankState) {
		final Tank tank = _tanks.get(pTankState._uuid);
		if (tank != null) {
			tank.apply(pTankState);
		}
	}

	public void apply(final BulletState pBulletState) {
		final Bullet bullet = _bullets.get(pBulletState._uuid);
		if (bullet != null) {
			bullet.apply(pBulletState);
		}
	}

	public int getBulletH() {
		return _bulletH;
	}

	public float getBulletSpeed() {
		return _bulletSpeed;
	}

	public int getBulletW() {
		return _bulletW;
	}

	public Collection<Bullet> getBullets() {
		return Collections.unmodifiableCollection(_bullets.values());
	}

	public Collection<LocalTank> getLocalLocalTanks() {
		return Collections.unmodifiableCollection(_localLocalTanks.values());
	}

	public Optional<LocalTank> getLocalTank(final String pTankUuid) {
		return Optional.of(_localLocalTanks.get(pTankUuid));
	}

	public Collection<Tank> getLocalTanks() {
		return Collections.unmodifiableCollection(_localTanks.values());
	}

	public int getPiecesX() {
		return _piecesX;
	}

	public int getPiecesY() {
		return _piecesY;
	}

	public Rules getRules() {
		return _rules.get();
	}

	public List<Object> getSharedState() {
		// Only add objects shared on all network nodes (eg. not LocalTanks)
		final List<Object> objects = new ArrayList<>();
		objects.addAll(_tanks.values());
		objects.addAll(_bullets.values());
		objects.add(getRules());
		return objects;
	}

	public Optional<Tank> getTank(final String pTankUuid) {
		return Optional.of(_tanks.get(pTankUuid));
	}

	public int getTankH() {
		return _tankH;
	}

	public float getTankSpeed() {
		return _tankSpeed;
	}

	public int getTankW() {
		return _tankW;
	}

	public Collection<Tank> getTanks() {
		return Collections.unmodifiableCollection(_tanks.values());
	}

	public int getTurretH() {
		return _turretH;
	}

	public int getTurretW() {
		return _turretW;
	}

	public int getWorldH() {
		return _worldH;
	}

	public int getWorldW() {
		return _worldW;
	}

	public void removeBullet(final String pBulletUuid) {
		_bullets.remove(pBulletUuid);
	}

	public void removeBullets(final Predicate<? super Bullet> pPredicate) {
		final Iterator<Bullet> bulletI = _bullets.values().iterator();
		while (bulletI.hasNext()) {
			final Bullet bullet = bulletI.next();
			if (pPredicate.test(bullet)) {
				bulletI.remove();
			}
		}
	}

	public void removeTanks(final Predicate<? super Tank> pPredicate) {
		final Iterator<Tank> tankI = _tanks.values().iterator();
		while (tankI.hasNext()) {
			final Tank tank = tankI.next();
			if (pPredicate.test(tank)) {
				tankI.remove();
				final String tankUuid = tank.getUuid();
				_localLocalTanks.remove(tankUuid);
				_localTanks.remove(tankUuid);
			}
		}
	}

	public void setRules(final Rules pRules) {
		_rules.set(pRules);
	}

	public boolean worldContains(final float pX, final float pY) {
		return _worldR.contains(pX, pY);
	}

}
