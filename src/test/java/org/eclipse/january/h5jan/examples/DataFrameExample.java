package org.eclipse.january.h5jan.examples;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.h5jan.Appender;
import org.eclipse.january.h5jan.DataFrame;
import org.junit.Test;

public class DataFrameExample {

	
	/**
	 * All data in memory, write it out.
	 * 
	 * @throws Exception
	 */
	@Test
	public void writeInMemoryDataFrame() throws Exception {
		
		// We create a place to put our data
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		
		// Save to HDF5
		frame.to_hdf("test-scratch/write_example/inmem_data_frame.h5", "/entry1/myData");
	}
	
	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void writeLazyDataFrame2D() throws Exception {
		
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
	public void writeLazyDataFrame3D() throws Exception {
		
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

}
