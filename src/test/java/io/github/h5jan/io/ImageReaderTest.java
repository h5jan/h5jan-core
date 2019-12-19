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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class ImageReaderTest extends AbstractReaderTest {
	
	@Test
	public void working() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/tile00.tif");
		DataFrame image = reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
		assertNotNull(image);
		assertArrayEquals(new int[] {96,128,1}, image.getShape());
		assertEquals("tile00.tif", image.getName());
		assertEquals(Dataset.RGB, image.getDtype());
		
		IDataset loaded = image.getData().get(0).getSlice();
		assertEquals(RGBDataset.class, loaded.getClass());
	}

	@Test(expected=IOException.class)
	public void badFilePath() throws Exception {
		Path path = JPaths.getTestResource("microscope/0/NOT-THERE.tif");
		reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
	}

	@Test(expected=IOException.class)
	public void badFile() throws Exception {
		Path path = JPaths.getTestResource("bad.tif");
		reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
	}

	@Test(expected=IOException.class)
	public void emptyFile() throws Exception {
		Path path = JPaths.getTestResource("empty.png");
		reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
	}
	
	@Test(expected=IOException.class)
	public void wrongExtension() throws Exception {
		Path path = JPaths.getTestResource("wrong_extension.tif");
		reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
	}
	
	@Test
	public void goodPNG() throws Exception {
		Path path = JPaths.getTestResource("dawn.png");
		DataFrame frame = reader.read(path, Configuration.createDefault(), new IMonitor.Stub());
		assertArrayEquals(new int[] {831, 1216, 1}, frame.getShape());
		assertEquals(Dataset.RGB, frame.getDtype());
	}

}
