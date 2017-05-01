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

import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public abstract class AbstractObject<S extends AbstractState<S>> implements Serializable {

	private final String _ownerAddress;

	private final AtomicReference<S> _stateHolder = new AtomicReference<>();
	private final AtomicReference<Vector2> _moveVectorHolder = new AtomicReference<>();

	protected AbstractObject(final String pOwnerAddress) {
		_ownerAddress = pOwnerAddress;
	}

	public final void apply(final S pNewState) {
		Objects.requireNonNull(pNewState);

		final S oldState = _stateHolder.getAndSet(pNewState);
		afterStateChange(oldState, pNewState);
	}

	public final String getOwnerAddress() {
		return _ownerAddress;
	}

	public final Vector2 getMoveVector() {
		return _moveVectorHolder.get();
	}

	public final S getState() {
		return _stateHolder.get();
	}

	public final String getUuid() {
		return getState()._uuid;
	}

	public final void move(final float pDistance) {
		transformState(pState -> true, pState -> move(pDistance, pState));
	}

	public final void move(final float pDistance, final S pState) {
		final Vector2 moveVector = getMoveVector();
		pState._centerX += moveVector.x * pDistance;
		pState._centerY += moveVector.y * pDistance;
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
		if (pOldState == null || pOldState._rotation != pNewState._rotation) {
			updateMoveVector(pNewState);
		}
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

	private void updateMoveVector(final S pState) {
		final Vector2 newMoveVector = new Vector2();
		newMoveVector.x = 0;
		newMoveVector.y = 1;
		newMoveVector.rotate(pState._rotation);
		_moveVectorHolder.set(newMoveVector);
	}

}
