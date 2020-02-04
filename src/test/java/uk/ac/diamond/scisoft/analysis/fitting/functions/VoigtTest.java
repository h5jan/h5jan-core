/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.eclipse.january.asserts.TestUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.Assert;
import org.junit.Test;


public class VoigtTest {

	private static final double ABS_TOL = 1e-7;
	private static final double REL_TOL = 1e-15;

	@Test
	public void testFunction() {
		Voigt f = new Voigt();
		f.setParameterValues(23., 2., 1.2, 2.3);
		Assert.assertEquals(4, f.getNoOfParameters());
		Assert.assertArrayEquals(new double[] {23., 2., 1.2, 2.3}, f.getParameterValues(), ABS_TOL);

		FunctionTestUtils.checkValues(f);

		double h = f.getHeight();
		Assert.assertEquals(h, f.val(23.), ABS_TOL);

		// check half-maximum intersections
		h *= 0.5;
		double dx = 1.77;
		Assert.assertTrue(f.val(23. - dx - 0.01) < h && f.val(23. - dx) > h);
		Assert.assertTrue(f.val(23. + dx) > h && f.val(23. + dx + 0.01) < h);

		// check area
		Dataset x = DatasetFactory.createLinearSpace(DoubleDataset.class, -20+23, 20+23, 401);
		Dataset v = f.calculateValues(x);
		double s = ((Number) v.sum()).doubleValue() * Math.abs(x.getDouble(0) - x.getDouble(1));
		Assert.assertEquals(1.2, s, 1e-1);
	}

	@Test
	public void testExtremes() {
		Dataset x = DatasetFactory.createLinearSpace(DoubleDataset.class, -20+23, 20+23, 401);

		Voigt v = new Voigt();
		v.setParameterValues(23., 2., 1.2, 0);
		Dataset pl = v.calculateValues(x);

		v.setParameterValues(23., 0, 1.2, 2.3);
		Dataset pg = v.calculateValues(x);

		Lorentzian lf = new Lorentzian();
		lf.setParameterValues(23., 2., 1.2);
		Dataset l = lf.calculateValues(x);
		TestUtils.assertDatasetEquals(l, pl, REL_TOL, ABS_TOL);

		Gaussian gf = new Gaussian();
		gf.setParameterValues(23., 2.3, 1.2);
		Dataset g = gf.calculateValues(x);
		TestUtils.assertDatasetEquals(g, pg, REL_TOL, ABS_TOL);
	}

	@Test
	public void testFunctionDerivative() {
		Voigt f = new Voigt();
		f.setParameterValues(23., 2., 1.2, 2.3);

		FunctionTestUtils.checkPartialDerivatives(f);
	}
}
