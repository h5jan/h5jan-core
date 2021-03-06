/*-
 *******************************************************************************
 * Copyright (c) 2019 Halliburton International, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package io.github.h5jan.core.boundary;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

import io.github.h5jan.core.AbstractH5JanTest;

public class TypeTest extends AbstractH5JanTest {
	
	private static final int[] types;
	static {
		types = new int[17];
		/**
		 * Booleans
		 */
		types[0] = Dataset.BOOL;

		/**
		 * Signed 8-bit integer
		 */
		types[1] = Dataset.INT8;

		/**
		 * Signed 16-bit integer
		 */
		types[2] = Dataset.INT16;

		/**
		 * Signed 32-bit integer
		 */
		types[3] = Dataset.INT32;

		/**
		 * Signed 64-bit integer
		 */
		types[4] = Dataset.INT64;

		/**
		 * 32-bit floating point
		 */
		types[5] = Dataset.FLOAT32;

		/**
		 * 64-bit floating point
		 */
		types[6] = Dataset.FLOAT64;

		/**
		 * 64-bit complex floating point (real and imaginary parts are 32-bit floats)
		 */
		types[7] = Dataset.COMPLEX64;

		/**
		 * 128-bit complex floating point (real and imaginary parts are 64-bit floats)
		 */
		types[8] = Dataset.COMPLEX128;

		/**
		 * Array of three signed 16-bit integers for RGB values
		 */
		types[9] = Dataset.RGB;

		/**
		 * Array of signed 8-bit integers
		 */
		types[10] = Dataset.ARRAYINT8;

		/**
		 * Array of signed 16-bit integers
		 */
		types[11] = Dataset.ARRAYINT16;

		/**
		 * Array of signed 32-bit integers
		 */
		types[12] = Dataset.ARRAYINT32;

		/**
		 * Array of signed 64-bit integers
		 */
		types[13] = Dataset.ARRAYINT64;

		/**
		 * Array of 32-bit floating points
		 */
		types[14] = Dataset.ARRAYFLOAT32;

		/**
		 * Array of 64-bit floating points
		 */
		types[15] = Dataset.ARRAYFLOAT64;

	}

	@SuppressWarnings("deprecation")
	@Test
	public void ones() throws Exception {
		for (int i = 0; i < 10; i++) {
			try {
				round(DatasetFactory.ones(new int[] {256, 3}, types[i]), "ones");
			} catch (Exception ne) {
				throw new Exception("Cannot process dtype "+DTypeUtils.getDTypeName(types[i], 1), ne);
			}
		}
	}
	
	@Test
	public void rgb() throws Exception {
		// This test is done above but it makes it easy to run a single failing type
		round(DatasetFactory.ones(new int[] {256, 3}, Dataset.RGB), "rgb");
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void zeros() throws Exception {
		for (int i = 0; i < types.length; i++) {
			round(DatasetFactory.zeros(new int[] {256, 3}, types[i]), "zeros");
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void strings() throws Exception {
		round(DatasetFactory.createFromList(Arrays.asList("one", "two", "three")), "strings");
	}

	@Test(expected=IllegalArgumentException.class) // Date is not yet supported.
	public void dates() throws Exception {
		round(DatasetFactory.createFromList(Arrays.asList(new Date(), new Date(), new Date())), "dates");
	}
}
