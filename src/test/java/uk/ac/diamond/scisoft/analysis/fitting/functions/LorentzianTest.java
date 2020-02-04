/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.Assert;
import org.junit.Test;

public class LorentzianTest {

	private static final double ABS_TOL = 1e-7;

	@Test
	public void testFunction() {
		APeak f = new Lorentzian();
		f.setParameterValues(23., 2., 1.2);
		Assert.assertEquals(3, f.getNoOfParameters());
		Assert.assertArrayEquals(new double[] {23., 2., 1.2}, f.getParameterValues(), ABS_TOL);

		FunctionTestUtils.checkValues(f);

		double h = 1.2 / Math.PI;
		Assert.assertEquals(h, f.getHeight(), ABS_TOL);
		Assert.assertEquals(h, f.val(23.), ABS_TOL);

		Assert.assertEquals(0.5 * h, f.val(23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * h, f.val(23. + 1), ABS_TOL);

		// test that equals(f2) works even if f2 is still "dirty" 
		IFunction f2 = new Lorentzian(new double[] {23., 2., 1.2});
		Assert.assertTrue(f.equals(f2));
		
		Dataset x = DatasetFactory.createLinearSpace(DoubleDataset.class, -100+23, 100+23, 201);
		Dataset v = f.calculateValues(x);
		double s = ((Number) v.sum()).doubleValue() * Math.abs(x.getDouble(0) - x.getDouble(1));
		Assert.assertEquals(1.2, s, 1e-2);

		// test that calculateValues(dataset) gives same as f.val(double)
		Assert.assertEquals(v.getDouble(0), f.val(x.getDouble(0)), ABS_TOL);
	}

	@Test
	public void testFunctionDerivative() {
		APeak f = new Lorentzian();
		f.setParameterValues(23., 2., 1.2);

		FunctionTestUtils.checkPartialDerivatives(f);
	}
}
