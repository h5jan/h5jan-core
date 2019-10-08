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
package org.eclipse.january.h5jan;

import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.ILazyWriteableDataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
	
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Attributes to tag data
	 */
	static void setReferenceAttributes(NxsFile nfile, String h5Path, String name) throws NexusException {
		nfile.addAttribute("/", new AttributeImpl(Constants.PATH, h5Path));
		nfile.addAttribute("/", new AttributeImpl(Constants.DATA_PATH, h5Path+"/"+name));
	}

	/**
	 * Column names and metadata written
	 * @param hFile
	 * @param h5Path
	 * @param names
	 * @throws NexusException
	 * @throws JsonProcessingException 
	 */
	public static void setMetaAttributues(NxsFile hFile, String h5Path, DataFrame data) throws NexusException, JsonProcessingException {
		
		
		if (data.getName() == null) {
			throw new IllegalArgumentException("The data frame must have a name!");
		}
		if (data.getColumnNames() == null) {
			throw new IllegalArgumentException("The columns must be named!");
		}

		hFile.addAttribute(h5Path, new AttributeImpl(Constants.NAME, data.getName()));
		hFile.addAttribute(h5Path, new AttributeImpl(Constants.COL_NAMES, data.getColumnNames()));
		if (data!=null) {
			// Difficult to store all metadata because ILazyDataset only provides
			// access by type and we do not know the type at the point of serialization to hdf5.
			// So we just save the first one of any type. This means only one entry of
			// metadata is supported per data frame.
			NxsMetadata meta = data.getMetadata();
			
			if (meta!=null) {
				if (meta instanceof GenericMetadata) {
					JsonNode node = ((GenericMetadata)meta).getNode();
					hFile.addAttribute(h5Path, new AttributeImpl(Constants.META, mapper.writeValueAsString(node)));
					hFile.addAttribute(h5Path, new AttributeImpl(Constants.META_TYPE, meta.getClass().getName()));
				} else {
					// The metadata set must serialize to json or this call will fail.
					hFile.addAttribute(h5Path, new AttributeImpl(Constants.META_TYPE, meta.getClass().getName()));
					hFile.addAttribute(h5Path, new AttributeImpl(Constants.META, mapper.writeValueAsString(meta)));
				}
			}
		}
	}

}
