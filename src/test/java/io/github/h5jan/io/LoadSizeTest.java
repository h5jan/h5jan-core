package io.github.h5jan.io;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.junit.Before;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class LoadSizeTest extends AbstractReaderTest {
	
	private Path dir;

	@Before
	public void mkdir() throws IOException {
		dir = Paths.get("test-scratch/sizeTest/");
		Files.createDirectories(dir);
	}

	@Test
	public void csvUncompressed() throws Exception {
		sizeTest(NexusFile.COMPRESSION_NONE, "csv/j21.csv", 3000000, dir.resolve("j21.h5"), 2500000);
	}
	
	@Test
	public void csvCompressed() throws Exception {
		sizeTest(NexusFile.COMPRESSION_LZW_L1, "csv/j21.csv", 3000000, dir.resolve("j21.h5"), 2500000);
	}

	@Test
	public void images() throws Exception {
		
		// Get dir
		Path microscope = JPaths.getTestResource("microscope");
		long dirSize = FileUtils.sizeOfDirectory(dir.toFile());
		
		// Write Data Frame for dir
		Path filePath = dir.resolve("scope_image.h5");
		writeImageStack(microscope, filePath.toString());
		
		long h5Size = Files.size(filePath);
		
		System.out.println("Dir size "+dirSize);
		System.out.println("H5 size compression "+h5Size);
		assertTrue(dirSize>h5Size);
	}

	private void sizeTest(int compression, String filePath, long scsv, Path writePath, long sh5) throws Exception {
		
		// Read and check size of source
		Path pcsv = JPaths.getTestResource(filePath);
		
		try {
			DataFrame csv = reader.read(pcsv, Configuration.createEmpty(), new IMonitor.Stub());
			assertTrue(scsv>Files.size(pcsv));
			
			// We write it, not sized
			csv.to_hdf(writePath.toString(), "/entry1/data", compression);
			
			// We get size of output file.
			assertTrue(sh5>Files.size(writePath));
		} finally {
			printSize("CSV size is ", pcsv);
			printSize("H5 size is ", writePath);
		}
	}
	
	private void printSize(String msg, Path path) throws IOException {
		if (Files.exists(path)) {
			System.out.println(msg+Math.round(Files.size(path)/1012)+"Kb");
		} else {
			System.out.println(msg+" DOES NOT EXIST");
		}
	}

}
