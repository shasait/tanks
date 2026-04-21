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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class GameConfig implements Serializable {

    private final List<PlayerConfig> _players = new ArrayList<>();
    private final Map<String, String> _networkSystemProperties = new HashMap<>();
    private String _roomName;
    private int _wishPiecesX, _wishPiecesY;
    private String _networkStack;

    public String getNetworkStack() {
        return _networkStack;
    }

    public void setNetworkStack(final String pNetworkStack) {
        _networkStack = pNetworkStack;
    }

    public Map<String, String> getNetworkSystemProperties() {
        return _networkSystemProperties;
    }

    public List<PlayerConfig> getPlayers() {
        return _players;
    }

    public String getRoomName() {
        return _roomName;
    }

    public void setRoomName(final String pRoomName) {
        _roomName = pRoomName;
    }

    public int getWishPiecesX() {
        return _wishPiecesX;
    }

    public void setWishPiecesX(final int pWishPiecesX) {
        _wishPiecesX = pWishPiecesX;
    }

    public int getWishPiecesY() {
        return _wishPiecesY;
    }

    public void setWishPiecesY(final int pWishPiecesY) {
        _wishPiecesY = pWishPiecesY;
    }

    public void putNetworkSystemProperty(final String pKey, final String pValue) {
        _networkSystemProperties.put(pKey, pValue);
    }

}
