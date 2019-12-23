/*-
 * 
 * Copyright 2019 Halliburton Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.h5jan.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;

import io.github.h5jan.core.DataFrame;

/**
 * A zip reader reads the well logs from a zip stream of
 * any known data types which the IWellReaderFactory can deal with.
 * 
 * These might be for instance a Zip file of LAS files or it might 
 * be a Zip stream of json files.
 * 
 * Different zip extractors are available controlled via a system
 * property:
 * <code>com.halliburton.lgc.gemasis.awc.io.zipExtractType</code>
 * <p>
 * It's value may be "memory" or "file". The default is to read the
 * zip files from memory but optionally a temporary file may be used.
 * 
 * @author Matthew Gerring
 *
 */
abstract class InflaterLoader<T extends InflaterInputStream> extends AbstractStreamLoader implements IStreamLoader {
	
	private DataFrameReader    factory;
	private Map<String, Extractor> extractors;

	InflaterLoader(DataFrameReader factory) {
		this.factory = factory;
		this.extractors = createExtractors();
	}
	
	/**
	 * 
	 * @return the class usually ZipInputStream or GZipInputStream.
	 */
	abstract Class<T> getInflaterClass();
	
	/**
	 * Name of next entry. For ZipInputStream is the name from the entry.
	 * for GZipInputStream is just the uncompressed file name.
	 * @return name of next entry or null if there are no more entries.
	 */
	abstract String getNextEntry(T stream, Configuration conf) throws IOException;

	@Override
	public DataFrame load(InputStream stream, Configuration configuration, IMonitor mon) throws IOException, DatasetException {

		T zstream = null;
		
		try {
			zstream = getInflaterClass().isAssignableFrom(stream.getClass())
				  ? (T)stream
				  : getInflaterClass().getConstructor(InputStream.class).newInstance(stream);
		} catch (IllegalAccessException  | NoSuchMethodException
				| SecurityException | InvocationTargetException ne) {
			throw new IOException("Cannot make extractor!", ne);
		} catch (InstantiationException| IllegalArgumentException ob) {
			throw new IOException("Problem making zip input stream!", ob);
		}
      
        List<DataFrame> frames = new ArrayList<>();
        
        // The entries must be the file paths.
        String entryName= null;
        while((entryName = getNextEntry(zstream, configuration))!=null) {
        	
        	DataFrame read;
			try {
				Extractor extractor = extractors.get(System.getProperty("io.github.h5jan.io.zipExtractType", "file"));
				read = extractor.extract(entryName, zstream, configuration, mon);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IOException(e);
			}
        	frames.add(read);
        }

        if (frames.size()==1) {
        	return frames.get(0);
        } else {
        	// TODO Make composite Data frames!
        	throw new IllegalArgumentException("More than one data frame in a compressed file is not supported.");
        }
	}
	
	private DataFrame fileExtract(String entryName, 
									InflaterInputStream zstream, 
									Configuration conf, 
									IMonitor mon) throws IOException, DatasetException, InstantiationException, IllegalAccessException {
		
        byte[] buffer = new byte[4096];
        
    	File tmp = File.createTempFile(FilenameUtils.getBaseName(entryName), "."+FilenameUtils.getExtension(entryName));
    	tmp.deleteOnExit();
    	
    	try(FileOutputStream out = new FileOutputStream(tmp)) {
        	int len = 0;
        	while ((len = zstream.read(buffer)) > 0) {
        		out.write(buffer, 0, len);
            }
        	out.close();
        	
        	conf = conf.clone();
        	conf.align(tmp, true);
        	
        	// Delegate reading of these wells down to existing readers.
        	return factory.load(new FileInputStream(tmp), conf, mon);
        	
    	} finally {
    		tmp.delete();
    	}		
	}
	
	private DataFrame memoryExtract(String entryName, 
										InflaterInputStream zstream, 
										Configuration conf, 
										IMonitor mon) throws IOException, DatasetException {
		
	     byte[] buffer = new byte[4096];
    	// We read the file into memory because after we load it, 
    	// that is where it will be anyway. Another option is to
    	// create a temp file
    	try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        	int len = 0;
        	while ((len = zstream.read(buffer)) > 0) {
        		out.write(buffer, 0, len);
            }
        	out.close();
        	
        	// Delegate reading of these wells down to existing readers.
        	try {
				return factory.load(new ByteArrayInputStream(out.toByteArray()), conf, mon);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IOException(e);
			} 
    	} 

	}
	
	@FunctionalInterface
	private interface Extractor {
		DataFrame extract(String entryName, 
							InflaterInputStream zstream, 
							Configuration conf, 
							IMonitor mon) throws IOException, DatasetException, InstantiationException, IllegalAccessException;
	}

	private Map<String, Extractor> createExtractors() {
		Map<String, Extractor> tmp = new HashMap<>();
		tmp.put("memory", (entry, zstream, conf, mon)->memoryExtract(entry, zstream, conf, mon));
		tmp.put("file",   (entry, zstream, conf, mon)->fileExtract(entry, zstream, conf, mon));
		return Collections.unmodifiableMap(tmp);
	}
	
}
