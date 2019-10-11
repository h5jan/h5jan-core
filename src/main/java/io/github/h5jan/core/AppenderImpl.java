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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;

/**
 * Class to append data during a slice writing operation.
 * 
 * @author Matthew Gerring
 *
 */
class AppenderImpl implements Appender {

	
	private String      			h5Path;
	private List<String> 			names;
	private NxsFile      			hFile;
	private ILazyWriteableDataset 	data;
	private DataFrame			 	frame;
	private Closeable 				closer;
	private IMonitor 				monitor = new IMonitor.Stub();
	
	AppenderImpl(List<String> names, String filePath, String h5Path, ILazyWriteableDataset data, DataFrame frame, Closeable closer) throws NexusException, IOException {
		
		this.names	= names;
		this.h5Path = h5Path;
		if (data==null) {
			throw new IllegalArgumentException("The data must not be null!");
		}
		this.hFile 	= NxsFile.create(filePath);
		this.hFile.createData(h5Path, data, true);
		
		Util.setReferenceAttributes(hFile, h5Path, data.getName());

		this.data 	= data;
		this.frame 	= frame;
		this.closer = closer;
	}
	
	public void append(String name, IDataset slice) throws DatasetException {
		names.add(name);
		
		int i = names.size()-1; // The index we are on
		
		DatasetFrame.addDimension(slice);
		data.setSlice(monitor, slice, DatasetFrame.orient(data,i,slice.getShape()));
	}

	public void close() throws Exception {
		Util.setMetaAttributues(hFile, h5Path, frame);
		this.hFile.close();
		this.hFile = null;
		this.closer.close();
	}

	public IMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}
}
