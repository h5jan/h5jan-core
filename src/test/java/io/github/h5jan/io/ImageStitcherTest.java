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
package io.github.h5jan.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class ImageStitcherTest extends AbstractReaderTest {
	
	@Test
	public void readSingleTiff() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/tile00.tif");
		DataFrame image = reader.read(path.toFile(), Configuration.DEFAULT, new IMonitor.Stub());
		assertNotNull(image);
		assertArrayEquals(new int[] {96,128,1}, image.getShape());
		assertEquals("tile00.tif", image.getName());
		assertEquals(Dataset.RGB, image.getDtype());
		
		IDataset loaded = image.getData().get(0).getSlice();
		assertEquals(RGBDataset.class, loaded.getClass());
	}
	
	@Test
	public void readGreyTiff() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/tile00.tif");
		DataFrame image = reader.read(path.toFile(), Configuration.GREYSCALE, new IMonitor.Stub());
		assertNotNull(image);
		assertArrayEquals(new int[] {96,128,1}, image.getShape());
		assertEquals("tile00.tif", image.getName());
		
		IDataset loaded = image.getData().get(0).getSlice();
		assertEquals(RGBDataset.class, loaded.getClass());
	}
	
	@Test
	public void greyNotEquals() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/tile00.tif");
		DataFrame rgb = reader.read(path.toFile(), Configuration.DEFAULT, new IMonitor.Stub());
		DataFrame grey = reader.read(path.toFile(), Configuration.GREYSCALE, new IMonitor.Stub());
		assertNotEquals(rgb, grey);
	}

	/**
	 * This test reads a tile of tiff images from a directory.
	 * @throws Exception
	 */
	@Test
	public void readDirectory() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame image = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, image.size());
		assertEquals(Arrays.asList("image-00", "image-01", "image-02", "image-03", "image-04",
								   "image-05", "image-06", "image-07", "image-08"),
									image.getColumnNames());
		// Check they loaded lazily
		image.forEach(i->{
			assertTrue(i instanceof LazyDataset);
		});
		
		// Check they can load into memory (they are small)
		image.forEach(i->{
			try {
				IDataset loaded = i.getSlice();
				assertTrue(loaded instanceof RGBDataset);
				assertArrayEquals(new int[] {96,128}, loaded.getShape());
			} catch (DatasetException e) {
				fail(e.getMessage());
			}
		});
	}
	
	/**
	 * This test reads a tile of tiff images from a directory.
	 * @throws Exception
	 */
	@Test
	public void stitchDirectory() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		
		Dataset stitch3x3 = stack.stitch(new int[] {3,3});
		int[] shape = stitch3x3.getShape();
		assertArrayEquals(new int[] {288,384}, shape);
		round(stitch3x3, "test-scratch/image", "stitched_3x3");
		
		Dataset stitch2x4 = stack.stitch(new int[] {2,4});
		shape = stitch2x4.getShape();
		assertArrayEquals(new int[] {192,512}, shape);
		round(stitch2x4, "test-scratch/image", "stitched_2x4");

		Dataset stitch1x7 = stack.stitch(new int[] {1,7});
		shape = stitch1x7.getShape();
		assertArrayEquals(new int[] {96,896}, shape);
		round(stitch1x7, "test-scratch/image", "stitch1x7");

		Dataset stitch4x2 = stack.stitch(new int[] {4,2});
		shape = stitch4x2.getShape();
		assertArrayEquals(new int[] {384,256}, shape);
		round(stitch4x2, "test-scratch/image", "stitch4x2");
	}
	
	@Test(expected=NullPointerException.class)
	public void stitchNull() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		stack.stitch(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void stitchTooLarge() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		stack.stitch(new int[] {4,3});
	}
	
	@Test
	public void stitchZero() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		Dataset image = stack.stitch(new int[] {0,3});
		assertArrayEquals(new int[] {0,384}, image.getShape());
	}

	@Test(expected=IllegalArgumentException.class)
	public void stitchNegative1() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		stack.stitch(new int[] {-1,3});
	}

	@Test(expected=IllegalArgumentException.class)
	public void stitchNegative2() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		stack.stitch(new int[] {3,-3});
	}

	@Test(expected=IllegalArgumentException.class)
	public void stitchLargeRank() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		stack.stitch(new int[] {3,3,3});
	}

	@Test
	public void stitchSmallRank() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/");
		DataFrame stack = reader.read(path, Configuration.DEFAULT, new IMonitor.Stub());
		assertEquals(9, stack.size());
		Dataset image = stack.stitch(new int[] {3});
		assertArrayEquals(new int[] {96,384}, image.getShape());
	}

}
