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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * This interface specifies the methods required to allow the ScanFileHolder to save data to a file of some
 * description
 */
public interface IStreamSaver {

	/**
	 * This function is called when the ScanFileHolder needs to save data from a particular source.
	 * It can also be called on by itself
	 * 
	 * @param output
	 * 			The directory to create image files within
	 * @param conf
	 * 			The configuration for writing the image.
	 * 
	 * @param holder
	 *            The object storing all the data to be saved
	 * @param monitor
	 *            The progress monitor object
	 * @return true if saved.
	 * 
	 * @throws IOException
	 */
	boolean save(OutputStream output, Configuration conf, DataFrame holder, IMonitor monitor) throws IOException;
}
