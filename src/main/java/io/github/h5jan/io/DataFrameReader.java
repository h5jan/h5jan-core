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
package io.github.h5jan.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * Used to read formats other than HDF5
 *
 * A class to read data from file format as a dataset.
 * The class attempts to figure out a load for a given format and use that.
 * @see https://github.com/DawnScience/scisoft-core/blob/master/uk.ac.diamond.scisoft.analysis/src/uk/ac/diamond/scisoft/analysis/io/LoaderFactory.java
 */
public class DataFrameReader {
		
	// TODO LoaderFactory has more than one loader for an extension.
	// We do not currently need this so it is not available.
	private final static Map<String, Class<? extends IFileLoader>> loaders;
	static {
		loaders = Collections.synchronizedMap(new HashMap<String,Class<? extends IFileLoader>>());
		loaders.put("tif",		TIFFImageLoader.class);
		loaders.put("tiff",		TIFFImageLoader.class);
		loaders.put("png",		JavaImageLoader.class);
		loaders.put("gif",		JavaImageLoader.class);
		loaders.put("jpg",		JavaImageLoader.class);
		loaders.put("jpeg",		JavaImageLoader.class);
	}
	
	/**
	 * Read a file in to a DataFrame
	 * @param path (file or directory) to read.
	 * @param configuration - configuration or null or empty
	 * @param monitor - monitor progress.
	 * @return DataFrame read from file.
	 * @throws IOExecption - If file read error.
	 * @throws DatasetException - If DataFrame construction fails
	 * @throws IllegalAccessException - If cannot make registered loader
	 * @throws InstantiationException - If cannot make registered loader
	 */
	public DataFrame read(Path path, Configuration configuration, IMonitor monitor) throws IOException, DatasetException, InstantiationException, IllegalAccessException {
		
		return read(path.toFile(), configuration, monitor);
	}

	/**
	 * Read a file in to a DataFrame
	 * @param path (file or directory) to read.
	 * @param configuration - configuration or null or empty
	 * @param monitor - monitor progress.
	 * @return DataFrame read from file.
	 * @throws IOExecption - If file read error.
	 * @throws DatasetException - If DataFrame construction fails
	 * @throws IllegalAccessException - If cannot make registered loader
	 * @throws InstantiationException - If cannot make registered loader
	 */
	public DataFrame read(File path, Configuration configuration, IMonitor monitor) throws IOException, DatasetException, InstantiationException, IllegalAccessException {
		
		if (path==null) throw new IOException("File is null.");
		if (!path.exists()) throw new IOException("File "+path.getName()+" does not exist.");
		
		if (path.isFile()) {
			return file(path, configuration, monitor);
		} else if (path.isDirectory()) {
			return dir(path, configuration, monitor);
		} else {
			throw new IOException("Only files and directories can be read.");
		}
	}

	private DataFrame dir(File dir, Configuration configuration, IMonitor monitor) throws InstantiationException, IllegalAccessException, IOException, DatasetException {
		
		DataFrame frame = new DataFrame(dir.getName());
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!hasLoader(file)) continue;
			DataFrame image = file(file, configuration, monitor);
			frame.setDtype(image.getDtype());
			image.forEach(i->{ // Normally just one
				String name = String.format(AbstractFileLoader.IMAGE_NAME_FORMAT, frame.size());
				i.setName(name);
				frame.add(i);
				monitor.worked(1);
			}); 
		}
		return frame;
	}

	private boolean hasLoader(File file) {
		if (file.isDirectory()) return false;
		String ext = FilenameUtils.getExtension(file.getName());
		if (ext==null) return false;
		return loaders.containsKey(ext);
	}

	private DataFrame file(File file, Configuration configuration, IMonitor monitor) throws IOException, InstantiationException, IllegalAccessException {
		monitor.subTask("Load "+file.getName());
		IFileLoader loader = getLoader(file);
		return loader.load(file, configuration, monitor);
	}

	private IFileLoader getLoader(File file) throws IOException, InstantiationException, IllegalAccessException {
		String ext = FilenameUtils.getExtension(file.getName());
		if (ext==null) throw new IOException("File "+file.getName()+" does not have an extension.");
		
		ext = ext.toLowerCase(); // TODO Tests with capitalized extensions.
		Class<? extends IFileLoader> loaderClass = loaders.get(ext);
		if (loaderClass==null) throw new InstantiationException("No load for extension "+ext+".");
		
		return loaderClass.newInstance();
	}
}