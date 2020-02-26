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

import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * A saver for data frames in the arrow format.
 * 
 * @author Matthew Gerring
 *
 */
public class ArrowSaver implements IStreamSaver<FileOutputStream> {

	@Override
	public boolean save(FileOutputStream stream, Configuration conf, DataFrame holder, IMonitor monitor) throws IOException, DatasetException {
		ArrowIO io = new ArrowIO();
		return io.write(holder, stream);
	}

}
