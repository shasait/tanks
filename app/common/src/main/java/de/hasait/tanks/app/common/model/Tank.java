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
		state._spawnAtMillis = pSpawnAtMillis;
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

	public Polygon createBounds(final TankState pState) {
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
		return bounds;
	}

	public Polygon getBounds() {
		return _boundsHolder.get();
	}

	public String getName() {
		return _name;
	}

	public float getWhDist2() {
		return _whDist2;
	}

	/**
	 * @return distance if intersecting; otherwise null.
	 */
	public Float intersects(final Polygon pOtherBounds, final float pOtherWhDist2) {
		if (pOtherBounds == null) {
			return null;
		}
		final TankState state1 = getState();

		final float distance = Vector2.dst(state1._centerX, state1._centerY, pOtherBounds.getX(), pOtherBounds.getY());
		if (distance > _whDist2 + pOtherWhDist2) {
			return null;
		}

		final Polygon bounds = _boundsHolder.get();

		if (Intersector.intersectPolygons(bounds, pOtherBounds, new Polygon())) {
			return distance;
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
		_boundsHolder.set(createBounds(pState));
	}

	private Object writeReplace() throws ObjectStreamException {
		final TankSerialized serialized = new TankSerialized();
		serialized._ownerAddress = getOwnerAddress();
		serialized._name = _name;
		serialized._width = _width;
		serialized._height = _height;
		serialized._state = getState();
		return serialized;
	}

}
