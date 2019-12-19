/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.github.h5jan.io;

import java.io.IOException;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;

/**
 * A class which can be extended when implementing IFileLoader
 */
abstract class AbstractStreamLoader implements IStreamLoader {

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


	protected abstract class LazyLoaderStub implements ILazyLoader {
		
		public static final long serialVersionUID = 5057544213374303912L;
		private IStreamLoader loader;
		private String name;

		public LazyLoaderStub() {
			loader = null;
			name = null;
		}

		public LazyLoaderStub(IStreamLoader loader) {
			this(loader, null);
		}

		/**
		 * @param loader
		 * @param name dataset name in data holder (can be null to signify first dataset)
		 */
		public LazyLoaderStub(IStreamLoader loader, String name) {
			this.loader = loader;
			this.name = name;
		}

		public IStreamLoader getLoader() {
			return loader;
		}

		public abstract IDataset getDataset(IMonitor mon, SliceND slice) throws IOException;
		
		@Override
		public boolean isFileReadable() {
			return true;
		}
	}

	protected LazyDataset createLazyDataset(LazyLoaderStub l, String dName, Class<? extends Dataset> clazz, int... shape) {
		return new LazyDataset(l, dName, clazz, shape);
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
