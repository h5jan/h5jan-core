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
package io.github.gerring.h5jan.examples;

import java.util.Arrays;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import io.github.gerring.h5jan.Appender;
import io.github.gerring.h5jan.DataFrame;
import io.github.gerring.h5jan.NxsFile;

/**
 * Test which aggregates the raw read operations possible to and
 * from either January or tables.
 * 
 * @author Matthew Gerring
 *
 */
@FixMethodOrder
public class DatasetWriteExample {
	
	private NxsFile nfile;
	
	@Before
	public void read() throws Exception {
		nfile = NxsFile.create("test-scratch/write_example/block.h5");
	}
	
	@After
	public void close() throws NexusException {
		nfile.close();
	}
	
	@Test
	public void writeDatasetLazyImages() throws Exception {
		
		// We create a place to put our data
		ILazyWriteableDataset data = new LazyWriteableDataset("data", Dataset.FLOAT32, new int[] { 10, 256, 256 }, null, null, null);

		// We have all the data in memory, it might be large at this point!
		nfile.createData("/entry1/acme/experiment1/", data, true);
		
		for (int i = 0; i < 10; i++) {
			// Make an image to write
			IDataset random = Random.rand(1, 256, 256);
			
			// Write one image, others may follow
			data.setSlice(new IMonitor.Stub(), random, SliceND.createSlice(data, new int[] {i,0,0}, new int[] {i+1,256,256}, new int[] {1,1,1}));
			
			// Optionally flush
			nfile.flush();
		}
	}

}
