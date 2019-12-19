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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * Used to write formats other than HDF5
 * 
 * A class to read data from file format as a dataset.
 * The class attempts to figure out a load for a given format and use that.
 * @see https://github.com/DawnScience/scisoft-core/blob/master/uk.ac.diamond.scisoft.analysis/src/uk/ac/diamond/scisoft/analysis/io/LoaderFactory.java
 */
public class DataFrameWriter {
		
	// TODO LoaderFactory has more than one loader for an extension.
	// We do not currently need this so it is not available.
	private final static Map<String, Class<? extends IStreamSaver>> writers;
	static {
		writers = Collections.synchronizedMap(new HashMap<String,Class<? extends IStreamSaver>>());
		writers.put("tif",		TIFFImageSaver.class);
		writers.put("tiff",		TIFFImageSaver.class);
		writers.put("png",		JavaImageSaver.class);
		writers.put("gif",		JavaImageSaver.class);
		writers.put("jpg",		JavaImageSaver.class);
		writers.put("jpeg",		JavaImageSaver.class);
	}
	
	/**
	 * Read a file in to a DataFrame
	 * @param path directory to save info.
	 * @param configuration - configuration or null or empty
	 * @param data - data frame
	 * @param monitor - monitor progress.
	 * @return true if saved, false otherwise
	 * @throws IOExecption - If file read error.
	 * @throws DatasetException - If DataFrame construction fails
	 * @throws IllegalAccessException - If cannot make registered loader
	 * @throws InstantiationException - If cannot make registered loader
	 */
	public boolean save(Path path, Configuration configuration, DataFrame data, IMonitor monitor) throws IOException, DatasetException, InstantiationException, IllegalAccessException {
		return save(path.toFile(), configuration, data, monitor);
	}

	/**
	 * Read a file in to a DataFrame
	 * @param path directory to save info.
	 * @param configuration - configuration or null or empty
	 * @param monitor - monitor progress.
	 * @return DataFrame read from file.
	 * @throws IOExecption - If file read error.
	 * @throws DatasetException - If DataFrame construction fails
	 * @throws IllegalAccessException - If cannot make registered loader
	 * @throws InstantiationException - If cannot make registered loader
	 */
	public boolean save(File path, Configuration configuration, DataFrame data, IMonitor monitor) throws IOException, DatasetException, InstantiationException, IllegalAccessException {
		
		if (path==null) throw new IOException("File is null.");
		if (!path.exists()) throw new IOException("File "+path.getName()+" does not exist.");
		
		if (path.isDirectory()) {
			return saveToDirectory(path, configuration, data, monitor);
		} else {
			throw new IOException("Only directories can be written to!");
		}
	}

	private boolean saveToDirectory(File dir, Configuration configuration, DataFrame data, IMonitor monitor) throws InstantiationException, IllegalAccessException, IOException, DatasetException {
		
		String fileType = configuration.getFileType();
		if (fileType==null) throw new IllegalArgumentException("Please provide the file type to save in the configuration!");
		if (!hasSaver(fileType)) throw new IllegalArgumentException("No saver for type '"+fileType+"'!");
		
		IStreamSaver saver = getSaver(fileType);
		File out = new File(dir, configuration.getFileName());
		return saver.save(new FileOutputStream(out), configuration, data, monitor);
	}

	private boolean hasSaver(String fileType) {
		return writers.containsKey(fileType.toLowerCase());
	}

	private IStreamSaver getSaver(String fileType) throws IOException, InstantiationException, IllegalAccessException {
		if (fileType==null) throw new IOException("No file type provided!");
		fileType = fileType.toLowerCase();
		Class<? extends IStreamSaver> saverClass = writers.get(fileType);
		if (saverClass==null) throw new InstantiationException("No saver for '"+fileType+"'.");
		
		return saverClass.newInstance();
	}
}
