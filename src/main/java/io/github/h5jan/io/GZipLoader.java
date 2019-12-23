package io.github.h5jan.io;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;

class GZipLoader extends InflaterLoader<GZIPInputStream> {

	public GZipLoader(DataFrameReader factory) {
		super(factory);
	}

	@Override
	Class<GZIPInputStream> getInflaterClass() {
		return GZIPInputStream.class;
	}
	
	// We only return one name from this loader.
	private boolean returnedOneName = false;

	@Override
	String getNextEntry(GZIPInputStream stream, Configuration conf) throws IOException {
		if (returnedOneName) return null;
		returnedOneName = true;
		return FilenameUtils.getBaseName(conf.getFileName());
	}

}
