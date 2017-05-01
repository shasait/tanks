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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class State implements Serializable {

	public final Map<String, Tank> _tanks = new ConcurrentHashMap<>();
	public final Map<String, Bullet> _bullets = new ConcurrentHashMap<>();

	public final Rules _rules = new Rules();

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

}
