/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.Assert;
import org.junit.Test;

public class FermiGaussTest {

	private static final double ABS_TOL = 1e-7;

	@Test
	public void testFunction() {
		AFunction f = new FermiGauss();
		f.setParameterValues(23., 110., 2.5, -0.5, -5.2, 0);
		Assert.assertEquals(6, f.getNoOfParameters());
		Assert.assertArrayEquals(new double[] {23., 110., 2.5, -0.5, -5.2, 0}, f.getParameterValues(), ABS_TOL);

		FunctionTestUtils.checkValues(f);

		Assert.assertEquals((2.5*0 - 0.5)/2. - 5.2, f.val(23.), ABS_TOL);

		double w = 110 * Math.log(2)*FermiGauss.K2EV_CONVERSION_FACTOR;
		Assert.assertEquals((2.5*w - 0.5) / 3 - 5.2, f.val(23. + w), ABS_TOL);
		Assert.assertEquals((-2.5*w - 0.5) / 1.5 - 5.2, f.val(23. - w), ABS_TOL);

		Dataset xd = DatasetFactory.createFromObject(new double[] {23. - w, 23, 23. + 2 * w});
		DoubleDataset fx;
		fx = f.calculateValues(xd);
		Assert.assertArrayEquals(new double[] {(-2.5*w - 0.5)/1.5 - 5.2, (2.5*0 - 0.5)/2. - 5.2,
				(2.5*2*w - 0.5)/5 - 5.2}, fx.getData(), ABS_TOL);

		f.setParameterValues(23., 110., 2.5, -0.5, -5.2, 1);
		fx = f.calculateValues(xd);
		Assert.assertArrayEquals(new double[] {((-2.5*w - 0.5)/1.5 - 5.2)*Math.exp(-5.685e-3),
				((2.5*0 - 0.5)/2. - 5.2)*Math.exp(-3.816e-3),
				((2.5*2*w - 0.5)/5 - 5.2)*Math.exp(9.81e-3)}, fx.getData(), 100*ABS_TOL);

		f.setParameterValues(23., 110., 2.5*2, -0.5*2, -5.2*2, 1);
		fx = f.calculateValues(xd);
		Assert.assertArrayEquals(new double[] {2*((-2.5*w - 0.5)/1.5 - 5.2)*Math.exp(-5.685e-3),
				2*((2.5*0 - 0.5)/2. - 5.2)*Math.exp(-3.816e-3),
				2*((2.5*2*w - 0.5)/5 - 5.2)*Math.exp(9.81e-3)}, fx.getData(), 200*ABS_TOL);
	}

	@Test
	public void testFunctionDerivative() {
		AFunction f = new FermiGauss();
		f.setParameterValues(23., 110., 2.5, -0.5, -5.2, 0);

		FunctionTestUtils.checkPartialDerivatives(f);
	}
}
