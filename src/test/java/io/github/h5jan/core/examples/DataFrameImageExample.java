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
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Random;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.io.h5.Appender;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

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
		
		writeLazyWithFilter("lazy_data_frame-3d-sobel.h5", "sobel_", data->Image.sobelFilter(data));
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
				
				// Apply the function and write that
				Dataset toWrite = imageTransform.apply(data);
				app.append(datasetName+i, toWrite);
				
				// Keeping raw data is a good idea
				// It might not be in this frame but it will be useful.
				app.record("raw", data); 
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
	
	@Test
	public void writeIntegrationBox() throws Exception {
		
		Function<Dataset,Dataset[]> integrate = data -> {
			RectangularROI rroi = new RectangularROI(new double[]{100,100}, new double[]{200,200});
			// x and y
			return ROIProfile.box(data, rroi);
		};

		writeIntegratedAndData("lazy_data_frame-3d-box-integration.h5", "box", integrate);
	}
	
	@Test
	public void writeIntegrationSector() throws Exception {
		
		Function<Dataset,Dataset[]> integrate = data -> {
			SectorROI sroi = new SectorROI(100,100, 10, 50, 0, Math.PI);
			// Radial and azimuthal
			return ROIProfile.sector(data, sroi);
		};

		writeIntegratedAndData("lazy_data_frame-3d-sector-integration.h5", "sector", integrate);
	}

	private void writeIntegratedAndData(String fileName, String datasetName, Function<Dataset,Dataset[]> integrator) throws Exception {
		
		// Make a writing frame
		DataFrame frame = new DataFrame("data", Dataset.FLOAT32, new int[] { 256, 256 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf("test-scratch/write_example/"+fileName, "/entry1/myData")) {
						
			// Add the columns incrementally without them all being in memory
			for (int i = 0; i < 10; i++) {
				Dataset data = Random.rand(256, 256);
				app.append("data_"+i, data); // Keeping raw data is a good idea
				
				Dataset[] integration = integrator.apply(data);
				
				// Store the slice. This will create a writable stack if
				// one does not already exist.
				app.record(datasetName+"_x", integration[0]);
				app.record(datasetName+"_y", integration[1]);
			}
		}
	}

}
