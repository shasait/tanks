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

import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public abstract class AbstractMovableGameObject<S extends AbstractState<S>> extends AbstractGameObject<S> {

	private final AtomicReference<Vector2> _moveVectorHolder = new AtomicReference<>();

	protected AbstractMovableGameObject(final String pOwnerAddress, final int pWidth, final int pHeight) {
		super(pOwnerAddress, pWidth, pHeight);
	}

	public final Vector2 getMoveVector() {
		return _moveVectorHolder.get();
	}

	public final void move(final float pDistance) {
		transformState(pState -> true, pState -> move(pDistance, pState));
	}

	public final void move(final float pDistance, final S pState) {
		final Vector2 moveVector = getMoveVector();
		pState._centerX += moveVector.x * pDistance;
		pState._centerY += moveVector.y * pDistance;
	}

	protected void afterStateChange(final S pOldState, final S pNewState) {
		if (pOldState == null || pOldState._rotation != pNewState._rotation) {
			updateMoveVector(pNewState);
		}
		super.afterStateChange(pOldState, pNewState);
	}

	protected void fillSerialized(final AbstractMovableGameObjectSerialized<S> pSerialized) {
		super.fillSerialized(pSerialized);
	}

	private void updateMoveVector(final S pState) {
		final Vector2 newMoveVector = new Vector2();
		newMoveVector.x = 0;
		newMoveVector.y = 1;
		newMoveVector.rotate(pState._rotation);
		_moveVectorHolder.set(newMoveVector);
	}

}
