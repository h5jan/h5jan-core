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
package io.github.h5jan;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import io.github.h5jan.core.boundary.DataFrameTest;
import io.github.h5jan.core.boundary.MetaTest;
import io.github.h5jan.core.boundary.SaveTest;
import io.github.h5jan.core.boundary.TypeTest;
import io.github.h5jan.core.examples.DataFrameExample;
import io.github.h5jan.core.examples.DatasetReadExample;
import io.github.h5jan.core.examples.DatasetWriteExample;
import io.github.h5jan.io.ImagesFormatTest;

/**
 * Suite to run just the io.github.h5jan tests.
 * 
 * @author Matthew Gerring
 */
@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
		
		DataFrameTest.class,
		MetaTest.class,
		SaveTest.class,
		TypeTest.class,
		DataFrameExample.class,
		DatasetReadExample.class,
		DatasetWriteExample.class,
		ImagesFormatTest.class
})
public class DataFrameSuite {

}
