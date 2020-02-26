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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;
import io.github.h5jan.io.h5.Appender;


public class ArrowTest {
	
	static {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN); // We don't need debugging in the tests.
	}
		
	@BeforeClass
	public static void init() throws Exception {
		// Call something to load the libraries as we are timing
		// things.
		compare(createTestH5Dataset(new int[] {256}, NexusFile.COMPRESSION_NONE), false);
	}

	
	@Test
	public void arrowPrediction1() throws Exception {
		
		DataFrameReader 		reader = new DataFrameReader();
		DataFrameWriter 		writer = new DataFrameWriter();

		DataFrame csv = reader.read(JPaths.getTestResource("csv/131.csv"), Configuration.createEmpty(), new IMonitor.Stub());
		File farw = new File("test-scratch/arrow/lith.arw");
		farw.getParentFile().mkdirs();
		if (!farw.exists()) farw.createNewFile();
		writer.save(farw, Configuration.arrow(), csv, new IMonitor.Stub());
		
		DataFrame arw = reader.read(farw, Configuration.createEmpty(), new IMonitor.Stub());
		assertFrameEquals(csv, arw);
	}

	@Test
	public void arrowPrediction2() throws Exception {
		Path path = JPaths.getTestResource("csv/131.csv");
		compare(path);
	}

	@Test
	public void arrow1DNone() throws Exception {
		Path created = createTestH5Dataset(new int[] {256}, NexusFile.COMPRESSION_NONE);
		compare(created);
	}

	@Test
	public void arrow1DNoneLarge() throws Exception {
		Path created = createTestH5Dataset(new int[] {2560000}, NexusFile.COMPRESSION_NONE);
		compare(created);
	}

	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow2DNone() throws Exception {
		Path created = createTestH5Dataset(new int[] {256,256}, NexusFile.COMPRESSION_NONE);
		compare(created);
	}
	
	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow3DNone() throws Exception {
		Path created = createTestH5Dataset(new int[] {8,256,256}, NexusFile.COMPRESSION_NONE);
		compare(created);
	}

	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow4DNone() throws Exception {
		Path created = createTestH5Dataset(new int[] {2,3,256,256}, NexusFile.COMPRESSION_NONE);
		compare(created);
	}

	@Test
	public void arrow1DLZWLarge() throws Exception {
		Path created = createTestH5Dataset(new int[] {2560000}, NexusFile.COMPRESSION_LZW_L1);
		compare(created);
	}

	@Test
	public void arrow1DLZW() throws Exception {
		Path created = createTestH5Dataset(new int[] {256}, NexusFile.COMPRESSION_LZW_L1);
		compare(created);
	}

	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow2DLZW() throws Exception {
		Path created = createTestH5Dataset(new int[] {256,256}, NexusFile.COMPRESSION_LZW_L1);
		compare(created);
	}
	
	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow3DLZW() throws Exception {
		Path created = createTestH5Dataset(new int[] {8,256,256}, NexusFile.COMPRESSION_LZW_L1);
		compare(created);
	}

	/**
	 * Yes! we allow nD columns to save to arrow!
	 * @throws Exception
	 */
	@Test
	public void arrow4DLZW() throws Exception {
		Path created = createTestH5Dataset(new int[] {2,3,256,256}, NexusFile.COMPRESSION_LZW_L1);
		compare(created);
	}
	
	private void compare(Path cur) throws Exception {
		compare(cur, true);
	}

	private static void compare(Path cur, boolean print) throws Exception {
		
		DataFrameReader 		reader = new DataFrameReader();
		ArrowIO 				arrowIO= new ArrowIO();

		Tuple<DataFrame,Long> timed = time(()->reader.read(cur, Configuration.createEmpty(), new IMonitor.Stub()));
		if (print) System.out.println("File '"+cur.getFileName()+"' is "+Files.size(cur)+" b");
		
		Tuple<Map<String,Object>,Long> raw = time(()->timed.getLeft().raw());
		if (print) System.out.println("Full load '"+cur.getFileName()+"' in "+(timed.getRight()+raw.getRight())+" ms");
		
		String name = cur.getFileName().toString();
		name = name.substring(0, name.indexOf('.'));
		Path aPath = Paths.get("test-scratch/arrow/").resolve(name+".arw");
		File farw = aPath.toFile();
		farw.getParentFile().mkdirs();
		if (!farw.exists()) farw.createNewFile();
		try (FileOutputStream output = new FileOutputStream(farw)){
			arrowIO.write(timed.getLeft(), output);
			output.flush();
		}
		
		if (print) System.out.println("File '"+aPath.getFileName()+"' is "+Files.size(aPath)+" b");

		try(FileInputStream input = new FileInputStream(farw)) {
			Tuple<DataFrame,Long> arw = time(()->arrowIO.read(new FileInputStream(farw)));
			if (print) System.out.println("Full load '"+aPath.getFileName()+"' in "+arw.getRight()+" ms");
			assertFrameEquals(timed.getLeft(), arw.getLeft());
		}
		if (print) System.out.println();
	}

	private static Path createTestH5Dataset(int[] columnShape, int compression) throws Exception {
		// Make a writing frame
		DataFrame frame = new DataFrame("data", Dataset.FLOAT32, columnShape);
		
		// Save to HDF5, columns can be large, these are not it's a test
		String ext = compression==1 ? ".lzw" : "";
		String path = "test-scratch/arrow/10x"+Arrays.toString(columnShape).replaceAll("\"", "")+ext+".h5";
		try (Appender app = frame.open_hdf(path, "/entry1/myData")) {
			app.setCompression(compression);
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				app.append("slice_"+i, Random.rand(columnShape));
			}
		}
		return Paths.get(path);
	}

	private static void assertFrameEquals(DataFrame comp, DataFrame with) throws DatasetException {
		assertEquals(comp.getColumnNames(), with.getColumnNames());
		assertArrayEquals(comp.getShape(), with.getShape());
		
		Map<String, Object> craw = comp.raw();
		Map<String, Object> wraw = with.raw();
		assertEquals(craw.keySet(), wraw.keySet());
		for (String name : craw.keySet()) {
			Object ca = craw.get(name);
			Object wa = wraw.get(name);
			if (ca instanceof short[]) {
				assertArrayEquals((short[])ca, (short[])wa);
			} else if (ca instanceof int[]) {
				assertArrayEquals((int[])ca, (int[])wa);
			} else if (ca instanceof long[]) {
				assertArrayEquals((long[])ca, (long[])wa);
			} else if (ca instanceof float[]) {
				assertArrayEquals((float[])ca, (float[])wa, 0.00001f);
			} else if (ca instanceof double[]) {
				assertArrayEquals((double[])ca, (double[])wa, 0.00001d);
			}
		}
	}

	@FunctionalInterface
	public interface FileLoad<T> {
		T load() throws Exception;
	}

	public static <T> Tuple<T, Long> time(FileLoad<T> function) throws Exception {
		long before = System.currentTimeMillis();
		T frame = function.load();
		long after = System.currentTimeMillis();
		Tuple<T, Long> ret = new Tuple<>();
		ret.setLeft(frame);
		ret.setRight(after-before);
		return ret;
	}

}
