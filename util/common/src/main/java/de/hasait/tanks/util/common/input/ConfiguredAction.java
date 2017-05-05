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

package de.hasait.tanks.util.common.input;

import java.io.Serializable;

import com.badlogic.gdx.utils.Disposable;

import de.hasait.tanks.util.common.Abstract2DScreen;

/**
 *
 */
public interface ConfiguredAction extends Disposable, Serializable {

	/**
	 * @return Return state between 0 and 1.
	 */
	float getState();

	void init(final Abstract2DScreen<?> pScreen);

}
