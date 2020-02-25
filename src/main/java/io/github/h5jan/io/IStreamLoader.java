/*-
 *******************************************************************************
 * Copyright (c) 2011, 2014 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Peter Chang - initial API and implementation and/or initial documentation
 *******************************************************************************/

package io.github.h5jan.io;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * This interface specifies the methods required to allow the ScanFileHolder to load in data from a file of some
 * description
 */
public interface IStreamLoader<T extends InputStream> extends MonitorableManager {

	/**
	 * This function is called when the ScanFileHolder needs to load data from a particular source
	 * It can also be called on by itself
	 * 
	 * @param stream - data to load
	 * @param configuration - configuration or null or empty
	 * @param mon - may be null
	 * @return This returned object is all the data which has been loaded returned in a small object package.
	 * @throws IOException - if cannot read stream
	 * @throws DatasetException - if cannot process datasets.
	 */
	DataFrame load(T stream, Configuration configuration, IMonitor mon) throws IOException, DatasetException;

	/**
	 * If this loader supports asynchronous loading
	 * @return true if still loading, false if finished or throws RuntimeException if asynchronous loading is not supported.
	 */
	default boolean isLoading() {
		throw new RuntimeException("Asynchronous loading is not supported!");
	}


}