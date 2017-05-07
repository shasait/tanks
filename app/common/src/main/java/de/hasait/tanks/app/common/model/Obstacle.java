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
public class Obstacle extends AbstractGameObject<ObstacleState> {

	private static ObstacleState createState(final float pCenterX, final float pCenterY, final float pRotation) {
		final ObstacleState state = new ObstacleState();
		state._uuid = UUID.randomUUID().toString();
		state._centerX = pCenterX;
		state._centerY = pCenterY;
		state._rotation = pRotation;
		return state;
	}

	public Obstacle(final String pOwnerAddress, final int pWidth, final int pHeight, final ObstacleState pState) {
		super(pOwnerAddress, pWidth, pHeight);

		apply(pState);
	}

	public Obstacle(final String pOwnerAddress, final int pWidth, final int pHeight, final float pCenterX, final float pCenterY, final float pRotation) {
		this(pOwnerAddress, pWidth, pHeight, createState(pCenterX, pCenterY, pRotation));
	}

	private Object writeReplace() throws ObjectStreamException {
		final ObstacleSerialized serialized = new ObstacleSerialized();
		fillSerialized(serialized);
		return serialized;
	}

}
