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
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
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
public class DataFrame extends DatasetFrame {
		
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger       logger = LoggerFactory.getLogger(DataFrame.class);

	private volatile boolean open = false;
	private NxsMetadata metadata;
	
	public DataFrame() {
		super();
	}

	public DataFrame(String name) {
		super(name);
	}
	
	public DataFrame(String name, int dtype, Dataset... columns) throws DatasetException {
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
		
		
		check();
		try(NxsFile nfile = NxsFile.create(filePath)) {
			
			IDataset toWrite = this.data.getSlice();
			if (dtype>-1) {
				toWrite = DatasetUtils.cast(toWrite, this.dtype); // Does nothing if it can.
			}
			toWrite.setName(data.getName());
			
			// We have all the data in memory, it might be large at this point!
			nfile.createData(h5Path, toWrite, true);
			Util.setMetaAttributues(nfile, h5Path, this);
			Util.setReferenceAttributes(nfile, h5Path, data.getName());
		}
		return this;
	}
	
	private void check() {
		if (getColumnNames() == null) {
			throw new IllegalArgumentException("The columns must be named!");
		}
		if (getData() == null) {
			throw new IllegalArgumentException("The data must not be null!");
		}
		if (data.getShape()[data.getRank()-1]!=columnNames.size()) {
			throw new IllegalArgumentException("The names must match the last dimension of data!");
		}
	}

	/**
	 * This method writes the data in slices down the column axis.
	 * If your data is already in a true LazyDataset it will be sliced
	 * in the column axis and each slice written to HDF. This might 
	 * be more memory efficient depending on what you are writing.
	 *  
	 * @param filePath
	 * @throws Exception 
	 */
	public DataFrame to_lazy_hdf(String filePath, String h5Path) throws Exception {
		
		
		check();
		
		// Make size of slice
		int size = getData().getShape()[getData().getRank()-1];
		int[] sshape = new int[getData().getRank()-1];
		System.arraycopy(getData().getShape(), 0, sshape, 0, sshape.length);

		// Make writeable dataset
		ILazyWriteableDataset data = DataFrame.create("data", getDtype(), sshape, size);

		// Clone a frame to 
		DataFrame writer = clone();
		writer.data = data;
		writer.columnNames = new ArrayList<>();
		try (Appender app = writer.open_hdf(filePath, h5Path)) {
			
			// Take the data of each slice and put it back again, this time writing.
			for (int i = 0; i < size; i++) {
				SliceND position = DatasetFrame.orient(getData(),i,getData().getShape());
				IDataset slice = getData().getSlice(position).squeeze();
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
		if (!(this.data instanceof ILazyWriteableDataset)) {
			throw new IllegalArgumentException("In order to open for slice writing please create with ILazyWriteableDataset!");
		}
		
		if (columnNames==null) columnNames = new ArrayList<>();
		this.open = true;
		
		return new AppenderImpl(columnNames, filePath, h5Path, (ILazyWriteableDataset)data, this, ()->this.open=false);
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
			this.data = nfile.getDataset(dataPath);
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
						data.addMetadata(meta);
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
		
		return create(name, dtype, columnShape, ILazyWriteableDataset.UNLIMITED);
	}

	/**
	 * Creates a writeable dataset using the shape of one column.
	 * @param name
	 * @param dtype
	 * @param columnShape
	 * @return writeable dataset
	 */
	public static ILazyWriteableDataset create(String name, int dtype, int[] columnShape, int sizeSlices) {
		
		int[] dshape = new int[columnShape.length+1];
		System.arraycopy(columnShape, 0, dshape, 0, columnShape.length);
		dshape[dshape.length-1] = sizeSlices;
		
		return new LazyWriteableDataset(name, dtype, dshape, null, null, null);
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
}
