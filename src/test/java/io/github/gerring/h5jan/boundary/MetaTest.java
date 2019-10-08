package io.github.gerring.h5jan.boundary;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.metadata.MetadataType;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;

import io.github.gerring.h5jan.AbstractH5JanTest;
import io.github.gerring.h5jan.DataFrame;
import io.github.gerring.h5jan.GenericMetadata;
import io.github.gerring.h5jan.JPaths;
import io.github.gerring.h5jan.NxsMetadata;
import io.github.gerring.h5jan.WellMetadata;

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
public class MetaTest extends AbstractH5JanTest {

	private DataFrame frame;

	@Before
	public void createLegalDataFrame() {
		
		// We create a place to put our data
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		this.frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
	}

	@Test
	public void none() throws Exception {
		checkMeta(frame, readWriteTmp(frame));
		checkMeta(frame, readWriteTmpLazy(frame));
	}
	
	@Test
	public void wells() throws Exception {
		WellMetadata meta = createWellMetadata();
		frame.setMetadata(meta);
		checkMeta(frame, readWriteTmp(frame));
		checkMeta(frame, readWriteTmpLazy(frame));
	}
	
	@Test
	public void genericEmpty() throws Exception {
		
		GenericMetadata meta = new GenericMetadata();
		frame.setMetadata(meta);
		checkMeta(frame, readWriteTmp(frame));
		checkMeta(frame, readWriteTmpLazy(frame));
	}

	@Test
	public void genericJson() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(JPaths.getTestResource("alias.json").toFile());
		GenericMetadata meta = new GenericMetadata(node);
		frame.setMetadata(meta);
		checkMeta(frame, readWriteTmp(frame));
		checkMeta(frame, readWriteTmpLazy(frame));
	}
	
	@Test(expected=InvalidDefinitionException.class)
	public void anonymous() throws Exception {
		frame.setMetadata(new NxsMetadata() {
			public NxsMetadata clone() {
				return null;
			}
		});
		checkMeta(frame, readWriteTmp(frame));
		checkMeta(frame, readWriteTmpLazy(frame));
	}

	private void checkMeta(DataFrame frm1, DataFrame frm2) {
		assertEquals(frm1.getMetadata(), frm2.getMetadata());
	}

}
