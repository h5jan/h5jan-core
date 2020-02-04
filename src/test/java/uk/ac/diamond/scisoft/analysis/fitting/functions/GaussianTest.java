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
import org.eclipse.january.dataset.Random;
import org.junit.Assert;
import org.junit.Test;

public class GaussianTest {

	private static final double ABS_TOL = 1e-7;

	@Test
	public void testFunction() {
		APeak f = new Gaussian();
		f.setParameterValues(23., 2., 1.2);
		Assert.assertEquals(3, f.getNoOfParameters());
		Assert.assertArrayEquals(new double[] {23., 2., 1.2}, f.getParameterValues(), ABS_TOL);

		FunctionTestUtils.checkValues(f);

		double tln2 = Math.log(2.) * 2;
		double cArea = 2 * Math.sqrt(Math.PI / (2. * tln2));
		double h = 1.2 / cArea;
		Assert.assertEquals(h, f.val(23.), ABS_TOL);
		Assert.assertEquals(h, f.getHeight(), ABS_TOL);

		Assert.assertEquals(0.5 * h, f.val(23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * h, f.val(23. + 1), ABS_TOL);

		Dataset x = DatasetFactory.createLinearSpace(DoubleDataset.class, 0, 46, 200);
		Dataset v = f.calculateValues(x);
		Assert.assertEquals(1.2, ((Number) v.sum()).doubleValue() * Math.abs(x.getDouble(0) - x.getDouble(1)), ABS_TOL);

		Dataset xd = DatasetFactory.createFromObject(new double[] {23. - 1, 23, 23. + 2});
		DoubleDataset dx;
		dx = f.calculateValues(xd);
		Assert.assertArrayEquals(new double[] {0.5 * h, h, h/16.}, dx.getData(), ABS_TOL);

		double c = h * tln2;
		Assert.assertEquals(0, f.partialDeriv(f.getParameter(0), 23.), ABS_TOL);
		Assert.assertEquals(0.5 * c * -1, f.partialDeriv(f.getParameter(0), 23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * c * 1,  f.partialDeriv(f.getParameter(0), 23. + 1), ABS_TOL);

		Assert.assertEquals(-h/2, f.partialDeriv(f.getParameter(1), 23.), ABS_TOL);
		Assert.assertEquals(0.5 * h * (tln2 / 2 - 1./2), f.partialDeriv(f.getParameter(1), 23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * h * (tln2 / 2 - 1./2), f.partialDeriv(f.getParameter(1), 23. + 1), ABS_TOL);

		Assert.assertEquals(h / 1.2, f.partialDeriv(f.getParameter(2), 23.), ABS_TOL);
		Assert.assertEquals(0.5 * h / 1.2, f.partialDeriv(f.getParameter(2), 23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * h / 1.2,  f.partialDeriv(f.getParameter(2), 23. + 1), ABS_TOL);

		xd = DatasetFactory.createFromObject(new double[] {23. - 1, 23, 23. + 1});
		dx = f.calculatePartialDerivativeValues(f.getParameter(0), xd);
		Assert.assertArrayEquals(new double[] {-0.5*c, 0, 0.5*c}, dx.getData(), ABS_TOL);

		dx = f.calculatePartialDerivativeValues(f.getParameter(1), xd);
		Assert.assertArrayEquals(new double[] {0.25*h*(tln2 - 1), -h/2, 0.25*h*(tln2 - 1)}, dx.getData(), ABS_TOL);

		dx = f.calculatePartialDerivativeValues(f.getParameter(2), xd);
		Assert.assertArrayEquals(new double[] {0.5*h/1.2, h/1.2, 0.5*h/1.2}, dx.getData(), ABS_TOL);

		DoubleDataset[] coords = new DoubleDataset[] {DatasetFactory.createRange(DoubleDataset.class, 15, 30, 0.25)};
		DoubleDataset weight = null;
		CoordinatesIterator it = CoordinatesIterator.createIterator(null, coords);
		DoubleDataset current = DatasetFactory.zeros(DoubleDataset.class, it.getShape());
		DoubleDataset data = Random.randn(it.getShape());
		f.fillWithValues(current, it);
		double rd = data.residual(current, weight, false);
		double rf = f.residual(true, data, weight, coords);
		Assert.assertEquals(rd, rf, 1e-9);
	}

	@Test
	public void testFunctionDerivative() {
		APeak f = new Gaussian();
		f.setParameterValues(23., 2., 1.2);

		FunctionTestUtils.checkPartialDerivatives(f);
	}
}
