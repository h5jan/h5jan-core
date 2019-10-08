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
package io.github.gerring.h5jan;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Public type to hold undefined metadata.
 * 
 * @author Matthew Gerring
 *
 */
public class GenericMetadata implements NxsMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1567759675418107270L;
	private JsonNode node;
	
	public GenericMetadata() {
	}

	public GenericMetadata(JsonNode node) {
		this.node = node;
	}

	@Override
	public GenericMetadata clone() {
		return new GenericMetadata(node);
	}

	public JsonNode getNode() {
		return node;
	}

	public void setNode(JsonNode node) {
		this.node = node;
	}

}