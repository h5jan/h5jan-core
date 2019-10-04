package org.eclipse.january.h5jan.examples;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.h5jan.Appender;
import org.eclipse.january.h5jan.DataFrame;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // We write some files then futher test them.
public class DataFrameExample {
	
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
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame2D() throws Exception {
		
		// We create a place to put our data
		ILazyWriteableDataset data = DataFrame.create("data", Dataset.FLOAT32, new int[] { 256 });
		
		// Make a test frame
		DataFrame frame = new DataFrame(data, Dataset.FLOAT32);
		
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
		
		// We create a place to put our data
		ILazyWriteableDataset data = DataFrame.create("data", Dataset.FLOAT32, new int[] { 256, 256 });
		
		// Make a test frame
		DataFrame frame = new DataFrame(data, Dataset.FLOAT32);
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_data_frame-3d.h5", "/entry1/myData")) {
			
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				app.append("slice_"+i, Random.rand(256, 256));
			}
		}
	}

	@Test
	public void readFrames() throws Exception {
		
		DataFrame frame = new DataFrame();

		frame.read_hdf("test-scratch/write_example/inmem_data_frame.h5");
		assertArrayEquals(new int[] {256, 3}, frame.getData().getShape());
		assertEquals(Arrays.asList("a", "b", "c"), frame.getNames());
		assertEquals(frame, readWriteTmp(frame));
		
		frame.read_hdf("test-scratch/write_example/inmem_data_frame_inc.h5");
		assertArrayEquals(new int[] {256, 3}, frame.getData().getShape());
		assertEquals(Arrays.asList("slice_0", "slice_1", "slice_2"), frame.getNames());
		assertEquals(frame, readWriteTmp(frame));
		
		frame.read_hdf("test-scratch/write_example/lazy_data_frame-2d.h5");
		assertArrayEquals(new int[] {256, 10}, frame.getData().getShape());
		List<String> snames = IntStream.range(0, 10).mapToObj(i->"slice_"+i).collect(Collectors.toList());
		assertEquals(snames, frame.getNames());
		assertEquals(frame, readWriteTmp(frame));

		frame.read_hdf("test-scratch/write_example/lazy_data_frame-3d.h5");
		assertArrayEquals(new int[] {256, 256, 10}, frame.getData().getShape());
		assertEquals(snames, frame.getNames());
		assertEquals(frame, readWriteTmp(frame));
	}

	private DataFrame readWriteTmp(DataFrame frame) throws NexusException, IOException, DatasetException {
		frame.to_hdf("test-scratch/write_example/tmp.h5", "/some/other/path");
		return frame.read_hdf("test-scratch/write_example/tmp.h5");
	}
}
