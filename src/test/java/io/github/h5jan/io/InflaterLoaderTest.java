package io.github.h5jan.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InflaterLoaderTest extends AbstractReaderTest {
	
	@Before
	public void classInit() throws Exception {
		reader.read(JPaths.getTestResource("zip/j21_csv.zip"), Configuration.createEmpty(), new IMonitor.Stub());
		reader.read(JPaths.getTestResource("zip/j21_h5.zip"), Configuration.createEmpty(), new IMonitor.Stub());
	}
	
	@Test
	public void zipCsv() throws Exception {
		round("zip/j21_csv.zip", 27, 1000);
	}
	
	@Test
	public void zipH5() throws Exception {
		round("zip/j21_h5.zip", 27, 500);
	}
	
	@Test
	public void gzipCsv() throws Exception {
		round("zip/j21.csv.gz", 27, 2000);
	}
	
	@Test
	public void gzipH5() throws Exception {
		round("zip/j21.h5.gz", 27, 1000);
	}

	private void round(String path, int dsCount, long maxTime) throws InstantiationException, IllegalAccessException, IOException, DatasetException {
		Path csvPath = JPaths.getTestResource(path);
		long before = System.currentTimeMillis();
		DataFrame ds = reader.read(csvPath, Configuration.createEmpty(), new IMonitor.Stub());
		long after = System.currentTimeMillis();
		assertEquals(dsCount, ds.size());
		long diff = after-before;
		System.out.println("Reading "+path+" took "+diff+"ms");
		assertTrue(maxTime>diff);
	}
	
	// Write with metadata
}
