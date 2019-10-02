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
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
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
public class DatasetReadExample {

	private NxsFile nfile;
	
	@Before
	public void read() throws Exception {
		Path file = JPaths.getTestResource("i05-4859.nxs");
		nfile = NxsFile.open(file.toString());
	}
	
	@After
	public void close() throws NexusException {
		nfile.close();
	}
	
	@Test
	public void readDatasetLazy() throws Exception {
		
		// Does not read into memory.
		ILazyDataset lz = nfile.getDataset("/entry1/instrument/analyser/data");

		// We have all the data in memory, it might be large at this point!
		assertArrayEquals(new int[] {1, 1000, 1040}, lz.getShape()); // Shape could be larger!
	}
	
	@Test
	public void readDatasetMemory() throws Exception {

		// Does not read into memory.
		ILazyDataset lz = nfile.getDataset("/entry1/instrument/analyser/data");
		IDataset    mem = lz.getSlice(); // Now it is all in memory

		// We have all the data in memory, it might be large at this point!
		assertArrayEquals(new int[] {1, 1000, 1040}, mem.getShape()); // Shape could be larger!
	}
	
	@Test
	public void readDatasetSliceMemory() throws Exception {
		
		// Does not read into memory. Could be very large data at this point.
		ILazyDataset lz = nfile.getData("/entry1/instrument/analyser/data").getDataset();

		// Slicing the data reads it into memory.
		IDataset    mem = lz.getSlice(new Slice(), new Slice(100, 600), new Slice(200, 700));
		mem.squeeze();

		// We have all the data in memory, it might be large at this point!
		assertArrayEquals(new int[] {500, 500}, mem.getShape()); // Shape could be larger!
	}
	
	@Test
	public void readDatasetSliceLazy() throws Exception {
		
		// Does not read into memory. Could be very large data at this point.
		ILazyDataset lz = nfile.getData("/entry1/instrument/analyser/data").getDataset();

		// Slice view does not read the data into memory.
		ILazyDataset  mem = lz.getSliceView(new Slice(), new Slice(100, 600), new Slice(200, 700));

		// We have all the data in memory, it might be large at this point!
		assertArrayEquals(new int[] {1, 500, 500}, mem.getShape()); // Shape could be larger!
	}

	
	@Test
	public void readTree() throws Exception {
		
		// Does not read into memory. Could be very large data at this point.
		Node top = nfile.getNode("/");
		print(top, "");
	}

	private void print(Node node, String depth) {
		System.out.println(depth+node);
		if (node instanceof Iterable) {
			Iterable<NodeLink> gnode = (Iterable<NodeLink>)node;
			for (NodeLink nodeLink : gnode) {
				print(nodeLink.getDestination(), depth+"->");
			}
		}
	}

}
