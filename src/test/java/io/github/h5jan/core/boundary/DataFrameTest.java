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
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;

public class DataFrameTest {

	@Test
	public void noColumns() {
		new DataFrame(Random.rand(256, 3), Dataset.ARRAYFLOAT32);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullDataset() {
		new DataFrame(null, Dataset.ARRAYFLOAT32);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void zeroRankDataset() {
		IDataset string = DatasetFactory.createFromObject("Hello World");
		assertEquals(0, string.getRank());
		new DataFrame(string, Dataset.ARRAYFLOAT32);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void oneRankDataset() {
		IDataset data = DatasetFactory.createFromObject(Arrays.asList(1,2,3));
		assertEquals(1, data.getRank());
		new DataFrame(data, Dataset.ARRAYFLOAT32);
	}

	@Test(expected=IllegalArgumentException.class)
	public void wrongColumns() {
		new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b"), Dataset.ARRAYFLOAT32);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongColumns() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b", "c"), Dataset.ARRAYFLOAT32);
		frame.setColumnNames(Arrays.asList("a","b"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank1() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b", "c"), Dataset.ARRAYFLOAT32);
		frame.setData(Random.rand(256, 2));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank2() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b", "c"), Dataset.ARRAYFLOAT32);
		frame.setData(Random.rand(256));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank3() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b", "c"), Dataset.ARRAYFLOAT32);
		frame.setData(DatasetFactory.createFromObject(1));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDtype() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b", "c"), Dataset.ARRAYFLOAT32);
		frame.setDtype(-345467);
	}

}
