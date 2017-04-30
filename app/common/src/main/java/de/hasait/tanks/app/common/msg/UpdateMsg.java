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

package de.hasait.tanks.app.common.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hasait.tanks.app.common.model.BulletState;
import de.hasait.tanks.app.common.model.TankState;

/**
 *
 */
public class UpdateMsg implements Serializable {

	public final Set<TankState> _tanks = new HashSet<>();
	public final Set<BulletState> _bullets = new HashSet<>();
	public final Set<String> _removedBullets = new HashSet<>();
	public final Set<String> _removedTanks = new HashSet<>();
	public final List<String> _incrementDamage = new ArrayList<>();

	public boolean isEmpty() {
		return _tanks.isEmpty() && _bullets.isEmpty() && _removedBullets.isEmpty() && _removedTanks.isEmpty() && _incrementDamage.isEmpty();
	}

}
