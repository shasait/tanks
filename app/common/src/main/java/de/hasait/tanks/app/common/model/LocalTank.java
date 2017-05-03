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

import java.util.concurrent.atomic.AtomicInteger;

import de.hasait.tanks.app.common.TankActions;

/**
 *
 */
public class LocalTank {

	private final String _tankUuid;
	private final PlayerConfig _playerConfig;
	private final AtomicInteger _tankDamageIncrement = new AtomicInteger();
	private long _lastShotTimeMillis;

	public LocalTank(final String pTankUuid, final PlayerConfig pPlayerConfig) {
		super();

		_tankUuid = pTankUuid;
		_playerConfig = pPlayerConfig;
	}

	public void fillActions(final TankActions pTankActions) {
		_playerConfig.fillActions(pTankActions);
	}

	public int getAndResetDamageIncrement() {
		return _tankDamageIncrement.getAndSet(0);
	}

	public long getLastShotTimeMillis() {
		return _lastShotTimeMillis;
	}

	public String getTankUuid() {
		return _tankUuid;
	}

	public void incrementDamageIncrement() {
		_tankDamageIncrement.incrementAndGet();
	}

	public void setLastShotTimeMillis(final long pLastShotTimeMillis) {
		_lastShotTimeMillis = pLastShotTimeMillis;
	}

}
