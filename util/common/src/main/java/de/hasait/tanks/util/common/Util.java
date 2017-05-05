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

package de.hasait.tanks.util.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public final class Util {

	public static final String EMPTY = "";

	public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

	public static boolean isBlank(final CharSequence pText) {
		final int length;
		if (pText == null || ((length = pText.length()) == 0)) {
			return true;
		}
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(pText.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(final CharSequence pText) {
		return pText == null || pText.length() == 0;
	}

	public static String repeat(final String pText, final int pCount) {
		if (pText == null) {
			return null;
		}
		if (pCount == 0) {
			return EMPTY;
		}
		if (pCount == 1) {
			return pText;
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pCount; i++) {
			sb.append(pText);
		}
		return sb.toString();
	}

	public static Object serializeFromString(final String pString) {
		try {
			final byte[] bytes = Base64.getDecoder().decode(pString);
			final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			return ois.readObject();
		} catch (final Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	public static String serializeToString(final Object pObject) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(pObject);
			oos.close();
			final byte[] bytes = baos.toByteArray();
			return Base64.getEncoder().encodeToString(bytes);
		} catch (final Exception pE) {
			throw new RuntimeException(pE);
		}
	}

	private Util() {
		super();
	}
}
