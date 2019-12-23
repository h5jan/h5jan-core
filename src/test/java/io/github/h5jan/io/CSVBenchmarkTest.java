package io.github.h5jan.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.january.IMonitor;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class CSVBenchmarkTest extends AbstractReaderTest {

	
	@Test
	public void one() throws Exception {
		
		Path csvPath = JPaths.getTestResource("csv/j21.csv");
		long csvSize = Files.size(csvPath);
		
		DataFrame ds = reader.read(csvPath, Configuration.createEmpty(), new IMonitor.Stub());
		String filePath = "test-scratch/csv/j21.h5";
		
		ds.to_hdf(filePath, "/entry1/data");
		Path h5Path = Paths.get(filePath);
		long h5Size = Files.size(h5Path);
		
		System.out.println("CSV size "+csvSize);
		System.out.println("H5 size no compression "+h5Size);
	}
	
	// Write with metadata
}
