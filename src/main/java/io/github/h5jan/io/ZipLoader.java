package io.github.h5jan.io;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipLoader extends InflaterLoader<ZipInputStream> {

	public ZipLoader(DataFrameReader factory) {
		super(factory);
	}

	@Override
	Class<ZipInputStream> getInflaterClass() {
		return ZipInputStream.class;
	}

	@Override
	String getNextEntry(ZipInputStream stream, Configuration conf) throws IOException {
		ZipEntry entry = stream.getNextEntry();
    	while (entry!=null && entry.isDirectory()) {
    		entry = stream.getNextEntry();
    	}
    	String entryName = entry!=null ? entry.getName() : null;
    	if (entryName!=null) conf.align(entryName);
		return entryName;
	}

}
