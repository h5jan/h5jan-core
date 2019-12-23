package io.github.h5jan.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

public class H5Loader extends AbstractStreamLoader {

	@Override
	public DataFrame load(InputStream stream, Configuration conf, IMonitor mon) throws IOException, DatasetException {

		String filePath = conf.getFilePath();
		if (filePath==null) throw new IOException(getClass().getSimpleName()+" requires a file path to work!");
		File file = new File(filePath);
		if (!file.exists()) throw new IOException(getClass().getSimpleName()+" requires an existing file to work!");
		
		DataFrame frame = new DataFrame();
		try {
			frame.read_hdf(filePath);
		} catch (NexusException e) {
			throw new DatasetException("Problem reading nexus!", e);
		}
		return frame;
	}

	
}
