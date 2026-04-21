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
import de.hasait.tanks.util.common.Abstract2DScreen;
import de.hasait.tanks.util.common.input.ConfiguredAction;

import java.io.Serializable;

/**
 *
 */
public class PlayerConfig implements Serializable {

    private String _name;

    private ConfiguredAction _moveForward, _moveBackward;
    private ConfiguredAction _rotateLeft, _rotateRight;
    private ConfiguredAction _turrentRotateLeft, _turrentRotateRight;
    private ConfiguredAction _fire;

    public void fillActions(final TankActions pTankActions) {
        pTankActions._moveForward = getState(_moveForward);
        pTankActions._moveBackward = getState(_moveBackward);
        pTankActions._rotateLeft = getState(_rotateLeft);
        pTankActions._rotateRight = getState(_rotateRight);
        pTankActions._turrentRotateLeft = getState(_turrentRotateLeft);
        pTankActions._turrentRotateRight = getState(_turrentRotateRight);
        pTankActions._fire = getState(_fire);
    }

    public ConfiguredAction getFire() {
        return _fire;
    }

    public void setFire(final ConfiguredAction pFire) {
        _fire = pFire;
    }

    public ConfiguredAction getMoveBackward() {
        return _moveBackward;
    }

    public void setMoveBackward(final ConfiguredAction pMoveBackward) {
        _moveBackward = pMoveBackward;
    }

    public ConfiguredAction getMoveForward() {
        return _moveForward;
    }

    public void setMoveForward(final ConfiguredAction pMoveForward) {
        _moveForward = pMoveForward;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String pName) {
        _name = pName;
    }

    public ConfiguredAction getRotateLeft() {
        return _rotateLeft;
    }

    public void setRotateLeft(final ConfiguredAction pRotateLeft) {
        _rotateLeft = pRotateLeft;
    }

    public ConfiguredAction getRotateRight() {
        return _rotateRight;
    }

    public void setRotateRight(final ConfiguredAction pRotateRight) {
        _rotateRight = pRotateRight;
    }

    public ConfiguredAction getTurrentRotateLeft() {
        return _turrentRotateLeft;
    }

    public void setTurrentRotateLeft(final ConfiguredAction pTurrentRotateLeft) {
        _turrentRotateLeft = pTurrentRotateLeft;
    }

    public ConfiguredAction getTurrentRotateRight() {
        return _turrentRotateRight;
    }

    public void setTurrentRotateRight(final ConfiguredAction pTurrentRotateRight) {
        _turrentRotateRight = pTurrentRotateRight;
    }

    public void initActions(final Abstract2DScreen<?> pScreen) {
        _moveForward.init(pScreen);
        _moveBackward.init(pScreen);
        _rotateLeft.init(pScreen);
        _rotateRight.init(pScreen);
        _turrentRotateLeft.init(pScreen);
        _turrentRotateRight.init(pScreen);
        _fire.init(pScreen);
    }

    private float getState(final ConfiguredAction pAction) {
        return pAction != null ? pAction.getState() : 0.0f;
    }

}
