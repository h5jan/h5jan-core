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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to append data during a slice writing operation.
 * 
 * @author Matthew Gerring
 *
 */
class AppenderImpl implements Appender {

	private static final Logger logger = LoggerFactory.getLogger(AppenderImpl.class);
	
	private String 					filePath;
	private String      			h5Path;
	private List<String> 			names;
	private NxsFile      			hFile;
	private ILazyWriteableDataset 	data;
	private DataFrame			 	frame;
	private Closeable 				closer;
	private IMonitor 				monitor = new IMonitor.Stub();
	
	private int compression = NexusFile.COMPRESSION_NONE;

	
	AppenderImpl(String filePath, String h5Path, ILazyWriteableDataset data, DataFrame frame, Closeable closer) throws NexusException, IOException {
		
		this.filePath = filePath;
		this.names	= new ArrayList<>();
		this.h5Path = h5Path;
		if (data==null) {
			throw new IllegalArgumentException("The data must not be null!");
		}

		this.data 	= data;
		this.frame 	= frame;
		this.closer = closer;
	}
	
	private void init() throws NexusException, IOException {
		// We make the hdf5 file when the first slice comes in.
		if (hFile==null) {
			this.hFile 	= NxsFile.create(filePath);
			this.hFile.createData(h5Path, data, compression, true);
			
			Util.setReferenceAttributes(hFile, h5Path, data.getName());
		}
	}
	
	public void append(String name, IDataset slice) throws DatasetException, NexusException, IOException {
		
		init();
		names.add(name);
		
		int i = names.size()-1; // The index we are on
		
		slice = FrameUtil.addDimension(slice);
		data.setSlice(monitor, slice, FrameUtil.orient(data,i,slice.getShape()));
	}

	@Override
	public void close() throws Exception {
		try {
			Util.setMetaAttributues(hFile, h5Path, frame, names);
		} catch (Exception ne) {
			logger.debug("Cannot write meta attributes", ne);
			throw ne;
		} finally {
			try {
				this.hFile.close();
			} finally {
				this.hFile = null;
				this.closer.close();
			}
		}
	}

	public IMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public int getCompression() {
		return compression;
	}

	@Override
	public void setCompression(int compression) {
		this.compression = compression;
	}
}
