/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.optimize;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Basic least squares solver using SVD method
 */
public class LinearLeastSquares {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(LinearLeastSquares.class);
	private final double threshold; // threshold ratio 

	/**
	 * Base constructor
	 * 
	 * @param tolerance
	 *            ratio of lowest to highest singular value to allow
	 */
	public LinearLeastSquares(double tolerance) {
		threshold = tolerance;
	}

	/**
	 * Solve linear least square problem defined by
	 * <pre>
	 * A x = b
	 * </pre>
	 * @param matrix <pre>A</pre>
	 * @param data <pre>b</pre>
	 * @param sigmasq estimate of squared error on each data point
	 * @return array of values
	 */
	public double[] solve(Dataset matrix, Dataset data, Dataset sigmasq) {
		if (matrix.getRank() != 2) {
			logger.error("Matrix was not 2D");
			throw new IllegalArgumentException("Matrix was not 2D");
		}

		final int[] shape = matrix.getShape();
		final int dlen = data.getShape()[0];
		if (data.getRank() != 1 && shape[1] != dlen) {
			logger.error("Data was not 1D or else not correct length");
			throw new IllegalArgumentException("Data was not 2D or else not correct length");
		}

		final Matrix X = new Matrix((double [][]) DatasetUtils.createJavaArray(matrix.cast(DoubleDataset.class)));
		final Matrix W = new Matrix((double [][]) DatasetUtils.createJavaArray(DatasetUtils.diag(Maths.reciprocal(sigmasq.cast(DoubleDataset.class)), 0)));

		final Matrix XtW = X.transpose().times(W);
		final Matrix A = XtW.times(X);

		final SingularValueDecomposition svd = A.svd();

		final double[] values = svd.getSingularValues();


		final Matrix b = new Matrix(dlen, dlen);
		for (int i = 0; i < dlen; i++) {
			b.set(i, 0, data.getDouble(i));
		}

		final Matrix c = svd.getV().transpose().times(XtW.times(b));

		final double limit = threshold*values[0];
		final int vlen = values.length;

		for (int i = 0; i < vlen; i++) {
			final double v = values[i];
			c.set(i, 0, v >= limit ? c.get(i, 0) / v : 0);
		}

		final Matrix d = svd.getU().times(c);

		final double[] result = new double[vlen];
		for (int i = 0; i < vlen; i++) {
			result[i] = d.get(i, 0);
		}

		return result;
	}

}
