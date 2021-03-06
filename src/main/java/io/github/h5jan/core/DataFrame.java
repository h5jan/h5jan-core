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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.StringDatasetBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.h5jan.io.h5.Appender;
import io.github.h5jan.io.h5.GenericMetadata;
import io.github.h5jan.io.h5.NxsFile;
import io.github.h5jan.io.h5.NxsMetadata;

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
public class DataFrame extends AbstractDataFrame {
		
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
		return to_hdf(filePath, h5Path, -1);
	}
	/**
	 * This method writes the data in slices down the column axis.
	 * If your data is already in a true LazyDataset it will be sliced
	 * in the column axis and each slice written to HDF. This might 
	 * be more memory efficient depending on what you are writing.
	 *  
	 * @param filePath - path to file
	 * @param h5Path - path in h5
	 * @param h5Path - NexusFile.COMPRESSION_NONE or NexusFile.COMPRESSION_LZW_L1
	 * The former is no compress, the latter is compressed.
	 * @throws NullPointerException - if data incomplete
	 * @throws IllegalArgumentException - if paths invalid
	 * @throws IOException - if cannot write file
	 * @throws Exception - any other error and HDF errors.
	 */
	public DataFrame to_hdf(String filePath, String h5Path, int compression) throws Exception {
		
		checkString(filePath, "There is no file path!");
		checkString(h5Path,   "There is no h5 path!");
		check();
		
		// Make an empty frame to use as a lazy writer.
		DataFrame frame = new DataFrame(getName(), getDtype(), getColumnShape());
		frame.setMetadata(getMetadata());
		frame.aux.putAll(this.aux);
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf(filePath, h5Path)) {
			
			if (compression>-1) { // -ve means leave as default.
				app.setCompression(compression); // Can make file small if set correctly.
			}
			
			List<String> cnames = getColumnNames();
			for (int i = 0; i < size(); i++) {
				IDataset slice = get(i).getSlice().squeeze();
				String columnName = cnames.get(i);
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
			writer = FrameUtil.create(name, dtype, columnShape);
		} else {
			throw new IllegalArgumentException("There is not enough data in the frame to create a LazyWriteableDataset!");
		}
		return Appender.instance(filePath, h5Path, writer, this, ()->this.open=false);
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
			ILazyDataset laz = nfile.getDataset(dataPath);
			this.data = unpack(laz, this.columnNames);
			this.dtype = DTypeUtils.getDType(laz);
			this.columnShape = null; // When we reset the data the columns could now be different.
			this.name = gdata.getAttribute(Constants.NAME).getValue().getString();
			
			Attribute attrib = gdata.getAttribute(Constants.AUX);
			if (attrib!=null) {
				IDataset anames = attrib.getValue();
				String[] auxNames = (String[])((StringDatasetBase)anames).getBuffer();
				for (String auxName : auxNames) {
					ILazyDataset set = nfile.getDataset(path+"/"+auxName);
					put(auxName, set);
				}
			}
			
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
	
	/**
	 * Loads raw data to primitive 1D primitive arrays.
	 * An nD column will return the raw buffer backed in January.
	 * This is really only useful for writing certain data types.
	 * 
	 * WARNING: This action loads all the data into memory!
	 * 
	 * @return map of raw data.
	 * @throws DatasetException 
	 */
	public Map<String, Object> raw() throws DatasetException {
		Map<String,Object> raw = new LinkedHashMap<>();
		for (String name : columnNames) {
			Dataset s = DatasetUtils.convertToDataset(get(name).getSlice());
			raw.put(name, s.getBuffer());
		}
		return raw;
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
		if (columnShape==null && !isEmpty()) return get(0).getShape();
		return columnShape;
	}

	public void setColumnShape(int[] columnShape) {
		this.columnShape = columnShape;
	}
}
