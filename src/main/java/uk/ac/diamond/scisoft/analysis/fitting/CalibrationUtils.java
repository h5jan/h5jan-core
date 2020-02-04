/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

public class CalibrationUtils {

	/**
	 * Function to take an axis along with some peaked data with approximately known positions, and re-map it to another
	 * axis with know peak positions
	 * 
	 * @param data
	 *            The actual collected calibration data
	 * @param originalAxis
	 *            The Axis on which the original data was collected
	 * @param originalAxisApproximatePeakPositions
	 *            The approximate positions where characteristic peaks should appear in the data in the old axis
	 *            coordinates
	 * @param newAxisExactPeakPositions
	 *            The exact positions where the peaks should appear on the new Axis
	 * @param peakClass
	 *            The class with which to fit the individual peaks
	 * @param polynomialOrder
	 *            The order of the polynomial with which to fit the mapping process from the old axis to the new
	 * @return The new Axis with the same dimensionality as the original data
	 */
	public static Dataset mapAxis(Dataset data, Dataset originalAxis,
			Dataset originalAxisApproximatePeakPositions, Dataset newAxisExactPeakPositions, Class<? extends IPeak> peakClass,
			int polynomialOrder) {

		Dataset peakPositions = refinePeakPositions(data, originalAxis, originalAxisApproximatePeakPositions,
				peakClass);

		// fit the data with a polynomial
		Polynomial fitResult = Fitter.polyFit(new Dataset[] {peakPositions} ,newAxisExactPeakPositions, 1e-15, polynomialOrder);
		
		// convert the dataset
		Dataset newAxis = fitResult.calculateValues(originalAxis);
		
		return newAxis;
	}

	/**
	 * Takes an approximate set of peak positions and refines them
	 * 
	 * @param originalAxis
	 *            The Axis on which the original data was collected
	 * @param originalAxisApproximatePeakPositions
	 *            The approximate positions where characteristic peaks should appear in the data in the old axis
	 *            coordinates
	 * @param peakClass
	 *            The class with which to fit the individual peaks
	 * @return val
	 */
	public static Dataset refinePeakPositions(Dataset data, Dataset originalAxis,
			Dataset originalAxisApproximatePeakPositions, Class<? extends IPeak> peakClass) {
		
		int numPeaks = originalAxisApproximatePeakPositions.getSize();
		
		List<IPeak> fitResult = Generic1DFitter.fitPeaks(originalAxis, data, peakClass, numPeaks );
		
		Dataset refinedPositions = selectSpecifiedPeaks(originalAxisApproximatePeakPositions, fitResult);
		
		return refinedPositions;
	}

	/**
	 * Given a list of APeak functions, find the ones which most closely match a set of input positions
	 * @param originalAxisApproximatePeakPositions the positions to match peaks to
	 * @param peakList the list of APeaks containing all the peaks to try to match
	 * @return the closest real peak position matches to the original positions.
	 */
	public static Dataset selectSpecifiedPeaks(Dataset originalAxisApproximatePeakPositions,
			List<IPeak> peakList) {
		
		Dataset peakPositions = getPeakList(peakList);
		Dataset resultPositions = DatasetFactory.zeros(DoubleDataset.class, originalAxisApproximatePeakPositions.getShape());
		
		for (int i = 0; i < originalAxisApproximatePeakPositions.getSize(); i++) {
			Dataset compare = Maths.subtract(peakPositions, originalAxisApproximatePeakPositions.getDouble(i));
			compare = Maths.abs(compare);
			resultPositions.set(peakPositions.getDouble(compare.minPos()),i);
		}
		
		return resultPositions;
	}

	/**
	 * Gets a dataset of peak positions out of a list of APeaks
	 * @param peakList a list of APeak functions
	 * @return the dataset of peak positions.
	 */
	public static Dataset getPeakList(List<IPeak> peakList) {
		int n = peakList.size();
		Dataset peakPositons = DatasetFactory.zeros(DoubleDataset.class, n);
		for (int i = 0; i < n; i++) {
			peakPositons.set(peakList.get(i).getPosition(), i);
		}
		return peakPositons;
	}

}
