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
package io.github.h5jan.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

public abstract class AbstractH5JanTest {

	
	protected WellMetadata createWellMetadata() {
		
		List<MetaValue> values = new ArrayList<MetaValue>();
		values.add(new MetaValue("WELL INFORMATION BLOCK", "WELL", null, "fred", "Freds Well"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "UWI", null, "12342345", "Unique?"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "ELEVREF", "KB", "ELEV", "Elevation ref"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "ELEV", "M", "100", null));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "XCOORD", null, "-11514943.610", "Surface X"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "YCOORD", null, "5924633.340", "Surface Y"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "STRT", "M", "25.14", "Start Depth"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "STOP", "M", "2931.56", "Stop Depth"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "STEP", "M", "0", "Step"));
		values.add(new MetaValue("WELL INFORMATION BLOCK", "NULL", null, "-999.25", "NULL VALUE"));
		values.add(new MetaValue(WellMetadata.CURVE_INFO, "DEPT", "M", null, "Depth"));
		values.add(new MetaValue(WellMetadata.CURVE_INFO, "NPHI_RATIO", "ratio", null, "Neutron Porosity - Measured with an neutron logging device calibrated to a given matrix (limestone, sandstone and dolomite)"));

		return new WellMetadata(values);
	}
	
	protected void round(Dataset someData, String name) throws Exception {
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), someData.getDType());
		frame.setMetadata(createWellMetadata());
		
		DataFrame tmp = readWriteLazy(frame, name);
		assertEquals(frame.getMetadata(), tmp.getMetadata());
		assertEquals(frame, tmp);
	}

	protected DataFrame readWriteLazy(DataFrame frame, String name) throws Exception {
		File dir = new File("test-scratch/temp/");
		if (!dir.exists()) dir.mkdirs();
		
		String path = "test-scratch/temp/"+name+".h5";
		frame.to_hdf(path, "/some/other/path");
		DataFrame read = frame.read_hdf(path);
		(new File(path)).deleteOnExit();
		return read;
	}

}
