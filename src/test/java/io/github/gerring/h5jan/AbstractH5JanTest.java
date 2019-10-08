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
package io.github.gerring.h5jan;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
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
	
	protected void round(Dataset someData) throws Exception {
		round(someData, true);
	}
	
	protected void round(Dataset someData, boolean doLazy) throws Exception {
		someData.setName("fred");
		
		// Make a test frame
		DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), someData.getDType());
		frame.setMetadata(createWellMetadata());
		
		DataFrame tmp = readWriteTmp(frame);
		assertEquals(frame.getMetadata(), tmp.getMetadata());
		assertEquals(frame, tmp);
		
		if (doLazy) {
			tmp = readWriteTmpLazy(frame);
			assertEquals(frame.getMetadata(), tmp.getMetadata());
			assertEquals(frame, tmp);
		}
	}

	protected DataFrame readWriteTmp(DataFrame frame) throws NexusException, IOException, DatasetException {
		frame.to_hdf("test-scratch/temp/tmp.h5", "/some/other/path");
		return frame.read_hdf("test-scratch/temp/tmp.h5");
	}

	protected DataFrame readWriteTmpLazy(DataFrame frame) throws Exception {
		
		frame.to_lazy_hdf("test-scratch/temp/tmp_lazy.h5", "/some/other/path");
		return frame.read_hdf("test-scratch/temp/tmp_lazy.h5");
	}

}
