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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.ILazyDataset;

/**
 * A lazy dataset list which also has a map of auxilliary data.
 * 
 * @author Matthew Gerring
 *
 */
public class AbstractDataFrame extends LazyDatasetList {

	protected Map<String, ILazyDataset> aux = new LinkedHashMap<String, ILazyDataset>();

	public AbstractDataFrame() {
		super();
	}

	public AbstractDataFrame(ILazyDataset stack, int index, List<String> names, int dtype) {
		super(stack, index, names, dtype);
	}

	public AbstractDataFrame(ILazyDataset stack, int dtype) {
		super(stack, dtype);
	}

	public AbstractDataFrame(String name, int dtype, ILazyDataset... columns) throws DatasetException {
		super(name, dtype, columns);
	}

	public AbstractDataFrame(String name, int dtype, List<? extends ILazyDataset> columns) throws DatasetException {
		super(name, dtype, columns);
	}

	public AbstractDataFrame(String name) {
		super(name);
	}

	public boolean containsKey(Object key) {
		return aux.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return aux.containsValue(value);
	}
	
	/**
	 * Get the column of this name.
	 * @param name of column
	 * @return column
	 * @throws DatasetException - If cannot get slice for column
	 * @throws NullPointerException if name is not known.
	 */
	public ILazyDataset get(String name) throws DatasetException {
		if (aux.containsKey(name)) return aux.get(name);
		return super.get(name, false);
	}

	public ILazyDataset put(String key, ILazyDataset value) {
		return aux.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends ILazyDataset> m) {
		aux.putAll(m);
	}

	public void clear() {
		aux.clear();
		super.clear();
	}

	public Collection<String> keySet() {
		return aux.keySet();
	}

	/**
	 * Data not in the frame but still saved to HDF5
	 * @return
	 */
	public Map<String, ILazyDataset> getAuxData() {
		return aux;
	}
}
