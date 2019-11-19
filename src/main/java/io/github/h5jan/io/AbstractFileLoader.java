/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.github.h5jan.io;

import java.io.File;
import java.io.IOException;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;

import io.github.h5jan.core.DataFrame;

/**
 * A class which can be extended when implementing IFileLoader
 */
abstract class AbstractFileLoader implements IFileLoader {

	/** 
	 * Name prefix for an image dataset (should be followed by two digits, starting with 01)
	 */
	protected static final String IMAGE_NAME_PREFIX = "image-";

	/** 
	 * Name format for an image dataset
	 */
	protected static final String IMAGE_NAME_FORMAT = IMAGE_NAME_PREFIX + "%02d";

	/**
	 * Default name for first image dataset
	 */
	protected static final String DEF_IMAGE_NAME = IMAGE_NAME_PREFIX + "01";

	/**
	 * Name for image stack
	 */
	protected static final String STACK_NAME = "image-stack";

	/**
	 * String to separate full file path or file name from dataset name
	 */
	protected static final String FILEPATH_DATASET_SEPARATOR = ":";

	protected File file = null;

	protected void setFile(final File file) {
		this.file = file;
	}
	
	protected Configuration configuration = null;

	protected void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	protected class LazyLoaderStub implements ILazyLoader {
		
		public static final long serialVersionUID = 5057544213374303912L;
		private IFileLoader loader;
		private String name;

		public LazyLoaderStub() {
			loader = null;
			name = null;
		}

		public LazyLoaderStub(IFileLoader loader) {
			this(loader, null);
		}

		/**
		 * @param loader
		 * @param name dataset name in data holder (can be null to signify first dataset)
		 */
		public LazyLoaderStub(IFileLoader loader, String name) {
			this.loader = loader;
			this.name = name;
		}

		public IFileLoader getLoader() {
			return loader;
		}

		@Override
		public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
			
			if (loader == null) {
				return null;
			}
			
			try {
				DataFrame holder = load(file, configuration, mon);
				if (holder != null) {
					ILazyDataset lazy = name == null ? holder.get(0) : holder.get(name);
					if (lazy != null) {
						IDataset data = lazy.getSlice();
						return DatasetUtils.convertToDataset(data).getSliceView(slice);
					}
				} 
	
				holder = loader.load(file, configuration, mon);
				ILazyDataset lazy = name == null ? holder.get(0) : holder.get(name);
				return lazy.getSlice().getSliceView(slice);
				
			} catch (DatasetException de) {
				throw new IOException(de);
			}
		}

		@Override
		public boolean isFileReadable() {
			return file.canRead();
		}
	}

	protected LazyDataset createLazyDataset(LazyLoaderStub l, String dName, Class<? extends Dataset> clazz, int... shape) {
		return new LazyDataset(l, dName, clazz, shape);
	}

	protected LazyDataset createLazyDataset(IFileLoader loader, String dName, Class<? extends Dataset> clazz, int... shape) {
		return createLazyDataset(loader, dName, dName, clazz, shape);
	}

	protected LazyDataset createLazyDataset(IFileLoader loader, String dName, String dhName, Class<? extends Dataset> clazz, int... shape) {
		return new LazyDataset(new LazyLoaderStub(loader, dhName), dName, clazz, shape);
	}

	/**
	 * @param mon
	 * @return false if cancelled
	 */
	protected static boolean monitorIncrement(IMonitor mon) {
		if (mon != null) {
			mon.worked(1);
			if (mon.isCancelled()) return false;
		}
		return true;
	}
}
