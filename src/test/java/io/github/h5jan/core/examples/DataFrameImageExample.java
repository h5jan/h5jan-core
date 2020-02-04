/*-
 *******************************************************************************
 * Copyright (c) 2020 Halliburton International, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package io.github.h5jan.core.examples;

import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.analysis.dataset.function.Downsample;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Random;
import org.junit.Test;

import io.github.h5jan.core.Appender;
import io.github.h5jan.core.DataFrame;

/**
 * Examples filtering images through a data frame 
 * and writing them to a stack.
 * 
 * @author Matthew Gerring
 *
 */
public class DataFrameImageExample {
	
	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame3DSobelFilter() throws Exception {
		
		writeLazyWithFilter("lazy_data_frame-3d-sobel.h5", "sobel_", data->Image.fanoFilter(data, 5, 5));
	}

	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame3DFanoFilter() throws Exception {
		
		writeLazyWithFilter("lazy_data_frame-3d-fano.h5", "fano_", data->Image.fanoFilter(data, 5, 5));
	}

	/**
	 * Data in slices
	 * @throws Exception
	 */
	@Test
	public void awriteLazyDataFrame3DDownsample() throws Exception {
		
		Function<Dataset,Dataset> filter = data -> {
			Downsample ds = new Downsample(DownsampleMode.MAXIMUM, 4, 4);
			return ds.value(data).get(0);
		};

		writeLazyWithFilter("lazy_data_frame-3d-downsample.h5", "downsample_", filter);
	}

	private void writeLazyWithFilter(String fileName, String datasetName, Function<Dataset,Dataset> imageTransform) throws Exception {
		
		// Make a writing frame
		DataFrame frame = new DataFrame("data", Dataset.FLOAT32, new int[] { 256, 256 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/"+fileName, "/entry1/myData")) {
			
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				Dataset data = Random.rand(256, 256);
				Dataset toWrite = imageTransform.apply(data);
				app.append(datasetName+i, toWrite);
			}
		}
	}
	
	@FunctionalInterface
	interface Function<T, R> {
	    /**
	     * Applies this function to the given argument.
	     *
	     * @param t the function argument
	     * @return the function result
	     */
	    R apply(T t) throws Exception;

	}
}
