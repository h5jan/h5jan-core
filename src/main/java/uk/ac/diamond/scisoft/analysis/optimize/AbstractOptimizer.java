/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.optimize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;

/**
 * Abstract base class for optimizer implementations
 */
public abstract class AbstractOptimizer implements IOptimizer {

	protected IFunction function;
	protected ArrayList<IParameter> params; // list of free parameters
	protected int n; // number of free parameters
	protected DoubleDataset[] coords;
	protected DoubleDataset data;
	protected DoubleDataset weight;
	protected double[] point;

	public AbstractOptimizer() {
		params = new ArrayList<IParameter>();
		weight = null;
	}

	/**
	 * Initialize parameters by finding unique and unfixed ones
	 */
	private void initializeParameters() {
		params.clear();
		n = function.getNoOfParameters();
		for (int i = 0; i < n; i++) {
			IParameter p = function.getParameter(i);
			if (p.isFixed())
				continue;

			boolean found = false;
			for (IParameter op : params) {
				if (p == op) {
					found = true;
					break;
				}
			}
			if (!found)
				params.add(p);
		}
		n = params.size();
	}

	/**
	 * Set the weights
	 * @param weight (can be null)
	 */
	public void setWeight(DoubleDataset weight) {
		this.weight = weight;
	}

	@Override
	public void optimize(IDataset[] coordinates, IDataset data, IFunction function) throws Exception {
		if (data.getElementsPerItem() > 1) {
			throw new IllegalArgumentException("The 'data' dataset must be elemental (or non-compound) as only scalar fitting functions are supported");
		}
		this.function = function;
		int nc = coordinates.length;
		this.coords = new DoubleDataset[nc];
		for (int i = 0; i < nc; i++) {
			coords[i] = DatasetUtils.cast(DoubleDataset.class, coordinates[i]);
		}

		this.data = DatasetUtils.cast(DoubleDataset.class, data);
		initializeParameters();
		internalOptimize();
	}

	@Override
	public void optimize(boolean minimize, IFunction function, double... point) throws Exception {
		this.function = function;
		this.point = point;
		initializeParameters();
		internalMinimax(minimize);
	}

	/**
	 * @return coordinates
	 */
	public DoubleDataset[] getCoords() {
		return coords;
	}

	/**
	 * @return data
	 */
	public DoubleDataset getData() {
		return data;
	}

	/**
	 * @return function
	 */
	public IFunction getFunction() {
		return function;
	}

	/**
	 * @return list of unfixed and unique parameters
	 */
	public List<IParameter> getParameters() {
		return params;
	}

	/**
	 * @return array of unfixed and unique parameter values
	 */
	public double[] getParameterValues() {
		double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			values[i] = params.get(i).getValue();
		}
		return values;
	}

	/**
	 * Set parameter values back in function
	 * @param parameters
	 */
	public void setParameterValues(double[] parameters) {
		if (parameters.length > n) {
			throw new IllegalArgumentException("Number of parameters should match number of unfixed and unique parameters in function");
		}
		for (int i = 0; i < parameters.length; i++) {
			params.get(i).setValue(parameters[i]);
		}
		function.setDirty(true);
	}

	/**
	 * Calculate function values for current coordinates
	 * @return values
	 */
	public DoubleDataset calculateValues() {
		return DatasetUtils.cast(DoubleDataset.class, function.calculateValues(coords));
	}

	/**
	 * Calculate function at set point for given parameters
	 * @param parameters
	 * @return residual
	 */
	public double calculateFunction(double[] parameters) {
		setParameterValues(parameters);
		return function.val(point);
	}

	/**
	 * Calculate residual for given parameters
	 * @param parameters
	 * @return residual
	 */
	public double calculateResidual(double[] parameters) {
		setParameterValues(parameters);
		return function.residual(true, data, weight, coords);
	}

	/**
	 * Calculate residual for current parameters
	 * @return residual
	 */
	public double calculateResidual() {
		return function.residual(true, data, weight, coords);
	}

	private final static double DELTA = 1/256.; // initial value
	private final static double DELTA_FACTOR = 0.25;

	private int indexOfParameter(IParameter parameter) {
		for (int i = 0; i < n; i++) {
			if (parameter == params.get(i))
				return i;
		}
		return -1;
	}

	/**
	 * Calculate partial derivative of function at set point with respect to the given parameter at the given parameter values
	 * @param parameter
	 * @param parameters parameter values
	 * @return functionderivative
	 */
	public double calculateFunctionDerivative(IParameter parameter, double[] parameters) {
		if (indexOfParameter(parameter) < 0) {
			return 0;
		}

		setParameterValues(parameters);
		return function.partialDeriv(parameter, point);
	}

	/**
	 * Calculate partial derivative of residual with respect to the given parameter at the given parameter values
	 * @param parameter
	 * @param parameters parameter values
	 * @return residual derivative
	 */
	public double calculateResidualDerivative(IParameter parameter, double[] parameters) {
		if (indexOfParameter(parameter) < 0) {
			return 0;
		}

		setParameterValues(parameters);
		CoordinatesIterator it = CoordinatesIterator.createIterator(data == null ? null : data.getShapeRef(), coords);
		DoubleDataset result = DatasetFactory.zeros(DoubleDataset.class, it.getShape());

		return calculateNumericalDerivative(1e-15, 1e-9, parameter, result, it);
	}

	private static final double SMALLEST_DELTA = Double.MIN_NORMAL * 1024 * 1024;

	private double calculateNumericalDerivative(double abs, double rel, IParameter parameter, DoubleDataset result, CoordinatesIterator it) {
		double delta = DELTA;
		double previous = evaluateNumericalDerivative(delta, parameter, result, it);
		double current = 0;

		while (delta >= SMALLEST_DELTA) {
			delta *= DELTA_FACTOR;
			current = evaluateNumericalDerivative(delta, parameter, result, it);
			if (Math.abs(current - previous) <= Math.max(abs, rel*Math.max(Math.abs(current), Math.abs(previous))))
				break;
			previous = current;
		}
		if (delta <= SMALLEST_DELTA) {
			System.err.println("Did not converge!");
		}

		return current;
	}

	private double evaluateNumericalDerivative(double delta, IParameter parameter, DoubleDataset result, CoordinatesIterator it) {
		double v = parameter.getValue();
		double dv = delta * (v != 0 ? v : 1);

		double d = 0;
		parameter.setValue(v + dv);
		function.setDirty(true);
		if (function instanceof AFunction) {
			((AFunction) function).fillWithValues(result, it);
			d = data.residual(result, weight, false);
		} else {
			d = function.residual(true, data, weight, coords);
		}

		parameter.setValue(v - dv);
		function.setDirty(true);
		if (function instanceof AFunction) {
			((AFunction) function).fillWithValues(result, it);
			d -= data.residual(result, weight, false);
		} else {
			d -= function.residual(true, data, weight, coords);
		}

		parameter.setValue(v);
		function.setDirty(true);

		return d * 0.5/dv;
	}

	/**
	 * This should do the work and set the parameters
	 */
	abstract void internalOptimize() throws Exception;

	/**
	 * This should do the work and set the parameters
	 */
	abstract void internalMinimax(boolean minimize) throws Exception;
}
