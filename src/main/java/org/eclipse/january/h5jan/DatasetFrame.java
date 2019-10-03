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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

/**
 * Dataset Frame is a holder of the kind of data needed by DataFrame
 * @author Matthew Gerring
 *
 */
class DatasetFrame {
	
	protected String 		name;
	protected int   		dtype;
	protected ILazyDataset 	data;
	protected int 			index=0;
	protected List<String> 	names;

	public DatasetFrame(String name) {
		this.name = name;
	}
	
	public DatasetFrame(String name, int dtype, IDataset... columns) throws DatasetException {
		this(name);
		int[] shape = checkSameShape(columns);
		int[] full  = new int[shape.length+1];
		System.arraycopy(shape, 0, full, 0, shape.length);
		full[full.length-1] = columns.length;
		this.dtype = dtype;
		this.data = new AggregateDataset(true, columns);
		this.names = new ArrayList<String>();
		for (int i = 0; i < columns.length; i++) {
			names.add(columns[i].getName());
		}
	}


	/**
	 * The classic data frame style constructor.
	 * The dataset can either be lazy or in memory.
	 * You can choose to write it in slices or all at once.
	 * @param data - nD array data
	 * @param index - index of this data frame.
	 * @param names - names of columns, last axis of data
	 * @param dtype - to write, e.g. Dataset.FLOAT32
	 */
	public DatasetFrame(ILazyDataset data, int dtype) {
		this(data.getName());
		this.data = data;
		this.dtype = dtype;
	}

	/**
	 * The classic data frame style constructor.
	 * The dataset can either be lazy or in memory.
	 * You can choose to write it in slices or all at once.
	 * @param data - nD array data
	 * @param index - index of this data frame.
	 * @param names - names of columns, last axis of data
	 * @param dtype - to write, e.g. Dataset.FLOAT32
	 */
	public DatasetFrame(ILazyDataset data, int index, List<String> names, int dtype) {
		this(data.getName());
		this.data = data;
		this.index = index;
		this.names = names;
		this.dtype = dtype;
	}
	

	private int[] checkSameShape(ILazyDataset[] columns) {
		int[] shape = columns[0].getShape();
		if (columns.length>1) {
			for (int i = 1; i < columns.length; i++) {
				if (!Arrays.equals(shape, columns[i].getShape())) {
					throw new IllegalArgumentException("Columns must be same shape!");
				}
			}
		}
		return shape;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDtype() {
		return dtype;
	}

	public void setDtype(int dtype) {
		this.dtype = dtype;
	}

	public ILazyDataset getData() {
		return data;
	}

	public void setData(ILazyDataset data) {
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + dtype;
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatasetFrame other = (DatasetFrame) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (dtype != other.dtype)
			return false;
		if (index != other.index)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		return true;
	}


}
