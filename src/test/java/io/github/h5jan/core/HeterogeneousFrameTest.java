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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.ComplexDoubleDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.StringDataset;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.io.Configuration;
import io.github.h5jan.io.DataFrameReader;

/**
 * 
 * @author Matthew Gerring
 *
 */
public class HeterogeneousFrameTest extends AbstractH5JanTest {
	
	private DataFrameReader reader;

	@Before
	public void before() {
		this.reader = new DataFrameReader();
	}

	@Test
	public void float1Int() throws Exception {
		
		int[] data = new int[256];
		Arrays.fill(data, 3);
		Dataset inject = DatasetFactory.createFromObject(IntegerDataset.class, data, 256);
		round("float1Int", false, inject);
	}

	@Test
	public void float1String() throws Exception {
				
		String[] data = new String[256];
		Arrays.fill(data, "Hello World");
		Dataset inject = DatasetFactory.createFromObject(StringDataset.class, data, 256);
		round("float1String", true, inject);
	}

	@Test
	public void float1RGB() throws Exception {
		
		Dataset inject = DatasetFactory.compoundZeros(3, RGBDataset.class, 256);
		round("float1RGB", true, inject);
	}


	@Test
	public void float1Eigen() throws Exception {
		
		double[] rev = new double[] {1,2,3,4,5,6,7,8,9,10};
		double[] iev = new double[] {1,2,3,4,5,6,7,8,9,10};

		Dataset inject = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, rev, iev);
		round("float1Eigen", false, inject);
	}

	@Test
	public void float2Int() throws Exception {
		
		int[] data = new int[256];
		Arrays.fill(data, 3);
		Dataset inject = DatasetFactory.createFromObject(IntegerDataset.class, data, 256);
		round("float2Int", false, inject, inject.clone());
	}

	@Test
	public void float2String() throws Exception {
				
		String[] data = new String[256];
		Arrays.fill(data, "Hello World");
		Dataset inject = DatasetFactory.createFromObject(StringDataset.class, data, 256);
		round("float2String", true, inject, inject.clone());
	}

	@Test
	public void float2RGB() throws Exception {
		
		Dataset inject = DatasetFactory.compoundZeros(3, RGBDataset.class, 256);
		round("float2RGB", true, inject, inject.clone());
	}


	@Test
	public void float2Eigen() throws Exception {
		
		double[] rev = new double[] {1,2,3,4,5,6,7,8,9,10};
		double[] iev = new double[] {1,2,3,4,5,6,7,8,9,10};

		Dataset inject = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, rev, iev);
		round("float2Eigen", false, inject, inject.clone());
	}

	private void round(String name, boolean isAux, Dataset... inject) throws Exception {
		
		Dataset[] sets = new Dataset[2+inject.length];
		for (int i = 0; i < 2; i++) {
			sets[i] = Random.rand(inject[0].getShape());
			sets[i].setName("slice_"+i);
 		}
		
		for (int i = 0; i < inject.length; i++) {
			inject[i].setName(name+"_"+i);
			sets[2+i] = inject[i];
		}
		io(name, isAux, sets);
	}

	@Test
	public void float1IntFloat() throws Exception {
		
		int[] data = new int[256];
		Arrays.fill(data, 3);
		Dataset inject = DatasetFactory.createFromObject(IntegerDataset.class, data, 256);
		roundPad("float1IntFloat", false, inject);
	}

	@Test
	public void float1StringFloat() throws Exception {
				
		String[] data = new String[256];
		Arrays.fill(data, "Hello World");
		Dataset inject = DatasetFactory.createFromObject(StringDataset.class, data, 256);
		roundPad("float1StringFloat", true, inject);
	}

	@Test
	public void float1RGBFloat() throws Exception {
		
		Dataset inject = DatasetFactory.compoundZeros(3, RGBDataset.class, 256);
		roundPad("float1RGBFloat", true, inject);
	}


	@Test
	public void float1EigenFloat() throws Exception {
		
		double[] rev = new double[] {1,2,3,4,5,6,7,8,9,10};
		double[] iev = new double[] {1,2,3,4,5,6,7,8,9,10};

		Dataset inject = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, rev, iev);
		roundPad("float1EigenFloat", false, inject);
	}

	@Test
	public void float2IntFloat() throws Exception {
		
		int[] data = new int[256];
		Arrays.fill(data, 3);
		Dataset inject = DatasetFactory.createFromObject(IntegerDataset.class, data, 256);
		roundPad("float2IntFloat", false, inject, inject.clone());
	}

	@Test
	public void float2StringFloat() throws Exception {
				
		String[] data = new String[256];
		Arrays.fill(data, "Hello World");
		Dataset inject = DatasetFactory.createFromObject(StringDataset.class, data, 256);
		roundPad("float2StringFloat", true, inject, inject.clone());
	}

	@Test
	public void float2RGBFloat() throws Exception {
		
		Dataset inject = DatasetFactory.compoundZeros(3, RGBDataset.class, 256);
		roundPad("float2RGBFloat", true, inject, inject.clone());
	}


	@Test
	public void float2EigenFloat() throws Exception {
		
		double[] rev = new double[] {1,2,3,4,5,6,7,8,9,10};
		double[] iev = new double[] {1,2,3,4,5,6,7,8,9,10};

		Dataset inject = DatasetFactory.createComplexDataset(ComplexDoubleDataset.class, rev, iev);
		roundPad("float2EigenFloat", false, inject, inject.clone());
	}

	private void roundPad(String name, boolean isAux, Dataset... inject) throws Exception {
		
		Dataset[] sets = new Dataset[4+inject.length];
		for (int i = 0; i < 2; i++) {
			sets[i] = Random.rand(inject[0].getShape());
			sets[i].setName("slice_"+i);
 		}
		
		for (int i = 0; i < inject.length; i++) {
			inject[i].setName(name+"_"+i);
			sets[2+i] = inject[i];
		}
		
		for (int i = 2+inject.length; i < 4+inject.length; i++) {
			sets[i] = Random.rand(inject[0].getShape());
			sets[i].setName("slice_"+i);
 		}

		io(name, isAux, sets);
	}


	private void io(String name, boolean isAux, Dataset... sets) throws Exception {
		
		// Make a test frame
		DataFrame frame = new DataFrame("some_columns", Dataset.FLOAT32, sets);
		
		// Save to HDF5
		String path = "test-scratch/hetero_test/"+name+".h5";
		frame.to_hdf(path, "/entry1/myData");
		
		Path fpath = Paths.get(path);
		DataFrame emarf = reader.read(fpath, Configuration.createEmpty(), new IMonitor.Stub());
		
		if (isAux) {
			assertTrue(emarf.containsKey(name+"_0"));
		} else {
			assertNotNull(emarf.get(name+"_0"));
		}
		
		// Now write emarf and check that 
		path = "test-scratch/hetero_test/"+name+"_v2.h5";
		fpath = Paths.get(path);
		emarf.to_hdf(path, "/entry1/myData");
		DataFrame emarf2 = reader.read(fpath, Configuration.createEmpty(), new IMonitor.Stub());
		
		if (isAux) {
			assertTrue(emarf2.containsKey(name+"_0"));
		} else {
			assertNotNull(emarf2.get(name+"_0"));
		}

	}

}
