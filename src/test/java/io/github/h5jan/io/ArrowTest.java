package io.github.h5jan.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.january.IMonitor;
import org.eclipse.january.IMonitor.Stub;
import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;


public class ArrowTest {
		
	protected DataFrameReader 		reader;
	protected DataFrameWriter 		writer;

	@Before
	public void before() throws Exception {
		this.reader 	= new DataFrameReader();
		this.writer 	= new DataFrameWriter();
	}
	
	
	@Test
	public void arrowPrediction() throws Exception {
		DataFrame csv = reader.read(JPaths.getTestResource("csv/131.csv"), Configuration.createEmpty(), new IMonitor.Stub());
		File farw = new File("test-scratch/arrow/lith.arw");
		farw.getParentFile().mkdirs();
		writer.save(farw, Configuration.arrow(), csv, new IMonitor.Stub());
		
		DataFrame arw = reader.read(farw, Configuration.createEmpty(), new IMonitor.Stub());

		IDataset depth1 = csv.get(0).getSlice();
		IDataset depth2 = arw.get(0).getSlice();
		assertEquals(depth1.getName(), depth2.getName());
		assertEquals(depth1, depth2);
	}

}
