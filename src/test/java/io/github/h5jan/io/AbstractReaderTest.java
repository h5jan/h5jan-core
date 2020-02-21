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

import java.io.File;
import java.nio.file.Path;

import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.junit.Before;

import io.github.h5jan.core.Appender;
import io.github.h5jan.core.DataFrame;

public abstract class AbstractReaderTest {
	
	protected DataFrameReader reader;
	protected DataFrameWriter writer;

	@Before
	public void before() throws Exception {
		this.reader = new DataFrameReader();
		this.writer = new DataFrameWriter();
	}

	protected void round(Dataset set, String sdir, String name) throws Exception {
		DataFrame output = new DataFrame(name, Dataset.RGB, set.getShape());
		output.add(set);
		Configuration conf = Configuration.image(output.getName(), "JPG", 8, false);
		File dir = new File(sdir);
		dir.mkdirs();
		writer.save(dir, conf, output, new IMonitor.Stub());
		
		File file = new File("test-scratch/image/"+name+".jpg");
		DataFrame frame = reader.read(file, Configuration.createDefault(), new IMonitor.Stub());
		
		assertArrayEquals(output.getShape(), frame.getShape());
	}
	
	@FunctionalInterface
	public interface FileLoad<T> {
		T load() throws Exception;
	}

	public <T> Tuple<T, Long> time(FileLoad<T> function) throws Exception {
		long before = System.currentTimeMillis();
		T frame = function.load();
		long after = System.currentTimeMillis();
		Tuple<T, Long> ret = new Tuple<>();
		ret.setLeft(frame);
		ret.setRight(after-before);
		return ret;
	}

	protected DataFrame writeImageStack(Path mainDir, String to) throws Exception {
		// Make a writing frame, the tiled image is this size.
		// Each tile is 96,128 so as we are making 3x3 stitching, we need 288,384
		DataFrame frame = new DataFrame("scope_image", Dataset.INT8, new int[] { 288,384 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf(to, "/entry1/myData")) {
			
			app.setCompression(NexusFile.COMPRESSION_LZW_L1); // Otherwise it will be too large.
			
			File[] dirs = mainDir.toFile().listFiles();
			
			for (int i = 0; i < dirs.length; i++) {
				
				// Directory of tiles
				File dir = dirs[i];

				// Read tiles, assuming their file name order is also their tile order.
				DataFrame tiles = reader.read(dir, Configuration.createGreyScale(), new IMonitor.Stub());
				
				// Stitch to make image based on a 3x3 matrix of tiles.
				Dataset image = tiles.stitch(new int[] {3,3});
				assertArrayEquals(new int[] {288,384}, frame.getColumnShape());
				image = DatasetUtils.cast(image, Dataset.INT8);
				
				// Add the image - note they are not all in memory
				// so this process should scale reasonably well.
				app.append("image_"+i, image);
			}
		}
		return frame;
	}


}
