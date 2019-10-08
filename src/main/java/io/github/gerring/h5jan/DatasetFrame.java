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
package io.github.gerring.h5jan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.ByteDataset;
import org.eclipse.january.dataset.ComplexDoubleDataset;
import org.eclipse.january.dataset.ComplexFloatDataset;
import org.eclipse.january.dataset.CompoundByteDataset;
import org.eclipse.january.dataset.CompoundDoubleDataset;
import org.eclipse.january.dataset.CompoundFloatDataset;
import org.eclipse.january.dataset.CompoundIntegerDataset;
import org.eclipse.january.dataset.CompoundLongDataset;
import org.eclipse.january.dataset.CompoundShortDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DateDataset;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.ObjectDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShortDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;

/**
 * Dataset Frame is a holder of the kind of data needed by DataFrame
 * @author Matthew Gerring
 *
 */
class DatasetFrame {
	
	protected String 		name;
	protected int   		dtype=-1; // NONE
	protected ILazyDataset 	data;
	protected int 			index=0;
	protected List<String> 	columnNames;

	public DatasetFrame() {
	}

	public DatasetFrame(String name) {
		this();
		this.name = name;
	}
	
	public DatasetFrame(String name, int dtype, Dataset... columns) throws DatasetException {
		this(name);
		int[] shape = checkSameShape(columns);
		int[] full  = new int[shape.length+1];
		System.arraycopy(shape, 0, full, 0, shape.length);
		full[full.length-1] = columns.length;
		
		Dataset tdata = DatasetFactory.zeros(columns[0].getClass(), full);
		tdata.setName(name);
		
		setDtype(dtype);
		this.name = name;
		this.columnNames = new ArrayList<String>();
		for (int i = 0; i < columns.length; i++) {
			
			if (columns[i].getName() == null) {
				throw new IllegalArgumentException("All columns must have a name please!");
			}
			columnNames.add(columns[i].getName());
			DatasetFrame.addDimension(columns[i]);
			tdata.setSlice(columns[i], orient(tdata, i, full));
		}
		this.data = tdata;
		check(this.data, this.columnNames);
	}


	/**
	 * The classic data frame style constructor.
	 * The dataset can either be lazy or in memory.
	 * You can choose to write it in slices or all at once.
	 * @param data - nD array data
	 * @param index - index of this data frame.
	 * @param columnNames - names of columns, last axis of data
	 * @param dtype - to write, e.g. Dataset.FLOAT32
	 */
	public DatasetFrame(ILazyDataset data, int dtype) {
		this(data.getName());
		this.data = data;
		setDtype(dtype);
		
		if (data.getRank()<1) {
			throw new IllegalArgumentException("The dataset rank must not be 0!");
		}
		int[] shape = data.getShape();
		int size = shape[shape.length-1];

		this.columnNames = IntStream.range(0, size).mapToObj(i->"column_"+i).collect(Collectors.toList());
		check(data, columnNames);
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
		this.columnNames = names;
		setDtype(dtype);
		check(data, names);
	}
	

	private static final void check(ILazyDataset data, List<String> names) {
		if (data==null) {
			throw new IllegalArgumentException("Null data is not allowed");
		}
		if (names==null) {
			throw new IllegalArgumentException("Null column names are not allowed");
		}
		if (data.getRank()<2) {
			throw new IllegalArgumentException("The data must be rank 2 or larger!");
		}
		int[] shape = data.getShape();
		int nCol = shape[shape.length-1];
		if (nCol!=names.size()) {
			throw new IllegalArgumentException("The size of the final dimension and the column names must be equal!");
		}
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
		if (!interface2DTypes.values().contains(dtype)) {
			throw new IllegalArgumentException("Cannot fomd dtype: "+dtype);
		}
		this.dtype = dtype;
	}

	public ILazyDataset getData() {
		return data;
	}

	public void setData(ILazyDataset data) {
		check(data, this.columnNames);
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> names) {
		check(this.data, names);
		this.columnNames = names;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + dtype;
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((columnNames == null) ? 0 : columnNames.hashCode());
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
		if (columnNames == null) {
			if (other.columnNames != null)
				return false;
		} else if (!columnNames.equals(other.columnNames))
			return false;
		return true;
	}

	
	public static SliceND orient(ILazyDataset data, int i, int[]shape) {
		int[] from = new int[shape.length];
		from[from.length-1] = i;
		
		int[] to = new int[shape.length];
		System.arraycopy(shape, 0, to, 0, to.length);
		to[to.length-1] = i+1;
		
		int[]step = new int[shape.length];
		Arrays.fill(step, 1);
		
		SliceND ret = SliceND.createSlice(data, from, to, step);
		return ret;
	}

	public static void addDimension(IDataset slice) {
		int [] shape = new int[slice.getShape().length+1];
		System.arraycopy(slice.getShape(), 0, shape, 0, slice.getShape().length);
		shape[shape.length-1] = 1;
		slice.resize(shape);
	}
	
	public DatasetFrame clone() {
		DatasetFrame ret = new DatasetFrame();
		ret.columnNames = new ArrayList<>(this.columnNames);
		ret.data = this.data; // Do not copy
		ret.dtype = this.dtype;
		ret.index = this.index;
		ret.name = this.name;
		return ret;
	}
	
	private static final Map<Class<? extends Dataset>, Integer> interface2DTypes = createInterfaceMap(); // map interface to dataset type

	private static Map<Class<? extends Dataset>, Integer> createInterfaceMap() {
		Map<Class<? extends Dataset>, Integer> map = new HashMap<Class<? extends Dataset>, Integer>();
		map.put(BooleanDataset.class, Dataset.BOOL);
		map.put(ByteDataset.class, Dataset.INT8);
		map.put(ShortDataset.class, Dataset.INT16);
		map.put(IntegerDataset.class, Dataset.INT32);
		map.put(LongDataset.class, Dataset.INT64);
		map.put(FloatDataset.class, Dataset.FLOAT32);
		map.put(DoubleDataset.class, Dataset.FLOAT64);
		map.put(CompoundByteDataset.class, Dataset.ARRAYINT8);
		map.put(CompoundShortDataset.class, Dataset.ARRAYINT16);
		map.put(CompoundIntegerDataset.class, Dataset.ARRAYINT32);
		map.put(CompoundLongDataset.class, Dataset.ARRAYINT64);
		map.put(CompoundFloatDataset.class, Dataset.ARRAYFLOAT32);
		map.put(CompoundDoubleDataset.class, Dataset.ARRAYFLOAT64);
		map.put(ComplexFloatDataset.class, Dataset.COMPLEX64);
		map.put(ComplexDoubleDataset.class, Dataset.COMPLEX128);
		map.put(ObjectDataset.class, Dataset.OBJECT);
		map.put(StringDataset.class, Dataset.STRING);
		map.put(DateDataset.class, Dataset.DATE);
		map.put(RGBDataset.class, Dataset.RGB);
		map.put(ObjectDataset.class, Dataset.OBJECT);
		return map;
	}

}
