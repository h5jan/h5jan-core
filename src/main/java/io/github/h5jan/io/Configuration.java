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

import java.util.HashMap;
import java.util.Map;

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
	public static final Configuration DEFAULT = new ConfigurationImpl();
	
	/**
	 * Grey scale conf.
	 */
	public static final Configuration GREYSCALE = concrete("asGrey", Boolean.TRUE);

	/**
	 * Keep pixel width
	 */
	public static final Configuration KEEPWIDTH = concrete("keepBitWidth", Boolean.TRUE);

	/**
	 * Create any configuration with key value pairs
	 * @param values
	 * @return
	 */
	public static Configuration concrete(Object... values) {
		return new ConfigurationImpl(values);
	}
	
	public static Configuration image(String fileName, String fileType, int numBits, boolean asUnsigned) {
		return new ConfigurationImpl("fileName", fileName,
									"fileType", fileType,
									"numBits", numBits,
									"asUnsigned", asUnsigned);
	}

	public default String getFileName() {
		return (String)get("fileName");
	}

	public default String getFileType() {
		return (String)get("fileType");
	}

	public default int getNumBits() {
		return (Integer)get("numBits");
	}

	public default boolean getAsUnsigned() {
		return (Boolean)get("asUnsigned");
	}
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
}
