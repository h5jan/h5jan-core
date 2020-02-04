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

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

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
	 * @param name
	 * @param slice
	 * @throws Exception 
	 */
	void append(String name, IDataset slice) throws Exception ;

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
