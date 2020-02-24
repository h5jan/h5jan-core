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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DTypeUtils;
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
	
	// Extra datasets which may be written, for instance
	// if the data frame as derived data that you also want to store.
	private Map<String, ILazyWriteableDataset> aux;

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
	
	@Override
	public void init() throws NexusException, IOException {
		// We make the hdf5 file when the first slice comes in.
		if (hFile==null) {
			this.hFile 	= NxsFile.create(filePath);
			this.hFile.createData(h5Path, data, compression, true);
			
			Util.setReferenceAttributes(hFile, h5Path, data.getName());
		}
	}
	
	@Override
	public void append(String columnName, IDataset slice) throws Exception {
		
		init();
		names.add(columnName);
		
		int i = names.size()-1; // The index we are on
		append(data, slice, i, true);
	}
	
	@Override
	public void record(String name, IDataset slice) throws Exception {
		
		init();
		
		// Create stack for the slices, if it not there already
		if (!containsRecord(name)) {
			create(name, slice.getElementClass(), slice.getShape());
		}

		ILazyWriteableDataset data = aux.get(name);
		int i = names.size()-1; // The index we are on
		append(data, slice, i, false);
	}
	
	@Override
	public ILazyWriteableDataset create(String name, Class<?> dtype, int... sliceShape) throws Exception {
		
		ILazyWriteableDataset writer = FrameUtil.create(name, dtype, sliceShape);
		if (aux==null) aux = Collections.synchronizedMap(new HashMap<>());
		aux.put(name, writer);
		this.hFile.createData(h5Path, writer, compression, true);
		return writer;
	}
	
	@Override
	public boolean containsRecord(String name) {
		if (aux!=null && aux.containsKey(name))         return true;
		//if (data!=null && name.equals(data.getName()))  return true;
		return false;
	}
	
	private void append(ILazyWriteableDataset data, IDataset slice, int index, boolean mayRecord) throws Exception {
		
		slice = FrameUtil.addDimension(slice);
		boolean compatible = isCompatible(data, slice);
		if (compatible) { // Are they the same type?
			data.setSlice(monitor, slice, FrameUtil.orient(data,index,slice.getShape()));
			// Put it back to its smallest shape.
			slice.squeeze(true);
		}
		
		if (mayRecord && (!compatible || isWildlyDifferent(data, slice))) {
			// We do this because forcing the dataset in the above might lose information.
			// For instance if shoehorning a RGB into a int type, it will be added but lost.
			// We also do this if the type is not compatible at all.
			record(slice.getName()+"_aux", slice);
		}
		
	}

	private boolean isWildlyDifferent(ILazyWriteableDataset d, IDataset s) {
		int dt = DTypeUtils.getDType(d);
		int st = DTypeUtils.getDType(s);
		if (dt==st) return false;
		if (DTypeUtils.isDTypeElemental(dt) && DTypeUtils.isDTypeElemental(st)) {
			return false;
		}
		return true; // Might be compound in a elemental so they are rather different.
	}

	private boolean isCompatible(ILazyWriteableDataset d, IDataset s) {
		int dt = DTypeUtils.getDType(d);
		int st = DTypeUtils.getDType(s);
		if (dt==st) return true;
		if (DTypeUtils.isDTypeNumerical(dt) && DTypeUtils.isDTypeNumerical(st)) {
			return true;
		} 
		return false;
	}

	@Override
	public void close() throws Exception {
		if (aux!=null) {
			aux.clear();
			aux = null;
		}
		try {
			Util.setMetaAttributues(hFile, h5Path, frame, names);
		} catch (Exception ne) {
			logger.debug("Cannot write meta attributes", ne);
			throw ne;
		} finally {
			try {
				if (this.hFile!=null) this.hFile.close();
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
