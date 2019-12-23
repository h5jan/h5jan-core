package io.github.h5jan.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.core.Appender;
import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class H5SizeTest extends AbstractReaderTest {
	
	@Test
	public void csvUncompressed() throws Exception {
		
		Path csvPath = JPaths.getTestResource("csv/j21.csv");
		long csvSize = Files.size(csvPath);
		
		DataFrame ds = reader.read(csvPath, Configuration.createEmpty(), new IMonitor.Stub());
		String filePath = "test-scratch/sizeTest/j21.h5";
		
		ds.to_hdf(filePath, "/entry1/data");
		Path h5Path = Paths.get(filePath);
		long h5Size = Files.size(h5Path);
		
		System.out.println("CSV size "+csvSize);
		System.out.println("H5 size no compression "+h5Size);
		assertTrue(csvSize>h5Size);
	}
	
	@Test
	public void csvCompressed() throws Exception {
		
		Path csvPath = JPaths.getTestResource("csv/j21.csv");
		long csvSize = Files.size(csvPath);
		
		DataFrame ds = reader.read(csvPath, Configuration.createEmpty(), new IMonitor.Stub());
		String filePath = "test-scratch/sizeTest/j21.h5";
		
		ds.to_hdf(filePath, "/entry1/data", NexusFile.COMPRESSION_LZW_L1);
		Path h5Path = Paths.get(filePath);
		long h5Size = Files.size(h5Path);
		
		System.out.println("CSV size "+csvSize);
		System.out.println("H5 size compression "+h5Size);
		assertTrue(csvSize>h5Size);
	}

	
	@Test
	public void images() throws Exception {
		
		// Get dir
		Path dir = JPaths.getTestResource("microscope");
		long dirSize = FileUtils.sizeOfDirectory(dir.toFile());
		
		// Write Data Frame for dir
		String filePath = "test-scratch/sizeTest/scope_image.h5";
		writeImageStack(dir, filePath);
		
		Path h5Path = Paths.get(filePath);
		long h5Size = Files.size(h5Path);
		
		System.out.println("Dir size "+dirSize);
		System.out.println("H5 size no compression "+h5Size);
		assertTrue(dirSize>h5Size);
	}

	private DataFrame writeImageStack(Path mainDir, String to) throws Exception {
		// Make a writing frame, the tiled image is this size.
		// Each tile is 96,128 so as we are making 3x3 stitching, we need 288,384
		DataFrame frame = new DataFrame("scope_image", Dataset.INT8, new int[] { 288,384 });
		
		// Save to HDF5, columns can be large, these are not it's a test
		try (Appender app = frame.open_hdf(to, "/entry1/myData")) {
			
			app.setCompression(NexusFile.COMPRESSION_LZW_L1); // Otherwise it will be too large.
			
			File[] dirs = mainDir.toFile().listFiles();
			
			for (int i = 0; i < dirs.length; i++) {
				
				// Directory of tiles
				File dir = dirs[i];

				// Read tiles, assuming their file name order is also their tile order.
				DataFrame tiles = reader.read(dir, Configuration.createGreyScale(), new IMonitor.Stub());
				
				// Stitch to make image based on a 3x3 matrix of tiles.
				Dataset image = tiles.stitch(new int[] {3,3});
				assertArrayEquals(new int[] {288,384}, frame.getColumnShape());
				image = DatasetUtils.cast(image, Dataset.INT8);
				
				// Add the image - note they are not all in memory
				// so this process should scale reasonably well.
				app.append("image_"+i, image);
			}
		}
		return frame;
	}

}
