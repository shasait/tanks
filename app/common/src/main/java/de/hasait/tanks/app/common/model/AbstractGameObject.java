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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public abstract class AbstractGameObject<S extends AbstractState<S>> implements Serializable {

	private final String _ownerAddress;
	private final AtomicReference<S> _stateHolder = new AtomicReference<>();

	private final int _width;
	private final int _height;
	private final float _whDist2;
	private final AtomicReference<Polygon> _boundsHolder = new AtomicReference<>();

	protected AbstractGameObject(final String pOwnerAddress, final int pWidth, final int pHeight) {
		_ownerAddress = pOwnerAddress;
		_width = pWidth;
		_height = pHeight;
		_whDist2 = (float) Math.sqrt(_width * _width + _height * _height) / 2.0f;
	}

	public final void apply(final S pNewState) {
		Objects.requireNonNull(pNewState);

		final S oldState = _stateHolder.getAndSet(pNewState);
		afterStateChange(oldState, pNewState);
	}

	public final boolean contains(final float pX, final float pY) {
		return _boundsHolder.get().contains(pX, pY);
	}

	public final Polygon createBounds(final S pState) {
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

	public final Polygon getBounds() {
		return _boundsHolder.get();
	}

	public final int getHeight() {
		return _height;
	}

	public final String getOwnerAddress() {
		return _ownerAddress;
	}

	public final S getState() {
		return _stateHolder.get();
	}

	public final String getUuid() {
		return getState()._uuid;
	}

	public final float getWhDist2() {
		return _whDist2;
	}

	public final int getWidth() {
		return _width;
	}

	/**
	 * @return distance if intersecting; otherwise null.
	 */
	public final Float intersects(final AbstractGameObject<?> pOther) {
		return intersects(pOther.getBounds(), pOther.getWhDist2());
	}

	/**
	 * @return distance if intersecting; otherwise null.
	 */
	public final Float intersects(final Polygon pOtherBounds, final float pOtherWhDist2) {
		if (pOtherBounds == null) {
			return null;
		}
		final S state1 = getState();

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

	public final void setCenter(final float pCenterX, final float pCenterY) {
		transformState(pState -> pState._centerX != pCenterX && pState._centerY != pCenterY, pState -> {
			pState._centerX = pCenterX;
			pState._centerY = pCenterY;
		});

	}

	public final void setRotation(final float pRotation) {
		transformState(pState -> pState._rotation != pRotation, pState -> pState._rotation = pRotation);
	}

	protected void afterStateChange(final S pOldState, final S pNewState) {
		if (pOldState == null
				|| pOldState._centerX != pNewState._centerX
				|| pOldState._centerY != pNewState._centerY
				|| pOldState._rotation != pNewState._rotation) {
			updateBounds(pNewState);
		}
	}

	protected void fillSerialized(final AbstractGameObjectSerialized<S> pSerialized) {
		pSerialized._ownerAddress = getOwnerAddress();
		pSerialized._state = getState();
		pSerialized._width = _width;
		pSerialized._height = _height;
	}

	protected final void transformState(final Predicate<S> pStillNeeded, final Consumer<S> pStateChange) {
		boolean success = false;
		while (!success) {
			final S oldState = _stateHolder.get();
			if (!pStillNeeded.test(oldState)) {
				return;
			}

			final S newState = oldState.clone();
			pStateChange.accept(newState);
			success = _stateHolder.compareAndSet(oldState, newState);
			if (success) {
				afterStateChange(oldState, newState);
			}
		}
	}

	private void updateBounds(final S pState) {
		_boundsHolder.set(createBounds(pState));
	}

}
