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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.junit.Before;

import io.github.h5jan.core.DataFrame;

public abstract class AbstractReaderTest {
	
	protected DataFrameReader reader;
	protected DataFrameWriter writer;

	@Before
	public void before() throws Exception {
		this.reader = new DataFrameReader();
		this.writer = new DataFrameWriter();
	}

	protected void round(Dataset set, String sdir, String name) throws Exception {
		DataFrame output = new DataFrame(name, Dataset.RGB, set.getShape());
		output.add(set);
		Configuration conf = Configuration.image(output.getName(), "JPG", 8, false);
		File dir = new File(sdir);
		dir.mkdirs();
		writer.save(dir, conf, output, new IMonitor.Stub());
		
		File file = new File("test-scratch/image/"+name+".jpg");
		DataFrame frame = reader.read(file, Configuration.DEFAULT, new IMonitor.Stub());
		
		assertArrayEquals(output.getShape(), frame.getShape());
	}


}
