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
package io.github.h5jan.core;

import java.util.Arrays;

import org.eclipse.january.dataset.ComplexDoubleDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.StringDataset;
import org.junit.Test;

/**
 * 
 * @author Matthew Gerring
 *
 */
public class HeterogeneousFrameTest extends AbstractH5JanTest {

	@Test
	public void floatInt() throws Exception {
		
		int[] data = new int[256];
		Arrays.fill(data, 3);
		Dataset inject = DatasetFactory.createFromObject(IntegerDataset.class, data, 256);
		write("floatInt", inject);
	}

	@Test
	public void floatString() throws Exception {
				
		String[] data = new String[256];
		Arrays.fill(data, "Hello World");
		Dataset inject = DatasetFactory.createFromObject(StringDataset.class, data, 256);
		write("floatString", inject);
	}

	@Test
	public void floatRGB() throws Exception {
		
		Dataset inject = DatasetFactory.compoundZeros(3, RGBDataset.class, 256);
		write("floatRGB", inject);
	}


	@Test
	public void floatEigen() throws Exception {
		
		double[] rev = new double[] {1,2,3,4,5,6,7,8,9,10};
		double[] iev = new double[] {1,2,3,4,5,6,7,8,9,10};

		Dataset inject = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, rev, iev);
		write("floatEigen", inject);
	}

	private void write(String name, Dataset inject) throws Exception {
		
		Dataset[] sets = new Dataset[3];
		for (int i = 0; i < sets.length-1; i++) {
			sets[i] = Random.rand(inject.getShape());
			sets[i].setName("slice_"+i);
 		}
		
		inject.setName(name);
		sets[2] = inject;
		
		// Make a test frame
		DataFrame frame = new DataFrame("some_columns", Dataset.FLOAT32, sets);
		
		// Save to HDF5
		frame.to_hdf("test-scratch/hetero_test/"+name+".h5", "/entry1/myData");
	}


}
