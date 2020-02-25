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

import java.util.Arrays;
import java.util.List;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

/**
 * Utils for shape and size of data held in data frame.
 * @author Matthew Gerring
 *
 */
public class FrameUtil {


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
		
		int[] dshape = declaredShape(columnShape, sizeSlices);	
		return new LazyWriteableDataset(name, dtype, dshape, null, null, null);
	}

	/**
	 * Creates a writeable dataset using the shape of one column.
	 * @param name
	 * @param dtype
	 * @param columnShape
	 * @return writeable dataset
	 */
	public static ILazyWriteableDataset create(String name, Class<?>  dtype, int[] columnShape) {
		
		int[] dshape = declaredShape(columnShape, ILazyWriteableDataset.UNLIMITED);	
		return new LazyWriteableDataset(name, dtype, dshape, null, null, null);
	}

	/**
	 * Creates a writeable dataset using the shape of one column.
	 * @param name
	 * @param dtype
	 * @param columnShape
	 * @return writeable dataset
	 */
	public static ILazyWriteableDataset create(String name, Class<?> dtype, int[] columnShape, int sizeSlices) {
		
		int[] dshape = declaredShape(columnShape, sizeSlices);	
		return new LazyWriteableDataset(name, dtype, dshape, null, null, null);
	}

	private static int[] declaredShape(int[] columnShape, int sizeSlices) {
		int[] dshape = new int[columnShape.length+1];
		System.arraycopy(columnShape, 0, dshape, 0, columnShape.length);
		dshape[dshape.length-1] = sizeSlices;
		return dshape;
	}

	static final int[] getShape(List<ILazyDataset> data) {
		checkSameShape(data);
		int[] shape = data.get(0).getShape();
		int[] ret = new int[shape.length+1];
		System.arraycopy(shape, 0, ret, 0, shape.length);
		ret[ret.length-1] = data.size();
		return ret;
	}

	static final void check(List<ILazyDataset> data, List<String> names) {
		if (names==null) {
			throw new IllegalArgumentException("Null column names are not allowed");
		}
		checkSameShape(data);		
		if (data.size()!=names.size()) {
			throw new IllegalArgumentException("The size of the final dimension and the column names must be equal!");
		}
	}

	static int[] checkSameShape(List<ILazyDataset> columns) {
		if (columns==null) {
			throw new IllegalArgumentException("Null data is not allowed");
		}		
		return checkSameShape(columns.toArray(new ILazyDataset[columns.size()]));
	}

	static int[] checkSameShape(ILazyDataset[] columns) {
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
	
	/**
	 * Orient the data in the frame to be written.
	 * @param data
	 * @param i
	 * @param shape
	 * @return
	 */
	public static SliceND orient(ILazyDataset data, int i, int[]shape) {
		
		if (shape==null) {
			shape = data.getShape();
		}
		
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

	/**
	 * Add a dimension to the slice
	 * @param slice
	 * @return
	 * @throws DatasetException
	 */
	public static IDataset addDimension(ILazyDataset slice) throws DatasetException {
		int [] shape = new int[slice.getShape().length+1];
		System.arraycopy(slice.getShape(), 0, shape, 0, slice.getShape().length);
		shape[shape.length-1] = 1;
		if (slice instanceof IDataset) {
			((IDataset)slice).resize(shape);
			return (IDataset)slice;
		} else {
			IDataset set = slice.getSlice(); // Loads data
			set.resize(shape);
			return set;
		}
	}

}
