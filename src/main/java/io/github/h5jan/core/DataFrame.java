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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.StringDatasetBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class DataFrame extends LazyDatasetList {
		
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger       logger = LoggerFactory.getLogger(DataFrame.class);

	/**
	 * The shape of a column or null if not set.
	 * NOTE: The columne shape may be null even if it is known.
	 * It is only used for lazy writing.
	 */
	private int[] columnShape; 
	
	private volatile boolean open = false;
	private NxsMetadata metadata;
	
	public DataFrame() {
		super();
	}

	public DataFrame(String name) {
		super(name);
	}
	
	/**
	 * This constructor usually used for writing lazy data.
	 * @param name - name
	 * @param dtype - data type
	 * @param columnShape - shape of columns we will be adding.
	 * @throws DatasetException if cannot write data.
	 */
	public DataFrame(String name, int dtype, int... columnShape) throws DatasetException {
		super(name);
		setDtype(dtype);
		this.columnShape = columnShape;
	}

	public DataFrame(String name, int dtype, Dataset... columns) throws DatasetException {
		super(name, dtype, columns);
	}
	
	public DataFrame(String name, int dtype, List<? extends ILazyDataset> columns) throws DatasetException {
		super(name, dtype, columns);
	}

	public DataFrame(ILazyDataset data, int dtype) {
		super(data, dtype);
	}

	public DataFrame(ILazyDataset data, int index, List<String> names, int dtype) {
		super(data, index, names, dtype);
	}

	private void check() {
		if (getColumnNames() == null) {
			throw new IllegalArgumentException("The columns must be named!");
		}
		if (getData() == null) {
			throw new IllegalArgumentException("The data must not be null!");
		}
		if (data.size()!=columnNames.size()) {
			throw new IllegalArgumentException("The names must match the last dimension of data!");
		}
		checkString(this.name, "There is no name for the data");
	}
	
	private void checkString(String toCheck, String message) {
		if (toCheck==null) {
			throw new NullPointerException(message);
		}
		if (toCheck.trim().length()<1) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * This method writes the data in slices down the column axis.
	 * If your data is already in a true LazyDataset it will be sliced
	 * in the column axis and each slice written to HDF. This might 
	 * be more memory efficient depending on what you are writing.
	 *  
	 * @param filePath - path to file
	 * @param h5Path - path in h5
	 * @throws NullPointerException - if data incomplete
	 * @throws IllegalArgumentException - if paths invalid
	 * @throws IOException - if cannot write file
	 * @throws Exception - any other error and HDF errors.
	 */
	public DataFrame to_hdf(String filePath, String h5Path) throws Exception {
		
		checkString(filePath, "There is no file path!");
		checkString(h5Path, "There is no h5 path!");
		check();
		
		// Make size of slice
		List<ILazyDataset> data = getData();

		// Clone a frame to 
		try (Appender app = open_hdf(filePath, h5Path)) {
			
			// Take the data of each slice and put it back again, this time writing.
			for (int i = 0; i < data.size(); i++) {
				IDataset slice = data.get(i).getSlice().squeeze();
				String columnName = columnNames.get(i);
				slice.setName(columnName);
				app.append(columnName, slice);
			}
		}
		return this;
	}

	/**
	 * Open the file for appending data yourself.
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
		
		if (columnNames==null) columnNames = new ArrayList<>();
		this.open = true;
		
		// Make writeable dataset
		ILazyWriteableDataset writer=null;
		if (data!=null) {
			writer = new LazyWriteableDataset(name, dtype, getShape(), null, null, null);
		} else if (columnShape!=null) {
			// We create a place to put our data
			writer = FrameUtil.create(name, Dataset.FLOAT32, columnShape);
		} else {
			throw new IllegalArgumentException("There is not enough data in the frame to create a LazyWriteableDataset!");
		}
		return new AppenderImpl(filePath, h5Path, writer, this, ()->this.open=false);
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
			String path = node.getAttribute(Constants.PATH).getValue().getString();
			String dataPath = node.getAttribute(Constants.DATA_PATH).getValue().getString();
			
			GroupNode gdata = nfile.getGroup(path, false);
			IDataset inames = gdata.getAttribute(Constants.COL_NAMES).getValue();
			String[] snames = (String[])((StringDatasetBase)inames).getBuffer();
			
			// Assign fields of DataFrame that we know.
			this.columnNames = new ArrayList<String>(Arrays.asList(snames));
			this.data = unpack(nfile.getDataset(dataPath), this.columnNames);
			this.name = gdata.getAttribute(Constants.NAME).getValue().getString();
			
			if (gdata.containsAttribute(Constants.META)) {
				String smeta = gdata.getAttribute(Constants.META).getValue().getString();
				
				if (smeta!=null) {
					
					NxsMetadata meta  = null;
					try {
						String metaType = gdata.getAttribute(Constants.META_TYPE).getValue().getString();
						
						@SuppressWarnings("unchecked")
						Class<? extends NxsMetadata> clazz = (Class<? extends NxsMetadata>) Class.forName(metaType);
						meta = mapper.readValue(smeta, clazz);
						
					} catch (Exception typeNotLoaded) {
						
						try {
							JsonNode jnode = mapper.readTree(smeta);
							meta = new GenericMetadata(jnode);
						} catch (Exception neOther) {
							logger.error("Cannot get metadata "+smeta, neOther);
						}
					}
					
					if (meta!=null) {
						setMetadata(meta);
					}
				}
			}
		}
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NxsMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(NxsMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataFrame other = (DataFrame) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		return true;
	}
	
	public DataFrame clone() {
		DataFrame ret = new DataFrame();
		ret.columnNames = new ArrayList<>(this.columnNames);
		ret.data = this.data; // Do not copy
		ret.dtype = this.dtype;
		ret.index = this.index;
		ret.name = this.name;
		if (this.metadata!=null) {
			ret.metadata = (NxsMetadata)this.metadata.clone();
		}
		return ret;
	}

	/**
	 * The number of columns in this frame.
	 * @return the count of columns by using the columnNames array.
	 * @throws NullPointerException if the columnNames are not set.
	 */
	public int size() {
		if (columnNames==null) return 0;
		return columnNames.size();
	}

	public int[] getColumnShape() {
		return columnShape;
	}

	public void setColumnShape(int[] columnShape) {
		this.columnShape = columnShape;
	}
}
