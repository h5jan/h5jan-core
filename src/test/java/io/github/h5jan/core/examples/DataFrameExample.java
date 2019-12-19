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
package io.github.h5jan.core.examples;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.github.h5jan.core.AbstractH5JanTest;
import io.github.h5jan.core.Appender;
import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;
import io.github.h5jan.core.WellMetadata;
import io.github.h5jan.io.Configuration;
import io.github.h5jan.io.DataFrameReader;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // We write some files then futher test them.
public class DataFrameExample extends AbstractH5JanTest {
	
	/**
	 * All data in memory, write it out.
	 * 
	 * @throws Exception
	 */
	@Test
	public void awriteInMemoryDataFrame1() throws Exception {
		
		// We create a place to put our data
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		
		// Save to HDF5
		frame.to_hdf("test-scratch/write_example/inmem_data_frame.h5", "/entry1/myData");
	}
	
	/**
	 * All data in memory, write it out.
	 * 
	 * @throws Exception
	 */
	@Test
	public void awriteInMemoryDataFrame2() throws Exception {
		
		Dataset[] sets = new Dataset[3];
		for (int i = 0; i < sets.length; i++) {
			sets[i] = Random.rand(256);
			sets[i].setName("slice_"+i);
 		}
		// Make a test frame
		DataFrame frame = new DataFrame("some_columns", Dataset.FLOAT32, sets);
		
		// Save to HDF5
		frame.to_hdf("test-scratch/write_example/inmem_data_frame_inc.h5", "/entry1/myData");
	}
	
	/**
	 * All data in memory, write it out.
	 * 
	 * @throws Exception
	 */
	@Test
	public void awriteInMemoryDataFrameWithMetadata() throws Exception {
		
		// We create a place to put our data
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		WellMetadata meta = createWellMetadata();
		frame.setMetadata(meta);
		
		// Save to HDF5
		frame.to_hdf("test-scratch/write_example/inmem_data_frame_meta.h5", "/entry1/myData");
		
		DataFrame read = (new DataFrame()).read_hdf("test-scratch/write_example/inmem_data_frame_meta.h5");
		WellMetadata readMeta = (WellMetadata)read.getMetadata();
		assertEquals(meta, readMeta);
	}


	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame2D() throws Exception {
		
		// Make a frame for lazy writing (does not hold data)
		DataFrame frame = new DataFrame("data", Dataset.FLOAT64, new int[] { 256 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_data_frame-2d.h5", "/entry1/myData")) {
			
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				app.append("slice_"+i, Random.rand(256));
			}
		}
	}

	
	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame3D() throws Exception {
		
		// Make a writing frame
		DataFrame frame = new DataFrame("data", Dataset.FLOAT32, new int[] { 256, 256 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_data_frame-3d.h5", "/entry1/myData")) {
			
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				app.append("slice_"+i, Random.rand(256, 256));
			}
		}
	}
	
	/**
	 * This example assembles TIFF images into a HDF file. 
	 * It is useful to create stitched stacks which can be sliced and visualised
	 * in DAWN http://www.dawnsci.org/
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void awriteLazyMicroscopeStack() throws Exception {
		
		// Make a writing frame, the tiled image is this size.
		// Each tile is 96,128 so as we are making 3x3 stitching, we need 288,384
		DataFrame frame = new DataFrame("scope_image", Dataset.FLOAT32, new int[] { 288,384 });
		
		// Make an object to read other formats, in this case TIFF
		DataFrameReader reader = new DataFrameReader();

		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_microscope_image.h5", "/entry1/myData")) {
			
			File[] dirs = JPaths.getTestResource("microscope").toFile().listFiles();
			
			for (int i = 0; i < dirs.length; i++) {
				
				// Directory of tiles
				File dir = dirs[i];

				// Read tiles, assuming their file name order is also their tile order.
				DataFrame tiles = reader.read(dir, Configuration.createGreyScale(), new IMonitor.Stub());
				
				// Stitch to make image based on a 3x3 matrix of tiles.
				Dataset image = tiles.stitch(new int[] {3,3});
				assertArrayEquals(new int[] {288,384}, frame.getColumnShape());
				image = DatasetUtils.cast(image, Dataset.FLOAT32);
				
				// Add the image - note they are not all in memory
				// so this process should scale reasonably well.
				app.append("image_"+i, image);
			}
		}

		DataFrame stack = new DataFrame();
		stack.read_hdf("test-scratch/write_example/lazy_microscope_image.h5");
		assertArrayEquals(new int[] {288,384,2}, stack.getShape());
	}

	@Test
	public void readFrames() throws Exception {
		
		DataFrame frame = new DataFrame("data", Dataset.FLOAT32, new int[] { 256 });

		frame.read_hdf("test-scratch/write_example/inmem_data_frame.h5");
		assertArrayEquals(new int[] {256, 3}, frame.getShape());
		assertEquals(Arrays.asList("a", "b", "c"), frame.getColumnNames());
		assertEquals(frame, readWriteLazy(frame, "readFrames"));
		
		frame.read_hdf("test-scratch/write_example/inmem_data_frame_inc.h5");
		assertArrayEquals(new int[] {256, 3}, frame.getShape());
		assertEquals(Arrays.asList("slice_0", "slice_1", "slice_2"), frame.getColumnNames());
		assertEquals(frame, readWriteLazy(frame, "readFrames"));
		
		frame.read_hdf("test-scratch/write_example/inmem_data_frame_meta.h5");
		assertArrayEquals(new int[] {256, 3}, frame.getShape());
		assertEquals(Arrays.asList("a", "b", "c"), frame.getColumnNames());
		assertEquals(frame, readWriteLazy(frame, "readFrames"));
		assertEquals(createWellMetadata(), frame.getMetadata());
		
		frame.read_hdf("test-scratch/write_example/lazy_data_frame-2d.h5");
		assertArrayEquals(new int[] {256, 10}, frame.getShape());
		List<String> snames = IntStream.range(0, 10).mapToObj(i->"slice_"+i).collect(Collectors.toList());
		assertEquals(snames, frame.getColumnNames());
		assertEquals(frame, readWriteLazy(frame, "readFrames"));

		frame.read_hdf("test-scratch/write_example/lazy_data_frame-3d.h5");
		assertArrayEquals(new int[] {256, 256, 10}, frame.getShape());
		assertEquals(snames, frame.getColumnNames());
		assertEquals(frame, readWriteLazy(frame, "readFrames"));
	}

}
