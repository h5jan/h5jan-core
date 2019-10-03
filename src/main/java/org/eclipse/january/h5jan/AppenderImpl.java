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
		this.data 	= data;
		this.closer = closer;
	}
	
	public void append(String name, IDataset slice) throws DatasetException {
		names.add(name);
		
		int i = names.size()-1; // The index we are on
		int [] shape = new int[slice.getShape().length+1];
		System.arraycopy(slice.getShape(), 0, shape, 0, slice.getShape().length);
		shape[shape.length-1] = 1;
		slice.resize(shape);
		
		int[] from = new int[shape.length];
		from[from.length-1] = i;
		
		int[] to = new int[shape.length];
		System.arraycopy(shape, 0, to, 0, to.length);
		to[to.length-1] = i+1;
		
		int[]step = new int[shape.length];
		Arrays.fill(step, 1);

		data.setSlice(monitor, slice, SliceND.createSlice(data, from, to, step));
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
