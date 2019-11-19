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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Random;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;

public class DataFrameTest {

	@Test
	public void noColumns() {
		new DataFrame(Random.rand(256, 3), Dataset.FLOAT64);
	}
	
	@Test
	public void checkShape() throws Exception {
		IDataset someData = Random.rand(256, 3);
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		assertArrayEquals(new int[] {256, 3}, frame.getShape());
	}
	
	@Test(expected=NullPointerException.class)
	public void nullDataset() {
		new DataFrame(null, Dataset.FLOAT64);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void zeroRankDataset() {
		IDataset string = DatasetFactory.createFromObject("Hello World");
		assertEquals(0, string.getRank());
		new DataFrame(string, Dataset.FLOAT64);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void oneRankDataset() {
		IDataset data = DatasetFactory.createFromObject(Arrays.asList(1,2,3));
		assertEquals(1, data.getRank());
		new DataFrame(data, Dataset.FLOAT64);
	}

	@Test(expected=IllegalArgumentException.class)
	public void wrongColumns() {
		new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a","b"), Dataset.FLOAT64);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongColumns() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.setColumnNames(Arrays.asList("a","b"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank1() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.setData(Random.rand(256, 2));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank2() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.setData(Random.rand(256));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDataRank3() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.setData(DatasetFactory.createFromObject(1));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setWrongDtype() {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.setDtype(-345467);
	}

	@Test
	public void checkGetSize1D() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		ILazyDataset a = frame.get(0);
		assertNotNull(a);
		assertEquals(256, a.getSize());
		assertArrayEquals(new int[] {256}, a.getShape());
		
		ILazyDataset b = frame.get("b");
		assertNotNull(b);
		assertEquals(256, b.getSize());
		assertArrayEquals(new int[] {256}, b.getShape());

		ILazyDataset bi = frame.get(1);
		assertEquals(b, bi);
	}
	
	@Test
	public void checkGetSize2D() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 100, 2), 1, Arrays.asList("fred", "bill"), Dataset.FLOAT64);
		ILazyDataset fred = frame.get(0);
		assertNotNull(fred);
		assertEquals(25600, fred.getSize());
		assertArrayEquals(new int[] {256, 100}, fred.getShape());
		
		ILazyDataset bill = frame.get("bill");
		assertNotNull(bill);
		assertEquals(25600, bill.getSize());
		assertArrayEquals(new int[] {256, 100}, bill.getShape());

		ILazyDataset billi = frame.get(1);
		assertEquals(bill, billi);
	}
	
	@Test
	public void checkGetSize3D() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 100, 10, 2), 1, Arrays.asList("fred", "bill"), Dataset.FLOAT64);
		ILazyDataset fred = frame.get(0);
		assertNotNull(fred);
		assertEquals(256000, fred.getSize());
		assertArrayEquals(new int[] {256, 100, 10}, fred.getShape());
		
		ILazyDataset bill = frame.get("bill");
		assertNotNull(bill);
		assertEquals(256000, bill.getSize());
		assertArrayEquals(new int[] {256, 100, 10}, bill.getShape());

		ILazyDataset billi = frame.get(1);
		assertEquals(bill, billi);
	}
	
	@Test(expected=DatasetException.class)
	public void badGet1() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.get("fred");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badGet2() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.get(-1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badGet3() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.get(3);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badGet4() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.get(4);
	}
	
	@Test
	public void getAutoName() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), Dataset.FLOAT64);
		assertNotNull(frame.get(0));
		assertNotNull(frame.get("column_0"));
		assertNotNull(frame.get(2));
		assertNotNull(frame.get("column_2"));
	}
	
	@Test(expected=DatasetException.class)
	public void badAutoName() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), Dataset.FLOAT64);
		assertNotNull(frame.get("column_3"));
	}

	@Test
	public void createAndFill1() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(rand("first", 256));
		assertArrayEquals(new int[] {256, 1}, frame.getShape());
	}

	@Test
	public void createAndFill3() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(rand("a", 256));
		frame.add(rand("b", 256));
		frame.add(rand("c", 256));
		assertArrayEquals(new int[] {256, 3}, frame.getShape());
		assertEquals(Arrays.asList("a", "b", "c"), frame.getColumnNames());
	}
	
	@Test
	public void createAndFill2D3() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(rand("p", 256, 100));
		frame.add(rand("q", 256, 100));
		frame.add(rand("r", 256, 100));
		assertArrayEquals(new int[] {256, 100, 3}, frame.getShape());
		assertEquals(Arrays.asList("p", "q", "r"), frame.getColumnNames());
	}

	@Test
	public void createAndFill3D3() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(rand("fred", 256, 100, 10));
		frame.add(rand("sue", 256, 100, 10));
		assertArrayEquals(new int[] {256, 100, 10, 2}, frame.getShape());
		assertEquals(Arrays.asList("fred", "sue"), frame.getColumnNames());
	}
	
	@Test
	public void addNamedDataAlready() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT64);
		frame.add(rand("fred", 256));
		assertArrayEquals(new int[] {256, 4}, frame.getShape());
		assertEquals(Arrays.asList("a", "b", "c", "fred"), frame.getColumnNames());
	}

	@Test
	public void addUnnamedDataAlready() throws Exception {
		DataFrame frame = new DataFrame(Random.rand(256, 3), Dataset.FLOAT64);
		frame.add(rand("fred", 256));
		assertArrayEquals(new int[] {256, 4}, frame.getShape());
		assertEquals(Arrays.asList("column_0", "column_1", "column_2", "fred"), frame.getColumnNames());
	}

	@Test(expected=NullPointerException.class)
	public void createNull() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void createNoName() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(Random.rand(10));
	}

	@Test(expected=IllegalArgumentException.class)
	public void createDataAlready() throws Exception {
		DataFrame frame = new DataFrame();
		frame.add(Random.rand(10));
	}

	private Dataset rand(String name, int... shape) {
		Dataset rand = Random.rand(-100, 100, shape);
		rand.setName(name);
		return rand;
	}

}
