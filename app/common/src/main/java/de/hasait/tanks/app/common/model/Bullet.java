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
public class Bullet extends AbstractGameObject<BulletState> {

	private static BulletState createState(final float pCenterX, final float pCenterY, final float pRotation) {
		final BulletState state = new BulletState();
		state._uuid = UUID.randomUUID().toString();
		state._centerX = pCenterX;
		state._centerY = pCenterY;
		state._rotation = pRotation;
		return state;
	}

	private final String _tankUuid;

	public Bullet(final String pOwnerAddress, final String pTankUuid, final BulletState pState) {
		super(pOwnerAddress);

		_tankUuid = pTankUuid;

		apply(pState);
	}

	public Bullet(final String pOwnerAddress, final String pTankUuid, final float pCenterX, final float pCenterY, final float pRotation) {
		this(pOwnerAddress, pTankUuid, createState(pCenterX, pCenterY, pRotation));
	}

	public String getTankUuid() {
		return _tankUuid;
	}


	private Object writeReplace() throws ObjectStreamException {
		final BulletSerialized serialized = new BulletSerialized();
		serialized._ownerAddress = getOwnerAddress();
		serialized._tankUuid = _tankUuid;
		serialized._state = getState();
		return serialized;
	}

}
