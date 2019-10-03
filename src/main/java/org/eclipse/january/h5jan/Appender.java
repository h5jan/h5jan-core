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
package org.eclipse.january.h5jan;

import org.eclipse.january.DatasetException;
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
	 * @throws DatasetException 
	 */
	default void append(IDataset slice) throws DatasetException {
		append(slice.getName(), slice);
	}
	
	/**
	 * Add slice to end of current writable dataset
	 * @param name
	 * @param slice
	 */
	void append(String name, IDataset slice) throws DatasetException ;

	/**
	 * Called to provide an object to return progress about the writing.
	 * @param monitor
	 */
	void setMonitor(IMonitor monitor);
}
