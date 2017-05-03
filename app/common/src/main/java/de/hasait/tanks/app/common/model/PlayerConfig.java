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

import de.hasait.tanks.app.common.TankActions;

/**
 *
 */
public class PlayerConfig {

	private final String _name;

	private ConfiguredAction _moveForward, _moveBackward;
	private ConfiguredAction _rotateLeft, _rotateRight;
	private ConfiguredAction _turrentRotateLeft, _turrentRotateRight;
	private ConfiguredAction _fire;

	public PlayerConfig(final String pName) {
		_name = pName;
	}

	public void fillActions(final TankActions pTankActions) {
		pTankActions._moveForward = isActive(_moveForward);
		pTankActions._moveBackward = isActive(_moveBackward);
		pTankActions._rotateLeft = isActive(_rotateLeft);
		pTankActions._rotateRight = isActive(_rotateRight);
		pTankActions._turrentRotateLeft = isActive(_turrentRotateLeft);
		pTankActions._turrentRotateRight = isActive(_turrentRotateRight);
		pTankActions._fire = isActive(_fire);
	}

	public ConfiguredAction getFire() {
		return _fire;
	}

	public ConfiguredAction getMoveBackward() {
		return _moveBackward;
	}

	public ConfiguredAction getMoveForward() {
		return _moveForward;
	}

	public String getName() {
		return _name;
	}

	public ConfiguredAction getRotateLeft() {
		return _rotateLeft;
	}

	public ConfiguredAction getRotateRight() {
		return _rotateRight;
	}

	public ConfiguredAction getTurrentRotateLeft() {
		return _turrentRotateLeft;
	}

	public ConfiguredAction getTurrentRotateRight() {
		return _turrentRotateRight;
	}

	public void setFire(final ConfiguredAction pFire) {
		_fire = pFire;
	}

	public void setMoveBackward(final ConfiguredAction pMoveBackward) {
		_moveBackward = pMoveBackward;
	}

	public void setMoveForward(final ConfiguredAction pMoveForward) {
		_moveForward = pMoveForward;
	}

	public void setRotateLeft(final ConfiguredAction pRotateLeft) {
		_rotateLeft = pRotateLeft;
	}

	public void setRotateRight(final ConfiguredAction pRotateRight) {
		_rotateRight = pRotateRight;
	}

	public void setTurrentRotateLeft(final ConfiguredAction pTurrentRotateLeft) {
		_turrentRotateLeft = pTurrentRotateLeft;
	}

	public void setTurrentRotateRight(final ConfiguredAction pTurrentRotateRight) {
		_turrentRotateRight = pTurrentRotateRight;
	}

	private boolean isActive(final ConfiguredAction pAction) {
		return pAction != null && pAction.isActive();
	}

}
