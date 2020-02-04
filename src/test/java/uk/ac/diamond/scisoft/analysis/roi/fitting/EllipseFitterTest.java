/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.roi.fitting;

import java.util.Arrays;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.eclipse.dawnsci.analysis.api.fitting.IConicSectionFitter;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.dataset.roi.fitting.AngleDerivativeFunction;
import org.eclipse.dawnsci.analysis.dataset.roi.fitting.EllipseFitter;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Random;
import org.junit.Assert;
import org.junit.Test;

public class EllipseFitterTest {

	@Test
	public void testOrthoDist() {

		AngleDerivativeFunction angleDerivative = new AngleDerivativeFunction();
		BrentSolver solver = new BrentSolver();
		double a = 10.2;
		double b = 3.1;
		final double twopi = 2*Math.PI;
		double alpha = twopi*10./360; // 0 to 2 pi
		angleDerivative.setRadii(a, b);
		angleDerivative.setAngle(alpha);

//		final double ca = 0;
//		final double cb = b-0.5;
		final double Xc = -5.; // Math.cos(alpha)*ca + Math.sin(alpha)*cb;
		final double Yc = 5.5; //Math.sin(alpha)*ca + Math.cos(alpha)*cb;

		angleDerivative.setCoordinate(Xc, Yc);
		try {
			// find quadrant to use
			double p = Math.atan2(Yc, Xc);
			if (p < 0)
				p += twopi;
			p -= alpha;
			final double end;
			final double halfpi = 0.5*Math.PI;
			p /= halfpi;
			end = Math.ceil(p)*halfpi;
			final double angle = solver.solve(100, angleDerivative, end-halfpi, end);
//			final double cos = Math.cos(angle);
//			final double sin = Math.sin(angle);

			Assert.assertEquals("Angle found is not close enough", 1.930, angle, 0.001);
//			double dx = a*cos + Xc;
//			double dy = b*sin + Yc;
//
//			System.out.println("Bracket angle = " + Math.ceil(p));
//			System.out.println("Delta angle = " + 180.*angle/Math.PI);
//			System.out.println(dx + ", " + dy);
		} catch (TooManyEvaluationsException e) {
			// TODO
			System.err.println(e);
		} catch (MathIllegalArgumentException e) {
			// TODO
			System.err.println(e);
		}

	}

	@Test
	public void testQuickEllipse() {
		checkGeneratedEllipse(20, 10, 344.2, 243.2, Math.PI*0.23, 0.2, -12.3);

		for (int i = 1; i <= 10; i++) {
			System.out.println("i = " + i);
			checkGeneratedEllipse(100, 3, 8.0701e+02, 4.7593e+00, i*0.002*Math.PI, 1.2024e+03, 1.6759e+02);
		}
	}

	void checkGeneratedEllipse(int pts, double std, double... original) {
		Random.seed(1277);
		DoubleDataset theta;
//		theta = DatasetFactory.createFromObject(DoubleDataset.class, new double[] {0.1, 0.2, 0.25, 0.33, 0.35, 0.37, 0.43, });
//		theta = Random.rand(0, 2*Math.PI, pts);
		theta = (DoubleDataset) DatasetFactory.createLinearSpace(0, 2*Math.PI, pts, Dataset.FLOAT64);

		Dataset[] coords = EllipseFitter.generateCoordinates(theta, original);

		Dataset x;
		Dataset y;
//		x = DatasetFactory.createFromObject(new double[] {242.34, 188.08, 300.04, 188.90, 300.97, 103.80, 157.67, 141.81, 302.64, 266.58});
//		y = DatasetFactory.createFromObject(new double[] {-262.478, 147.192, -107.673, 136.293, -118.735, 217.387, 166.996, 192.521, -55.201, 17.826});
		x = Maths.add(coords[0], Random.randn(0.0, std, theta.getShape()));
		y = Maths.add(coords[1], Random.randn(0.0, std, theta.getShape()));
		System.err.println(x.toString(true));
		System.err.println(y.toString(true));

		IConicSectionFitter fitter = new EllipseFitter();

		fitter.algebraicFit(x, y);
		double[] result = fitter.getParameters();

		double[] rtols = new double[] {1.5e-1, 11.5e-1, 1.5e-1, 7, 2e-1};
		double[] atols = new double[] {10, 10, 5*Math.PI/180, 10, 10};
		for (int i = 0; i < original.length; i++) {
			double err = Math.max(Math.abs(original[i]*rtols[i]), atols[i]);
			Assert.assertEquals("Algebraic fit: " + i, original[i], result[i], err);
		}
		System.err.println(Arrays.toString(original));
		System.err.println(Arrays.toString(result));

		fitter.geometricFit(x, y, result);
		result = fitter.getParameters();
		System.err.println(Arrays.toString(result));
		for (int i = 0; i < original.length; i++) {
			double err = Math.max(Math.abs(original[i]*rtols[i]), atols[i]);
			Assert.assertEquals("Geometric fit: " + i, original[i], result[i], err);
		}
	}
}

