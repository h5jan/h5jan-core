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
package io.github.h5jan.core.boundary;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.core.AbstractH5JanTest;
import io.github.h5jan.core.DataFrame;

/**
 * Pinning down the saving of some invalid states.
 * @author Matthew Gerring
 *
 */
public class SaveTest extends AbstractH5JanTest {

	private DataFrame frame;

	@Before
	public void createLegalDataFrame() {
		
		// We create a place to put our data
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		this.frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		frame.setMetadata(createWellMetadata());
	}
	
	@Test
	public void good() throws Exception {
		assertEquals(frame, readWriteLazy(frame, "good"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void badDtype() throws Exception {
		frame.setDtype(-1456456568);
		assertEquals(frame, readWriteLazy(frame, "badDtype"));
	}
	
	@Test(expected=NullPointerException.class)
	public void noName() throws Exception {
		frame.setName(null);
		assertEquals(frame, readWriteLazy(frame, "noName"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noColumns() throws Exception {
		frame.setColumnNames(null);
		assertEquals(frame, readWriteLazy(frame, "noColumns"));
	}
	
	@Test(expected=NullPointerException.class)
	public void noData() throws Exception {
		frame.setData(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void noFile1() throws Exception {
		frame.to_hdf(null, "/some/other/path");
	}

	@Test(expected=IllegalArgumentException.class)
	public void noFile2() throws Exception {
		frame.to_hdf("", "/some/other/path");
	}

	@Test(expected=IllegalArgumentException.class)
	public void noFile3() throws Exception {
		frame.to_hdf("           ", "/some/other/path");
	}
	
	@Test(expected=Throwable.class)
	public void noFile4() throws Exception {
		frame.to_hdf("\\||??", "/some/other/path");
	}
	
	@Test(expected=NullPointerException.class)
	public void noPath1() throws Exception {
		frame.to_hdf("test-scratch/temp/tmp.h5", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noPath2() throws Exception {
		frame.to_hdf("test-scratch/temp/tmp.h5", "");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noPath3() throws Exception {
		frame.to_hdf("test-scratch/temp/tmp.h5", "      ");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noPath4() throws Exception {
		frame.to_hdf("test-scratch/temp/tmp.h5", "|||%%&&-");
	}

}
