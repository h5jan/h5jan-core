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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

/**
 * A class to hold common dataset loading configurations.
 * 
 * @author Matthew Gerring
 *
 */
public interface Configuration extends Map<String, Object> {

	/**
	 * No conf
	 */
	public static Configuration createDefault() {
		return new ConfigurationImpl();
	}

	public static Configuration createEmpty() {
		return new ConfigurationImpl();
	}
	
	/**
	 * Grey scale conf.
	 */
	public static Configuration createGreyScale() { 
		return concrete("asGrey", Boolean.TRUE);
	}

	/**
	 * Keep pixel width
	 */
	public static Configuration createKeepWidth() {
	    return concrete("keepBitWidth", Boolean.TRUE);
	}

	/**
	 * Create any configuration with key value pairs
	 * @param values
	 * @return
	 */
	public static Configuration concrete(Object... values) {
		return new ConfigurationImpl(values);
	}
	
	/**
	 * Align from name and type
	 * @param fileName - file name
	 * @param fileType - file type
	 * @param numBits - number of bits
	 * @param asUnsigned - signed or unsigned.
	 * @return configuration
	 */
	public static Configuration image(String fileName, String fileType, int numBits, boolean asUnsigned) {
		return new ConfigurationImpl("fileName", fileName,
									"fileType", fileType,
									"numBits", numBits,
									"asUnsigned", asUnsigned);
	}
	
	/**
	 * Align from file.
	 * @param file - file 
	 * @param numBits - number of bits
	 * @param asUnsigned - signed or unsigned.
	 * @return configuration
	 */
	public static Configuration image(File file, int numBits, boolean asUnsigned) {
		Configuration ret = new ConfigurationImpl("numBits", numBits,
									"asUnsigned", asUnsigned);
		ret.align(file);
		return ret;
	}

	public default String setFileName(String fileName) {
		return (String)put("fileName", fileName);
	}
	
	public default String setFilePath(String filePath) {
		return (String)put("filePath", filePath);
	}

	public default String setFileType(String fileType) {
		return (String)put("fileType", fileType);
	}

	public default String setFilterName(String filterName) {
		return (String)put("filterName", filterName);
	}

	public default int getNumBits(Integer numBits) {
		return (Integer)put("numBits", numBits);
	}

	public default boolean setAsUnsigned(boolean asUnsigned) {
		return (Boolean)put("asUnsigned", asUnsigned);
	}
	
	public default String getFileName() {
		return (String)get("fileName");
	}
	
	public default String getFilePath() {
		return (String)get("filePath");
	}

	public default String getFileType() {
		return (String)get("fileType");
	}

	public default String getFilterName() {
		return (String)get("filterName");
	}

	public default int getNumBits() {
		return (Integer)get("numBits");
	}

	public default boolean getAsUnsigned() {
		return (Boolean)get("asUnsigned");
	}
	
	public default void align(File file) {
		align(file, false);
	}
	
	public default void align(File file, boolean force) {
		if (force || !containsKey("fileName")) put("fileName", file.getName());
		if (force || !containsKey("filePath")) put("filePath", file.getAbsolutePath());
		if (force || !containsKey("fileType")) {
			String ext = FilenameUtils.getExtension(file.getName());
			if (ext!=null) {
				 put("fileType", ext.toUpperCase());
			}
		}
	}
	
	public default void align(String entryName) {
		put("fileName", entryName);
		String ext = FilenameUtils.getExtension(entryName);
		if (ext!=null) {
			put("fileType", ext.toUpperCase());
		}
	}

	public Configuration clone();
}

class ConfigurationImpl extends HashMap<String,Object> implements Configuration {
	/**
	 * 
	 */
	private static final long serialVersionUID = -377185509220648108L;

	ConfigurationImpl(Object... values) {
		super();
		if (values!=null) {
			if (values.length%2!=0) throw new IllegalArgumentException("There must be an even number of arguments!");
			for (int i = 0; i < values.length; i+=2) {
				put(values[i].toString(), values[i+1]);
			}
		}
	}
	
	public Configuration clone() {
		Configuration clone = new ConfigurationImpl();
		clone.putAll(this);
		return clone;
	}
}
