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

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.IMonitor;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class LoadSpeedTest extends AbstractReaderTest {
	
	private Path dir;

	@Before
	public void mkdir() throws Exception {
		dir = Paths.get("test-scratch/speedTest/");
		Files.createDirectories(dir);
		speedTest(NexusFile.COMPRESSION_LZW_L1, "csv/j21.csv", 2000, dir.resolve("j21.h5"), 500, false);
	}
	
	@Test
	public void csvUncompressed() throws Exception {
		
		speedTest(NexusFile.COMPRESSION_NONE, "csv/j21.csv", 2000, dir.resolve("j21.h5"), 500);
	}

	@Test
	public void csvCompressed() throws Exception {
		speedTest(NexusFile.COMPRESSION_LZW_L1, "csv/j21.csv", 2000, dir.resolve("j21.h5"), 500);
	}
	
	@Test
	public void csvUncompressedLrg() throws Exception {
		
		speedTest(NexusFile.COMPRESSION_NONE, "csv/j21_large.csv", 8000, dir.resolve("j21.h5"), 500);
	}

	@Test
	public void csvCompressedLrg() throws Exception {
		speedTest(NexusFile.COMPRESSION_LZW_L1, "csv/j21_large.csv", 5000, dir.resolve("j21.h5"), 500);
	}

	@Test
	public void images() throws Exception {
		
		// Write Data Frame for dir
		Path filePath = dir.resolve("scope_image.h5");
		writeImageStack(JPaths.getTestResource("microscope"), filePath.toString());
		
		Tuple<DataFrame, Long> rh5 = time(()->reader.read(filePath, Configuration.createEmpty(), new IMonitor.Stub()));

		System.out.println("Read image stack in "+rh5.getRight()+"ms");
		assertTrue(200>rh5.getRight());
	}


	private void speedTest(int compression, String filePath, long tcsv, Path writePath, long th5) throws Exception {
		speedTest(compression, filePath, tcsv, writePath, th5, true);
	}

	private void speedTest(int compression, String filePath, long tcsv, Path writePath, long th5, boolean prod) throws Exception {
		
		Tuple<DataFrame, Long> rcsv = time(()->reader.read(JPaths.getTestResource(filePath), Configuration.createEmpty(), new IMonitor.Stub()));
		if (prod) assertTrue(tcsv+25>=rcsv.getRight());
		
		// We write it, not timed
		rcsv.getLeft().to_hdf(writePath.toString(), "/entry1/data", compression);
		
		// We time the reading of that h5 file
		Tuple<DataFrame, Long> rh5 = time(()->reader.read(writePath, Configuration.createEmpty(), new IMonitor.Stub()));
		
		if (prod) assertTrue(th5>rh5.getRight());
		
		if (prod) {
			System.out.println("CSV read in "+rcsv.getRight()+"ms");
			System.out.println("H5 read in "+rh5.getRight()+"ms");
		}

	}
}
