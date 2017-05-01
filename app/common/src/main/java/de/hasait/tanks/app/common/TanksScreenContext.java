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

import de.hasait.tanks.util.common.AbstractScreenContext;
import de.hasait.tanks.util.common.Game;

/**
 *
 */
public class TanksScreenContext extends AbstractScreenContext {

	private final int _tankW, _tankH;
	private final int _turretW, _turretH;
	private final int _bulletW, _bulletH;
	private final float _tankSpeed = 100.0f;
	private final float _bulletSpeed = _tankSpeed * 2.0f;

	public TanksScreenContext(Game<TanksScreenContext> pGame) {
		super(pGame, 1280, 720);
		_tankW = 32;
		_tankH = _tankW;
		_turretW = _tankW / 2;
		_turretH = _tankH * 3 / 4;
		_bulletW = _tankW / 4;
		_bulletH = _tankH / 3;
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

	public int getTankH() {
		return _tankH;
	}

	public float getTankSpeed() {
		return _tankSpeed;
	}

	public int getTankW() {
		return _tankW;
	}

	public int getTurretH() {
		return _turretH;
	}

	public int getTurretW() {
		return _turretW;
	}

}
