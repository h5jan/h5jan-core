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

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

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
	private Closeable 				closer;
	private IMonitor 				monitor = new IMonitor.Stub();
	
	AppenderImpl(List<String> names, String filePath, String h5Path, ILazyWriteableDataset data, Closeable closer) throws NexusException, IOException {
		
		this.names	= names;
		this.h5Path = h5Path;
		this.hFile 	= NxsFile.create(filePath);
		this.hFile.createData(h5Path, data, true);
		hFile.addAttribute("/", new AttributeImpl(DataFrame.PATH, h5Path));
		hFile.addAttribute("/", new AttributeImpl(DataFrame.DATA_PATH, h5Path+"/"+data.getName()));

		this.data 	= data;
		this.closer = closer;
	}
	
	public void append(String name, IDataset slice) throws DatasetException {
		names.add(name);
		
		int i = names.size()-1; // The index we are on
		
		DatasetFrame.addDimension(slice);
		data.setSlice(monitor, slice, DatasetFrame.orient(data,i,slice.getShape()));
	}

	public void close() throws Exception {
		this.hFile.addAttribute(h5Path, new AttributeImpl("column_names", names));
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
