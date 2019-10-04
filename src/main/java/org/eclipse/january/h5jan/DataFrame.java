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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.dataset.StringDatasetBase;

/**
 * A data frame of January datasets which
 * can be written and read from HDF5.
 * 
 * The data is lazy, you may write slices of the 
 * data table. The Table is nD, each written "column"
 * does not have to be 1D.
 * 
 * The data is written in HDF in an easy way to call the
 * DataFrame constructor from python. It is not written
 * in the DataFrame format for HDF5 because that is not
 * very nice for further analysis. Instead the data is
 * stored in hdf5-NeXus format which is a block of contiguous
 * data that may be sliced.
 */
public class DataFrame extends DatasetFrame {
	
	final static String COL_NAMES = "column_names";
	final static String PATH 	  = "path";
	final static String DATA_PATH = "data";
	
	private volatile boolean open = false;
	
	public DataFrame() {
		super();
	}

	public DataFrame(String name) {
		super(name);
	}
	
	public DataFrame(String name, int dtype, IDataset... columns) throws DatasetException {
		super(name, dtype, columns);
	}

	public DataFrame(ILazyDataset data, int dtype) {
		super(data, dtype);
	}

	public DataFrame(ILazyDataset data, int index, List<String> names, int dtype) {
		super(data, index, names, dtype);
	}

	/**
	 * This method imports all the data frame into memory then writes it to HDF.
	 * @param filePath
	 * @throws NexusException
	 * @throws IOException
	 * @throws DatasetException
	 */
	@SuppressWarnings("deprecation")
	public DataFrame to_hdf(String filePath, String h5Path) throws NexusException, IOException, DatasetException {
		
		if (data.getShape()[data.getRank()-1]!=names.size()) {
			throw new IllegalArgumentException("The names must match the last dimension of data!");
		}
		try(NxsFile nfile = NxsFile.create(filePath)) {
			
			IDataset toWrite = this.data.getSlice();
			if (dtype>-1) {
				toWrite = DatasetUtils.cast(toWrite, this.dtype); // Does nothing if it can.
			}
			toWrite.setName(data.getName());
			
			// We have all the data in memory, it might be large at this point!
			nfile.createData(h5Path, toWrite, true);
			nfile.addAttribute(h5Path, new AttributeImpl(COL_NAMES, names));
			nfile.addAttribute("/", new AttributeImpl(PATH, h5Path));
			nfile.addAttribute("/", new AttributeImpl(DATA_PATH, h5Path+"/"+data.getName()));
		}
		return this;
	}

	/**
	 * Open the file for appending data.
	 * @param filePath
	 * @param h5Path
	 * @return
	 * @throws IOException 
	 * @throws NexusException 
	 */
	public synchronized Appender open_hdf(String filePath, String h5Path) throws NexusException, IOException {
		
		if (this.open) {
			throw new IllegalArgumentException("HDF file already open!");
		}
		if (!(this.data instanceof ILazyWriteableDataset)) {
			throw new IllegalArgumentException("In order to open for slice writing please create with ILazyWriteableDataset!");
		}
		
		if (names==null) names = new ArrayList<>();
		this.open = true;
		
		return new AppenderImpl(names, filePath, h5Path, (ILazyWriteableDataset)data, ()->this.open=false);
	}
	
	/**
	 * Read a dataframe as LazyDatasets from the file.
	 * @param filePath
	 * @throws NexusException
	 * @throws IOException
	 * @throws DatasetException 
	 */
	public DataFrame read_hdf(String filePath) throws NexusException, IOException, DatasetException {
		try(NxsFile nfile = NxsFile.reference(filePath)) {
			
			GroupNode node = nfile.getGroup("/", false);
			String path = node.getAttribute(DataFrame.PATH).getValue().getString();
			String dataPath = node.getAttribute(DataFrame.DATA_PATH).getValue().getString();
			
			GroupNode gdata = nfile.getGroup(path, false);
			IDataset inames = gdata.getAttribute(COL_NAMES).getValue();
			String[] snames = (String[])((StringDatasetBase)inames).getBuffer();
			
			// Assign fields of DataFrame that we know.
			this.names = new ArrayList<String>(Arrays.asList(snames));
			this.data = nfile.getDataset(dataPath);
			this.name = this.data.getName();
		}
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Creates a tabular writeable dataset using the shape of one column.
	 * @param name
	 * @param dtype
	 * @return writeable dataset
	 */
	public static ILazyWriteableDataset create(String name, int dtype) {
		return create(name, dtype, new int[ILazyWriteableDataset.UNLIMITED]);
	}
	
	/**
	 * Creates a writeable dataset using the shape of one column.
	 * @param name
	 * @param dtype
	 * @param columnShape
	 * @return writeable dataset
	 */
	public static ILazyWriteableDataset create(String name, int dtype, int[] columnShape) {
		
		int[] dshape = new int[columnShape.length+1];
		System.arraycopy(columnShape, 0, dshape, 0, columnShape.length);
		dshape[dshape.length-1] = ILazyWriteableDataset.UNLIMITED;
		
		return new LazyWriteableDataset(name, dtype, dshape, null, null, null);
	}
}
