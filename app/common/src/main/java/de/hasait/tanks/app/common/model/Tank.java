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
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public class Tank extends AbstractGameObject<TankState> {

	private static TankState createState(final long pSpawnAtMillis) {
		final TankState state = new TankState();
		state._uuid = UUID.randomUUID().toString();
		state._respawnAtMillis = pSpawnAtMillis;
		return state;
	}

	private final String _name;
	private final float _width;
	private final float _height;
	private final float _whDist2;
	private final AtomicReference<Polygon> _boundsHolder = new AtomicReference<>();


	public Tank(final String pOwnerAddress, final String pName, final float pWidth, final float pHeight, final TankState pState) {
		super(pOwnerAddress);

		_name = pName;
		_width = pWidth;
		_height = pHeight;
		_whDist2 = (float) Math.sqrt(_width * _width + _height * _height) / 2.0f;

		apply(pState);
	}

	public Tank(final String pOwnerAddress, final String pName, final float pWidth, final float pHeight, final long pSpawnAtMillis) {
		this(pOwnerAddress, pName, pWidth, pHeight, createState(pSpawnAtMillis));
	}

	public boolean contains(final float pX, final float pY) {
		return _boundsHolder.get().contains(pX, pY);
	}

	public String getName() {
		return _name;
	}

	/**
	 * @return distance if intersecting; otherwise null.
	 */
	public Float intersects(final Tank pTank) {
		if (pTank == null) {
			return null;
		}
		if (getUuid().equals(pTank.getUuid())) {
			return null;
		}

		final TankState state1 = getState();
		final TankState state2 = pTank.getState();

		final float dst = Vector2.dst(state1._centerX, state1._centerY, state2._centerX, state2._centerY);
		if (dst > _whDist2 + pTank._whDist2) {
			return null;
		}

		final Polygon p1 = _boundsHolder.get();
		final Polygon p2 = pTank._boundsHolder.get();

		if (Intersector.intersectPolygons(p1, p2, new Polygon())) {
			return dst;
		}

		return null;
	}

	public boolean isMyBullet(final Bullet pBullet) {
		return getUuid().equals(pBullet.getTankUuid());
	}

	public void setTurretRotation(final float pTurretRotation) {
		transformState(pState -> pState._turretRotation != pTurretRotation, pState -> pState._turretRotation = pTurretRotation);
	}

	@Override
	protected void afterStateChange(final TankState pOldState, final TankState pNewState) {
		super.afterStateChange(pOldState, pNewState);
		if (pOldState == null
				|| pOldState._centerX != pNewState._centerX
				|| pOldState._centerY != pNewState._centerY
				|| pOldState._rotation != pNewState._rotation) {
			updateBounds(pNewState);
		}
	}

	private void updateBounds(final TankState pState) {
		final float w2 = _width / 2.0f;
		final float h2 = _height / 2.0f;
		final Polygon bounds = new Polygon(new float[]{
				-w2,
				h2,
				w2,
				h2,
				w2,
				-h2,
				-w2,
				-h2
		});
		bounds.setPosition(pState._centerX, pState._centerY);
		bounds.setRotation(pState._rotation);
		_boundsHolder.set(bounds);
	}

	private Object writeReplace() throws ObjectStreamException {
		final SerializableTank serializable = new SerializableTank();
		serializable._ownerAddress = getOwnerAddress();
		serializable._name = _name;
		serializable._width = _width;
		serializable._height = _height;
		serializable._state = getState();
		return serializable;
	}

}
