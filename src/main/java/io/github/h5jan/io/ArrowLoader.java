package io.github.h5jan.io;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * Simple class for reading arrow files to DataFrames.
 * 
 * Currently only reading arrow files we have written is supported.
 * 
 * @author Matthew Gerring
 *
 */
public class ArrowLoader implements IStreamLoader<FileInputStream> {

	@Override
	public DataFrame load(FileInputStream stream, Configuration configuration, IMonitor mon) throws IOException, DatasetException {
		// TODO in Configuration save if we want metadata perhaps
		ArrowIO io = new ArrowIO();
		return io.read(stream);
	}

}
