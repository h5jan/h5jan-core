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
package io.github.h5jan.core;

import java.io.IOException;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;

/**
 * An appender adds slices to a DataFrame
 * NOTE: It keeps a count starting at 0 of the columns it is adding.
 * 
 * @author Matthew Gerring
 *
 */
public interface Appender extends AutoCloseable {

	/**
	 * Add slice to end of current writable dataset
	 * 
	 * @param slice
	 * @throws Exception 
	 */
	default void append(IDataset slice) throws Exception {
		append(slice.getName(), slice);
	}
	
	/**
	 * Add slice to end of current writable dataset
	 * 
	 * @param name - name of dataset we are appending.
	 * @param slice - slice to append.
	 * @throws DatasetException - if the dataset cannot be written.
	 * @throws NexusException - if nexus cannot write the slice.
	 * @throws IOException - if there is an IO error writing to the file.
	 */
	void append(String name, IDataset slice) throws Exception ;

	/**
	 * Append or record extra data. This data does not get saved as
	 * data frame columns.
	 * 
	 * @param name - name of dataset we are appending.
	 * @param slice - slice to append.
	 * @throws DatasetException - if the dataset cannot be written.
	 * @throws NexusException - if nexus cannot write the slice.
	 * @throws IOException - if there is an IO error writing to the file.
	 */
	void record(String name, IDataset slice) throws Exception;
	
	/**
	 * Append or record extra data. This data does not get saved as
	 * data frame columns.
	 * 
	 * @param name - name of dataset we are appending.
	 * @return true if record is a writable record.
	 */
	boolean containsRecord(String name);

	/**
	 * Allows the writer to create a writable dataset in addition
	 * to the main one of the frame. This can be useful to store
	 * derived data such as automated analysis results.
	 * 
	 * @param name - name of 
	 * @param class - class of data type, for instance Double.class.
	 * @return dataset to which we are writing. Use the record method to add slices to this by name.
	 * @throws Exception if the dtype is not supported, for instance if you try BigDecimal.class.
	 */
	ILazyWriteableDataset create(String name, Class<?> dtype, int... sliceShape) throws Exception;
	
	/**
	 * Called to provide an object to return progress about the writing.
	 * @param monitor
	 */
	void setMonitor(IMonitor monitor);
	
	/**
	 * The compression one of NexusFile values, currently:
	 * NexusFile.COMPRESSION_NONE
	 * NexusFile.COMPRESSION_LZW_L1
	 * @return compression, default is NexusFile.COMPRESSION_NONE
	 */
	public int getCompression();

	/**
	 * The compression one of NexusFile values, currently:
	 * NexusFile.COMPRESSION_NONE
	 * NexusFile.COMPRESSION_LZW_L1
	 * @return compression, default is NexusFile.COMPRESSION_NONE
	 */
	public void setCompression(int compression);

	/**
	 * May be optionally called to create the file and make
	 * it ready for writing. Setting compression after init() has
	 * no effect.
	 * @throws Exception if the file cannot be made ready.
	 */
	void init() throws Exception;
}
