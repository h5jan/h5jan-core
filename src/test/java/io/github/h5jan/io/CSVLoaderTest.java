/*-
 *******************************************************************************
 * Copyright (c) 2019 Halliburton International, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package io.github.h5jan.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Test;

import io.github.h5jan.core.DataFrame;
import io.github.h5jan.core.JPaths;

public class CSVLoaderTest extends AbstractReaderTest {
	
	/**
	 * A convenience regex for finding depth.
	 * Some data frames hold depth with different cases and sometimes truncate it.
	 * For example 
	 */
	public static final String DEPTH_REGEX = "(?i)depth?";

	@Test
	public void singleFile() throws Exception {
		test("csv/sw0.csv", Configuration.createEmpty(), frame->{
			assertNotNull(frame);
			assertEquals(5,frame.size());
			ILazyDataset depth = frame.get("Depth");
			assertNotNull(depth);
		});
	}

	@Test(expected=DatasetException.class)
	public void noDepth() throws Exception {
		final Configuration conf = Configuration.createEmpty();
		conf.setFilterName("^(?!.*Depth).*$");
		test("csv/sw0.csv", conf, frame->{
			assertNotNull(frame);
			assertEquals(4,frame.size());
			
			// throws DatasetException
			frame.get("Depth");
		});
	}
	
	@Test(expected=DatasetException.class)
	public void shankles() throws Exception {
		
		final Configuration conf = Configuration.createEmpty();
		conf.setFilterName("^SHANKLE.*$");
		
		test("csv/sw0.csv", conf, frame->{
			assertNotNull(frame);
			assertEquals(4,frame.size());
			
			// throws DatasetException
			frame.get("Depth");
		});
	}
	
	@Test
	public void all() throws Exception {
		final Configuration conf = Configuration.createEmpty();
		test("csv", conf, frame-> {
			assertNotNull(frame);
			ILazyDataset depth = frame.find(DEPTH_REGEX);
			assertNotNull(depth);
		});
	}
	
	@Test
	public void csvStringData() throws Exception {
		
		Path path = JPaths.getTestResource("csv/131.csv");
		DataFrame frame = reader.read(path, Configuration.createEmpty(), new IMonitor.Stub());
		
		assertNotNull(frame);
		ILazyDataset depth = frame.find(DEPTH_REGEX);
		assertNotNull(depth);
	}

	private void test(String frag, final Configuration conf, Consumer<DataFrame> consumer) throws Exception {
		Path path = JPaths.getTestResource(frag);
		if (Files.isDirectory(path)) {
			Files.walk(path).forEach(p->{
				if (Files.isDirectory(p)) return;
				try {
					load(p, conf, consumer);
				} catch (Exception e) {
					fail(p.getFileName()+" failed. "+e.getMessage());
				}
			});
		} else {
			load(path, conf, consumer);
		}
	}
	
	private void load(Path path, final Configuration conf, Consumer<DataFrame> consumer) throws Exception {
		DataFrame ds = reader.read(path, conf, new IMonitor.Stub());
		consumer.accept(ds);
	}
	
	interface Consumer<T> {

	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param t the input argument
	     */
	    void accept(T t) throws Exception ;
	    
	}
}
