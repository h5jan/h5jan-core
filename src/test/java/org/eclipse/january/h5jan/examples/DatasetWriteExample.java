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
package org.eclipse.january.h5jan.examples;

import static org.junit.Assert.assertArrayEquals;

import java.nio.file.Path;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.h5jan.JPaths;
import org.eclipse.january.h5jan.NxsFile;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import hdf.hdf5lib.H5;

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
		nfile = NxsFile.create("test-scratch/write_example/block.nxs");
	}
	
	@After
	public void close() throws NexusException {
		nfile.close();
	}
	
	@Test
	public void writeDatasetLazyImages() throws Exception {
		
		// We create a place to put our data
		ILazyWriteableDataset data = new LazyWriteableDataset("data", Dataset.FLOAT64, new int[] { 10, 1024, 1024 }, null, null, null);

		// We have all the data in memory, it might be large at this point!
		nfile.createData("/entry1/acme/experiment1/", data, true);
		
		for (int i = 0; i < 10; i++) {
			// Make an image to write
			IDataset random = Random.rand(1, 1024, 1024);
			
			// Write one image, others may follow
			data.setSlice(new IMonitor.Stub(), random, SliceND.createSlice(data, new int[] {i,0,0}, new int[] {i+1,1024,1024}, new int[] {1,1,1}));
			
			// Optionally flush
			nfile.flush();
		}
	}
}
