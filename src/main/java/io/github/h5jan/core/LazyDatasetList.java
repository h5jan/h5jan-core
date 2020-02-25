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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.eclipse.january.dataset.DateDataset;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.ObjectDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShortDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.StringDataset;

/**
 * Dataset Frame is a holder of the kind of data needed by DataFrame
 * 
 * This class is similar to DataHolder and AggregateDataset from Janauary.
 * However it attempts to have equivalent methods to the API from DataFrame.
 * @see https://github.com/DawnScience/scisoft-core/blob/master/uk.ac.diamond.scisoft.analysis/src/uk/ac/diamond/scisoft/analysis/io/DataHolder.java
 * @see https://github.com/eclipse/january/blob/master/org.eclipse.january/src/org/eclipse/january/dataset/AggregateDataset.java
 * 
 * @author Matthew Gerring
 *
 */
class LazyDatasetList implements List<ILazyDataset> {
		
	protected String 				name;
	protected int   				dtype=-1; // NONE
	protected List<ILazyDataset> 	data;
	protected int 					index=0;
	protected List<String> 			columnNames;

	public LazyDatasetList() {
	}

	public LazyDatasetList(String name) {
		this();
		this.name = name;
	}
	
	public LazyDatasetList(String name, int dtype, ILazyDataset... columns) throws DatasetException {
		this(name);
		setDtype(dtype);
		this.data = new ArrayList<>();
		data.addAll(Arrays.asList(columns));
		this.columnNames = new ArrayList<>();
		for (int i = 0; i < columns.length; i++) {
			String cname = columns[i].getName();
			if (cname==null || cname.length()<1) cname = "column_"+i;
			columnNames.add(cname);
		}
		FrameUtil.check(this.data, this.columnNames);
	}

	public LazyDatasetList(String name, int dtype, List<? extends ILazyDataset> columns) throws DatasetException {
		this(name);
		setDtype(dtype);
		this.data = new ArrayList<>();
		data.addAll(columns);
		this.columnNames = new ArrayList<>();
		for (int i = 0; i < columns.size(); i++) {
			String cname = columns.get(i).getName();
			if (cname==null || cname.length()<1) cname = "column_"+i;
			columnNames.add(cname);
		}
		FrameUtil.check(this.data, this.columnNames);
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
	public LazyDatasetList(ILazyDataset stack, int dtype) {
		this(stack.getName());
		if (stack.getRank()<1) {
			throw new IllegalArgumentException("The dataset rank must not be 0!");
		}
		
		// Size comes from the last dimension, not the first.
		int size = stack.getShape()[stack.getShape().length-1];
		this.columnNames = new ArrayList<>(IntStream.range(0, size).mapToObj(i->"column_"+i).collect(Collectors.toList()));
		this.data = unpack(stack, columnNames);
		setDtype(dtype);
		
		FrameUtil.check(data, columnNames);
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
	public LazyDatasetList(ILazyDataset stack, int index, List<String> names, int dtype) {
		this(stack.getName());
		this.data = unpack(stack, names);
		this.index = index;
		this.columnNames = new ArrayList<>(names);
		setDtype(dtype);
		FrameUtil.check(data, names);
	}
	
	/**
	 * Get the shape of the data.
	 * @return shape
	 */
	public int[] getShape() {
		return FrameUtil.getShape(data);
	}
	
	/**
	 * Unpacks the stack using the last dimension as the stack index.
	 * AggregateDataset uses the first dimension.
	 * @param stack to unpack.
	 * @return list of slice views.
	 */
	protected List<ILazyDataset> unpack(ILazyDataset stack, List<String> columnNames) {

		int[] shape = stack.getShape();
		if (shape.length<2) {
			throw new IllegalArgumentException("Dataset must have rank 2 or more!");
		}
		
		int count = shape[shape.length-1]; // The last dimension, DataFrame style.
		List<ILazyDataset> data = new ArrayList<>(count);
		Slice[] slices = new Slice[shape.length];
		for (int i = 0; i < slices.length; i++) slices[i] = new Slice();
		
		for (int i = 0; i < count; i++) {
			slices[slices.length-1] = new Slice(i,i+1);
			ILazyDataset col = stack.getSliceView(slices);
			col = col.squeezeEnds();
			if (i>-1 && i<columnNames.size()) {
			    col.setName(columnNames.get(i));
			} else {
				col.setName("column_"+i);
			}
			data.add(col);
		}
		return data;
	}
	
	/**
	 * Stiches the images in this list into a single tile
	 * using the shape suggested. The size of this shape must be
	 * equal to the size of the tile.
	 * 
	 * @param tileShape - shape of tiles
	 * @return stiched dataset
	 * @throws DatasetException - if cannot stitch
	 */
	public Dataset stitch(int[] tileShape) throws DatasetException {
		Stitcher sticher = new Stitcher(this, tileShape);
		return sticher.stitch();
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

	/**
	 * This perhaps could be 
	 */
	public List<ILazyDataset> getData() {
		return data;
	}

	public void setData(ILazyDataset data) {
		List<ILazyDataset> expanded = unpack(data, this.columnNames);
		FrameUtil.check(expanded, this.columnNames);
		this.data = expanded;
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
		FrameUtil.check(this.data, names);
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
		LazyDatasetList other = (LazyDatasetList) obj;
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

	/**
	 * Get the column of this name.
	 * @param index - index of column
	 * @return column
	 * @throws DatasetException - If cannot get slice for column
	 */
	public ILazyDataset get(int index) {
		if (data == null|| data.isEmpty()) throw new IllegalArgumentException("Incomplete data frame, no data!");
		if (index<0 || index>=data.size()) throw new IllegalArgumentException("No such column "+index);
		return data.get(index);
	}
	
	/**
	 * Get the column of this name.
	 * @param name of column
	 * @return column
	 * @throws DatasetException - If cannot get slice for column
	 * @throws NullPointerException if name is not known.
	 */
	public ILazyDataset get(String name) throws DatasetException {
		return get(name, false);
	}
	
	/**
	 * Get the column of this name.
	 * @param name of column
	 * @param ignoreCase true to ignore case of dataset
	 * @return dataset
	 * @throws DatasetException
	 */
	public ILazyDataset get(String name, boolean ignoreCase) throws DatasetException {
		if (columnNames == null) throw new DatasetException("Incomplete data frame, no column names!");
		
		List<String> testList = ignoreCase ? columnNames.stream().map(String::toLowerCase).collect(Collectors.toList()) : columnNames;
		String testString = ignoreCase ? name.toLowerCase() : name;
		if (!testList.contains(testString)) throw new DatasetException("No such column "+name);
		return get(testList.indexOf(testString));
	}
	
	/**
	 * Get the column of this name.
	 * @param regExName a regex to match the name
	 * @return dataset
	 * @throws DatasetException
	 */
	public ILazyDataset find(String regExName) throws DatasetException {
		if (columnNames == null) throw new DatasetException("Incomplete data frame, no column names!");
		int index = -1;
		for (int i = 0; i < columnNames.size(); i++) {
			String name = columnNames.get(i);
			if (name.matches(regExName)) {
				index = i;
				break;
			}
		}
		if (index<0) throw new DatasetException("The regex '"+regExName+"' could not be matched! Available names are: "+columnNames);
		return get(index);
	}

	/**
	 * Append this dataset to the end of the data frame (last column)
	 * NOTE: Although this takes a lazy dataset, it will load the data from it.
	 * 
	 * @return data to add
	 * @throws DatasetException - If cannot get slice for column
	 */
	@Override
	public boolean add(ILazyDataset toAdd) {

		check(toAdd);
		this.columnNames.add(toAdd.getName());
		return this.data.add(toAdd);
	}

	@Override
	public void add(int index, ILazyDataset toAdd) {
		check(toAdd);
		this.columnNames.add(index, toAdd.getName());
		data.add(index, toAdd);
	}

	private void check(ILazyDataset toAdd) {
		if (toAdd.getName() == null || toAdd.getName().trim().length()<1) throw new IllegalArgumentException("The data to add must be named!");
		List<ILazyDataset> toCheck = this.data!=null ? new ArrayList<>(this.data) : new ArrayList<>();
		toCheck.add(toAdd);
		FrameUtil.checkSameShape(toCheck);
		
		if (this.columnNames==null || data == null) {
			if (toAdd instanceof Dataset) {
				this.dtype = ((Dataset)toAdd).getDType();
			}
			this.columnNames = new ArrayList<>();
			this.data = new ArrayList<>();
		}
		
		if (toAdd instanceof Dataset) {
			int dtype = ((Dataset)toAdd).getDType();
			if (this.dtype != dtype) {
				throw new IllegalArgumentException("The dtype of this DataFrame is '"+this.dtype+"'. You cannot add dtype '"+dtype+"' to it!");
			}
		}
	}

	/**
	 * Test if this is a frame waiting for data to be filled.
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return (columnNames==null && data==null);
	}

	
	public LazyDatasetList clone() {
		LazyDatasetList ret = new LazyDatasetList();
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

	public void forEach(Consumer<? super ILazyDataset> action) {
		data.forEach(action);
	}

	public int size() {
		return data.size();
	}

	public boolean contains(Object o) {
		return data.contains(o);
	}

	public Iterator<ILazyDataset> iterator() {
		return data.iterator();
	}

	public Object[] toArray() {
		return data.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}

	public boolean remove(Object o) {
		return data.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	public boolean addAll(Collection<? extends ILazyDataset> c) {
		return data.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends ILazyDataset> c) {
		return data.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	public void replaceAll(UnaryOperator<ILazyDataset> operator) {
		data.replaceAll(operator);
	}

	public void sort(Comparator<? super ILazyDataset> c) {
		data.sort(c);
	}

	public void clear() {
		data.clear();
	}

	public boolean removeIf(Predicate<? super ILazyDataset> filter) {
		return data.removeIf(filter);
	}

	public ILazyDataset set(int index, ILazyDataset element) {
		return data.set(index, element);
	}

	public ILazyDataset remove(int index) {
		return data.remove(index);
	}

	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	public ListIterator<ILazyDataset> listIterator() {
		return data.listIterator();
	}

	public ListIterator<ILazyDataset> listIterator(int index) {
		return data.listIterator(index);
	}

	public List<ILazyDataset> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	public Spliterator<ILazyDataset> spliterator() {
		return data.spliterator();
	}

	public Stream<ILazyDataset> stream() {
		return data.stream();
	}

	public Stream<ILazyDataset> parallelStream() {
		return data.parallelStream();
	}

	@Override
	public String toString() {
		return "LazyDatasetList [name=" + name + ", dtype=" + dtype + ", index=" + index + ", columnNames="
				+ columnNames + "]";
	}

}
