/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.axis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;

/**
 * Container that holds values for an axis to Plot
 */

public class AxisValues implements Iterable<Double>, Serializable, Cloneable {

	private DoubleDataset values = null;
	private String name;

	private double minValue;
	private double maxValue;
	private boolean isDirty; // flag if min or max needs to be recalculated

	/**
	 * @return Returns the values.
	 */
	public List<Double> getValues() {
		if (values == null) return new ArrayList<Double>();
		ArrayList<Double> arrayList = new ArrayList<Double>();
		for (int i = 0; i < values.getShape()[0]; i++) {
			arrayList.add(values.get(i));
		}
		return arrayList;
	}

	/**
	 * @return Returns a 1D dataset of values
	 */
	public DoubleDataset toDataset() {
		return values;
	}

	/**
	 * Default constructor
	 */
	public AxisValues() {
		values = null;
		minValue = Float.MAX_VALUE;
		maxValue = -Float.MAX_VALUE;
		isDirty = false;
	}

	/**
	 * Calls setValues with the data.
	 * 
	 * @param data
	 */
	public AxisValues(List<Double> data) {
		setValues(data);
	}

	/**
	 * @param doubleArray
	 */
	public AxisValues(double[] doubleArray) {
		setValues(doubleArray);
	}

	/**
	 * @param data
	 */
	public AxisValues(Dataset data) {
		setValues(data);
	}

	public AxisValues(String name, IDataset data) {
		this.name=name;
		if (data!=null) setValues(data);
	}

	/**
	 * Add a new entry into the collection
	 * 
	 * @param newValue
	 */
	public void addValue(double newValue) {
		addValues(newValue);
	}

	/**
	 * Add new entries into the collection
	 * 
	 * @param newValues
	 */
	public void addValues(double... newValues) {
		if (values == null) {
			values = DatasetFactory.createFromObject(DoubleDataset.class, newValues);
		} else {
			int n = values.getSize();
			values.resize(n + newValues.length);
			for (double v : newValues) {
				values.set(v, n++);
			}
		}
		isDirty = true;
	}

	/**
	 * Please only call with the x values in increasing order.
	 * 
	 * @param v
	 *            The values to set.
	 */
	public void setValues(List<Double> v) {
		values = (DoubleDataset) DatasetFactory.createFromList(v);
		isDirty = true;
	}

	/**
	 * @param v
	 */
	public void setValues(double[] v) {
		values = DatasetFactory.createFromObject(DoubleDataset.class, v);
		isDirty = true;
	}

	/**
	 * Set axis with a dataset after clear out stored values
	 * 
	 * @param data
	 */
	public void setValues(IDataset data) {
		values = DatasetUtils.cast(DoubleDataset.class, data);
		isDirty = true;
	}

	public void setLowEntry(double value) {
		boolean overwriteMin = (values.get(0) == minValue);
		values.set(value, 0);
		if (overwriteMin)
			minValue = value;
	}

	public void setTopEntry(double value) {
		int n = values.getSize() - 1;
		boolean overwriteMax = values.get(n) == maxValue;
		values.set(value, n);
		if (overwriteMax)
			maxValue = value;
	}

	/**
	 * Return the value at the given position number in the collection
	 * 
	 * @param nr
	 *            position number
	 * @return the value at the position number
	 */
	public double getValue(int nr) {
		int n = values.getSize();
		if (n > 0) {
			if (nr < n)
				return values.get(nr);
			return values.get(n - 1);
		}
		return Double.NaN;
	}

	/**
	 * Get the distance between two position numbers in the collection
	 * 
	 * @param nr
	 *            first position number
	 * @param nr2
	 *            second position number
	 * @return the distance between the two
	 */
	public double distBetween(int nr, int nr2) {
		return values.get(nr2) - values.get(nr);
	}

	/**
	 * Clears the collection of all values and resets the min and max value
	 */
	public void clear() {
		values = null;
		maxValue = -Float.MAX_VALUE;
		minValue = Float.MAX_VALUE;
		isDirty = false;
	}

	/**
	 * Find the nearest element number in the values list to the request value if the value is larger or smaller than
	 * the largest / smallest entry in the list it will return -1. It will always return the nearest upper boundary
	 * entry
	 * 
	 * @param value
	 *            search value
	 * @return -1 if none could be found otherwise the upper boundary element number
	 */
	public int nearestUpEntry(double value) {
		if (isDirty)
			sanityCheckMinMax();
		if (value > maxValue || value < minValue)
			return -1;

		int counter = isAscending() ? DatasetUtils.findIndexGreaterThanOrEqualTo(values, value) : DatasetUtils.findIndexLessThan(values, value);
		if (counter >= values.getSize())
			return -1;
		return counter;
	}

	/**
	 * Find the nearest element number in the values list to the request value if the value is larger or smaller than
	 * the largest / smallest entry in the list it will return -1. It will always return the nearest lower boundary
	 * entry
	 * 
	 * @param value
	 *            search value
	 * @return -1 if none could be found otherwise the lower boundary element number
	 */
	public int nearestLowEntry(double value) {
		if (isDirty)
			sanityCheckMinMax();
		if (value > maxValue || value < minValue)
			return -1;

		if (isAscending()) {
			int counter = DatasetUtils.findIndexGreaterThanOrEqualTo(values, value);
			if (counter >= values.getSize()) {
				return -1;
			}
			if (values.getAbs(counter) > value) {
				counter--;
			}
			return counter;
		}
		int counter = DatasetUtils.findIndexLessThan(values, value);
		if (counter >= values.getSize()) {
			return -1;
		}
		return counter - 1;
	}

	/**
	 * Get a subset from the AxisValues
	 * 
	 * @param start
	 *            beginning
	 * @param stop
	 *            end (exclusive)
	 * @return the subset of the AxisValues
	 */
	public AxisValues subset(int start, int stop) {
		return subset(start, stop, 1);
	}

	/**
	 * Get a subset from the AxisValues
	 * 
	 * @param start
	 *            beginning
	 * @param stop
	 *            end (exclusive)
	 * @param step
	 * @return the subset of the AxisValues
	 */
	public AxisValues subset(int start, int stop, int step) {
		return new AxisValues(values.getSlice(new int[] { start}, new int[] { stop }, new int[] { step }));
	}

	/**
	 * Determine number of elements
	 * 
	 * @return number of elements
	 */
	public int size() {
		if (values == null) {
			return 0;
		}

		return values.getSize();
	}

	@Override
	public AxisValues clone() {
		AxisValues cloned = new AxisValues(values);
		cloned.maxValue = this.maxValue;
		cloned.minValue = this.minValue;
		cloned.isDirty = this.isDirty;
		return cloned;
	}

	/**
	 * @return Returns the minValue.
	 */
	public double getMinValue() {
		sanityCheckMinMax();
		return minValue;
	}

	/**
	 * @param minValue
	 *            The minValue to set.
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
		isDirty = false;
	}

	/**
	 * @return Returns the maxValue.
	 */
	public double getMaxValue() {
		sanityCheckMinMax();
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            The maxValue to set.
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		isDirty = false;
	}

	private void sanityCheckMinMax() {
		if (isDirty) {
			minValue = values.min(true).doubleValue();
			maxValue = values.max(true).doubleValue();
			isDirty = false;
		}
		if (minValue > maxValue) {
			double temp = minValue;
			minValue = maxValue;
			maxValue = temp;
		}
	}

	/**
	 * Check if the AxisValues are ascending or descending
	 * 
	 * @return true if ascending otherwise false
	 */
	public boolean isAscending() {
		if (values.getShape()[0] > 0)
			return values.get(0) < values.get(values.getShape()[0] - 1);
		return true;
	}

	@Override
	public String toString() {
		return values != null ? values.toString() : "Empty";
	}

	private static Iterator<Double> NULL_ITERATOR = new Iterator<Double>() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Double next() {
			throw new NoSuchElementException("No elements left as no values defined");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");
		}
	}; 

	/**
	 * Returns an iterator but note that remove is not supported
	 * @return an iterator
	 */
	@Override
	public Iterator<Double> iterator() {
		Iterator<Double> iterator = values == null ? NULL_ITERATOR : new Iterator<Double>() {
			IndexIterator it = values.getIterator();
			boolean read = true;

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported");
			}

			@Override
			public Double next() {
				if (read) {
					if (it.hasNext()) {
						read = false;
						return next();
					}
					throw new NoSuchElementException("No elements left");
				}
				read = true;
				return values.getAbs(it.index);
			}

			@Override
			public boolean hasNext() {
				if (read) {
					read = false;
					return it.hasNext();
				}
				return true;
			}
		};

		return iterator;
	}

	public String getName() {
		if (name!=null)   return name;
		if (values!=null) return values.getName();
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isData() {
		return values!=null;
	}

}
