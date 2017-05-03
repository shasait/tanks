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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GameConfig {

	private final List<PlayerConfig> _players = new ArrayList<>();

	private final String _roomName;

	private final int _wishPiecesX, _wishPiecesY;

	public GameConfig(final String pRoomName, final int pWishPiecesX, final int pWishPiecesY) {
		super();

		_roomName = pRoomName;
		_wishPiecesX = pWishPiecesX;
		_wishPiecesY = pWishPiecesY;
	}

	public List<PlayerConfig> getPlayers() {
		return _players;
	}

	public String getRoomName() {
		return _roomName;
	}

	public int getWishPiecesX() {
		return _wishPiecesX;
	}

	public int getWishPiecesY() {
		return _wishPiecesY;
	}

}
