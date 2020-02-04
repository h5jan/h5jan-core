/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.dataset.function;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.function.DatasetToDatasetFunction;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;

/**
 * Function acts as a 2D interpolator using cubic interpolation 
 */
public class BicubicInterpolator implements DatasetToDatasetFunction {

	int[] shape;

	public BicubicInterpolator(int[] newShape) {
		if (newShape == null || newShape.length != 2) {
			throw new IllegalArgumentException("Shape must be 2D");
		}
		shape = newShape;
	}


	// parameters used with the interpolation, for speeding up the process
	private double a00, a01, a02, a03;
	private double a10, a11, a12, a13;
	private double a20, a21, a22, a23;
	private double a30, a31, a32, a33;

	/**
	 * @param p A 4x4 array containing the values of the 16 surrounding points
	 */
	protected void calculateParameters(double[][] p) {
		a00 = p[1][1];
		a01 = -.5*p[1][0] + .5*p[1][2];
		a02 = p[1][0] - 2.5*p[1][1] + 2*p[1][2] - .5*p[1][3];
		a03 = -.5*p[1][0] + 1.5*p[1][1] - 1.5*p[1][2] + .5*p[1][3];
		a10 = -.5*p[0][1] + .5*p[2][1];
		a11 = .25*p[0][0] - .25*p[0][2] - .25*p[2][0] + .25*p[2][2];
		a12 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + .5*p[2][0] - 1.25*p[2][1] + p[2][2] - .25*p[2][3];
		a13 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .25*p[2][0] + .75*p[2][1] - .75*p[2][2] + .25*p[2][3];
		a20 = p[0][1] - 2.5*p[1][1] + 2*p[2][1] - .5*p[3][1];
		a21 = -.5*p[0][0] + .5*p[0][2] + 1.25*p[1][0] - 1.25*p[1][2] - p[2][0] + p[2][2] + .25*p[3][0] - .25*p[3][2];
		a22 = p[0][0] - 2.5*p[0][1] + 2*p[0][2] - .5*p[0][3] - 2.5*p[1][0] + 6.25*p[1][1] - 5*p[1][2] + 1.25*p[1][3] + 2*p[2][0] - 5*p[2][1] + 4*p[2][2] - p[2][3] - .5*p[3][0] + 1.25*p[3][1] - p[3][2] + .25*p[3][3];
		a23 = -.5*p[0][0] + 1.5*p[0][1] - 1.5*p[0][2] + .5*p[0][3] + 1.25*p[1][0] - 3.75*p[1][1] + 3.75*p[1][2] - 1.25*p[1][3] - p[2][0] + 3*p[2][1] - 3*p[2][2] + p[2][3] + .25*p[3][0] - .75*p[3][1] + .75*p[3][2] - .25*p[3][3];
		a30 = -.5*p[0][1] + 1.5*p[1][1] - 1.5*p[2][1] + .5*p[3][1];
		a31 = .25*p[0][0] - .25*p[0][2] - .75*p[1][0] + .75*p[1][2] + .75*p[2][0] - .75*p[2][2] - .25*p[3][0] + .25*p[3][2];
		a32 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + 1.5*p[1][0] - 3.75*p[1][1] + 3*p[1][2] - .75*p[1][3] - 1.5*p[2][0] + 3.75*p[2][1] - 3*p[2][2] + .75*p[2][3] + .5*p[3][0] - 1.25*p[3][1] + p[3][2] - .25*p[3][3];
		a33 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .75*p[1][0] + 2.25*p[1][1] - 2.25*p[1][2] + .75*p[1][3] + .75*p[2][0] - 2.25*p[2][1] + 2.25*p[2][2] - .75*p[2][3] - .25*p[3][0] + .75*p[3][1] - .75*p[3][2] + .25*p[3][3];
	}

	/**
	 * initially nicked from "http://www.paulinternet.nl/?page=bicubic"
	 * 
	 * Interpolates the value of a point in a two dimensional surface using bicubic interpolation.
	 * The value is calculated using the position of the point and the values of the 16 surrounding points.
	 * Note that the returned value can be more or less than any of the values of the surrounding points. 
	 * 
	 * @param x The horizontal distance between the point and the four points left of it, between 0 and 1
	 * @param y The vertical distance between the point and the four points below it, between 0 and 1
	 * @return the interpolated value
	 */
	public double bicubicInterpolate (double x, double y) {
		// only this needs to be called per point
		double x2 = x * x;
		double x3 = x2 * x;
		double y2 = y * y;
		double y3 = y2 * y;

		return a00 + a01 * y + a02 * y2 + a03 * y3 +
		a10 * x + a11 * x * y + a12 * x * y2 + a13 * x * y3 +
		a20 * x2 + a21 * x2 * y + a22 * x2 * y2 + a23 * x2 * y3 +
		a30 * x3 + a31 * x3 * y + a32 * x3 * y2 + a33 * x3 * y3;
	}

	double[][] result = new double[4][4];

	protected double[][] generateSurroundingPoints(int x, int y, final Dataset ds, final int[] dShape) {
		x--; // start to left
		y--; // and below
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++)  {
				result[i][j] = getPoint(x + i, y + j, ds, dShape);
			}
		}
		return result;
	}

	private double getPoint(int i, int j, final Dataset ds, final int[] dShape) {
		// first check the bounds
		if (i < 0) {
			i = 0;
		} else if (i >= dShape[0]) {
			i = dShape[0] - 1;
		}
		if (j < 0) {
			j = 0;
		} else if (j >= dShape[1]) {
			j = dShape[1] - 1;
		}

		return ds.getDouble(i, j);
	}

	@Override
	public List<Dataset> value(IDataset... datasets) {

		List<Dataset> result = new ArrayList<Dataset>();

		for (IDataset ds : datasets) {
			Dataset d = DatasetUtils.convertToDataset(ds);
			final int[] dShape = d.getShapeRef();
			if (dShape == null || dShape.length != 2) {
				throw new IllegalArgumentException("Shape must be 2D");
			}
			// first create the dataset which we will put the data into
			DoubleDataset dds = DatasetFactory.zeros(DoubleDataset.class, shape);

			// calculate the new step size
			double dx = (dShape[0] - 1.0) / (shape[0] - 1.0);
			double dy = (dShape[1] - 1.0) / (shape[1] - 1.0);
			double xscale = 1./dx;
			double yscale = 1./dy;

			for (int i = 0; i < dShape[0] - 1; i++) {
				for (int j = 0; j < dShape[1] - 1; j++) {

					// at this point we can make the pre-calculation to save time
					calculateParameters(generateSurroundingPoints(i, j, d, dShape));

					int xstart = (int) (i * xscale);
					int xend   = (int) ((i + 1) * xscale);
					int ystart = (int) (j * yscale);
					int yend   = (int) ((j + 1) * yscale);

					for (int x = xstart; x <= xend; x++) {
						for (int y = ystart; y <= yend; y++) {

							// find the position
							double xpos = x * dx;
							double ypos = y * dy;

							// remove any whole component
							xpos -= i;
							ypos -= j;

							dds.setItem(bicubicInterpolate(xpos, ypos), x, y);
						}
					}					
				}
			}

			result.add(dds);			
		}

		return result;
	}
}
