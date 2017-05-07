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

import java.io.ObjectStreamException;
import java.util.UUID;

/**
 *
 */
public class Tank extends AbstractMovableGameObject<TankState> {

	private static TankState createState(final long pSpawnAtMillis) {
		final TankState state = new TankState();
		state._uuid = UUID.randomUUID().toString();
		state._spawnAtMillis = pSpawnAtMillis;
		return state;
	}

	private final String _name;


	public Tank(final String pOwnerAddress, final int pWidth, final int pHeight, final String pName, final TankState pState) {
		super(pOwnerAddress, pWidth, pHeight);

		_name = pName;

		apply(pState);
	}

	public Tank(final String pOwnerAddress, final int pWidth, final int pHeight, final String pName, final long pSpawnAtMillis) {
		this(pOwnerAddress, pWidth, pHeight, pName, createState(pSpawnAtMillis));
	}

	public String getName() {
		return _name;
	}

	public boolean isMyBullet(final Bullet pBullet) {
		return getUuid().equals(pBullet.getTankUuid());
	}

	public void setTurretRotation(final float pTurretRotation) {
		transformState(pState -> pState._turretRotation != pTurretRotation, pState -> pState._turretRotation = pTurretRotation);
	}

	private Object writeReplace() throws ObjectStreamException {
		final TankSerialized serialized = new TankSerialized();
		fillSerialized(serialized);
		serialized._name = _name;
		return serialized;
	}

}
