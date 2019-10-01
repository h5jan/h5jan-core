/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This file was auto-generated from the NXDL XML definition.
 *******************************************************************************/

package org.eclipse.dawnsci.nexus.impl;

import java.util.Set;
import java.util.EnumSet;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;

import org.eclipse.january.dataset.IDataset;

import org.eclipse.dawnsci.nexus.*;

/**
 * :ref:`NXdata` describes the plottable data and related dimension scales.
 * .. index:: plotting
 * It is mandatory that there is at least one :ref:`NXdata` group
 * in each :ref:`NXentry` group.
 * Note that the ``variable`` and ``data``
 * can be defined with different names.
 * The ``signal`` and ``axes`` attributes of the
 * ``data`` group define which items
 * are plottable data and which are *dimension scales*, respectively.
 * :ref:`NXdata` is used to implement one of the basic motivations in NeXus,
 * to provide a default plot for the data of this :ref:`NXentry`. The actual data
 * might be stored in another group and (hard) linked to the :ref:`NXdata` group.
 * * Each :ref:`NXdata` group will define only one data set
 * containing plottable data, dimension scales, and
 * possibly associated standard deviations.
 * Other data sets may be present in the group.
 * * The plottable data may be of arbitrary rank up to a maximum
 * of ``NX_MAXRANK=32``.
 * * The plottable data will be named as the value of
 * the group ``signal`` attribute, such as::
 * data:NXdata
 * @signal = "counts"
 * @axes = "mr"
 * @mr_indices = 0
 * counts: float[100] --> the default dependent data
 * mr: float[100] --> the default independent data
 * The field named in the ``signal`` attribute **must** exist, either
 * directly as a dataset or defined through a link.
 * * The group ``axes`` attribute will name the
 * *dimension scale* associated with the plottable data.
 * If available, the standard deviations of the data are to be
 * stored in a data set of the same rank and dimensions, with the name ``errors``.
 * * For each data dimension, there should be a one-dimensional array
 * of the same length.
 * * These one-dimensional arrays are the *dimension scales* of the
 * data, *i.e*. the values of the independent variables at which the data
 * is measured, such as scattering angle or energy transfer.
 * .. index:: link
 * .. index:: axes (attribute)
 * The preferred method to associate each data dimension with
 * its respective dimension scale is to specify the field name
 * of each dimension scale in the group ``axes`` attribute as a string list.
 * Here is an example for a 2-D data set *data* plotted
 * against *time*, and *pressure*. (An additional *temperature* data set
 * is provided and could be selected as an alternate for the *pressure* axis.)::
 * data_2d:NXdata
 * @signal="data"
 * @axes=["time", "pressure"]
 * @pressure_indices=1
 * @temperature_indices=1
 * @time_indices=0
 * data: float[1000,20]
 * pressure: float[20]
 * temperature: float[20]
 * time: float[1000]
 * .. rubric:: Old methods to identify the plottable data
 * There are two older methods of associating
 * each data dimension to its respective dimension scale.
 * Both are now out of date and
 * should not be used when writing new data files.
 * However, client software should expect to see data files
 * written with any of these methods.
 * * One method uses the ``axes``
 * attribute to specify the names of each *dimension scale*.
 * * The oldest method uses the ``axis`` attribute on each
 * *dimension scale* to identify
 * with an integer the axis whose value is the number of the dimension.
 * 
 */
public class NXdataImpl extends NXobjectImpl implements NXdata {

	private static final long serialVersionUID = 1L;  // no state in this class, so always compatible


	public static final Set<NexusBaseClass> PERMITTED_CHILD_GROUP_CLASSES = EnumSet.noneOf(NexusBaseClass.class);

	public NXdataImpl() {
		super();
	}

	public NXdataImpl(final long oid) {
		super(oid);
	}
	
	@Override
	public Class<? extends NXobject> getNXclass() {
		return NXdata.class;
	}
	
	@Override
	public NexusBaseClass getNexusBaseClass() {
		return NexusBaseClass.NX_DATA;
	}
	
	@Override
	public Set<NexusBaseClass> getPermittedChildGroupClasses() {
		return PERMITTED_CHILD_GROUP_CLASSES;
	}
	

	@Override
	public String getAttributeSignal() {
		return getAttrString(null, NX_ATTRIBUTE_SIGNAL);
	}

	@Override
	public void setAttributeSignal(String signal) {
		setAttribute(null, NX_ATTRIBUTE_SIGNAL, signal);
	}

	@Override
	public String getAttributeAxes() {
		return getAttrString(null, NX_ATTRIBUTE_AXES);
	}

	@Override
	public void setAttributeAxes(String axes) {
		setAttribute(null, NX_ATTRIBUTE_AXES, axes);
	}

	@Override
	public String getAttributeAXISNAME_indices() {
		return getAttrString(null, NX_ATTRIBUTE_AXISNAME_INDICES);
	}

	@Override
	public void setAttributeAXISNAME_indices(String AXISNAME_indices) {
		setAttribute(null, NX_ATTRIBUTE_AXISNAME_INDICES, AXISNAME_indices);
	}

	@Override
	public IDataset getVARIABLE() {
		return getDataset(NX_VARIABLE);
	}

	@Override
	public Number getVARIABLEScalar() {
		return getNumber(NX_VARIABLE);
	}

	@Override
	public DataNode setVARIABLE(IDataset VARIABLE) {
		return setDataset(NX_VARIABLE, VARIABLE);
	}

	@Override
	public DataNode setVARIABLEScalar(Number VARIABLE) {
		return setField(NX_VARIABLE, VARIABLE);
	}

	@Override
	public Map<String, IDataset> getAllVARIABLE() {
		return getAllDatasets();
	}

	public void setVARIABLE(String name, IDataset VARIABLE) {
		setDataset(name, VARIABLE);
	}

	@Override
	public String getVARIABLEAttributeLong_name() {
		return getAttrString(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_LONG_NAME);
	}

	@Override
	public void setVARIABLEAttributeLong_name(String long_name) {
		setAttribute(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_LONG_NAME, long_name);
	}

	@Override
	public Boolean getVARIABLEAttributeDistribution() {
		return getAttrBoolean(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_DISTRIBUTION);
	}

	@Override
	public void setVARIABLEAttributeDistribution(Boolean distribution) {
		setAttribute(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_DISTRIBUTION, distribution);
	}

	@Override
	public Long getVARIABLEAttributeFirst_good() {
		return getAttrLong(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_FIRST_GOOD);
	}

	@Override
	public void setVARIABLEAttributeFirst_good(Long first_good) {
		setAttribute(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_FIRST_GOOD, first_good);
	}

	@Override
	public Long getVARIABLEAttributeLast_good() {
		return getAttrLong(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_LAST_GOOD);
	}

	@Override
	public void setVARIABLEAttributeLast_good(Long last_good) {
		setAttribute(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_LAST_GOOD, last_good);
	}

	@Override
	@Deprecated
	public Long getVARIABLEAttributeAxis() {
		return getAttrLong(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_AXIS);
	}

	@Override
	@Deprecated
	public void setVARIABLEAttributeAxis(Long axis) {
		setAttribute(NX_VARIABLE, NX_VARIABLE_ATTRIBUTE_AXIS, axis);
	}

	@Override
	public IDataset getVARIABLE_errors() {
		return getDataset(NX_VARIABLE_ERRORS);
	}

	@Override
	public Number getVARIABLE_errorsScalar() {
		return getNumber(NX_VARIABLE_ERRORS);
	}

	@Override
	public DataNode setVARIABLE_errors(IDataset VARIABLE_errors) {
		return setDataset(NX_VARIABLE_ERRORS, VARIABLE_errors);
	}

	@Override
	public DataNode setVARIABLE_errorsScalar(Number VARIABLE_errors) {
		return setField(NX_VARIABLE_ERRORS, VARIABLE_errors);
	}

	@Override
	public Map<String, IDataset> getAllVARIABLE_errors() {
		return getAllDatasets();
	}

	public void setVARIABLE_errors(String name, IDataset VARIABLE_errors) {
		setDataset(name, VARIABLE_errors);
	}

	@Override
	public IDataset getData() {
		return getDataset(NX_DATA);
	}

	@Override
	public Number getDataScalar() {
		return getNumber(NX_DATA);
	}

	@Override
	public DataNode setData(IDataset data) {
		return setDataset(NX_DATA, data);
	}

	@Override
	public DataNode setDataScalar(Number data) {
		return setField(NX_DATA, data);
	}

	@Override
	public Map<String, IDataset> getAllData() {
		return getAllDatasets();
	}

	public void setData(String name, IDataset data) {
		setDataset(name, data);
	}

	@Override
	@Deprecated
	public Long getDataAttributeSignal() {
		return getAttrLong(NX_DATA, NX_DATA_ATTRIBUTE_SIGNAL);
	}

	@Override
	@Deprecated
	public void setDataAttributeSignal(Long signal) {
		setAttribute(NX_DATA, NX_DATA_ATTRIBUTE_SIGNAL, signal);
	}

	@Override
	@Deprecated
	public String getDataAttributeAxes() {
		return getAttrString(NX_DATA, NX_DATA_ATTRIBUTE_AXES);
	}

	@Override
	@Deprecated
	public void setDataAttributeAxes(String axes) {
		setAttribute(NX_DATA, NX_DATA_ATTRIBUTE_AXES, axes);
	}

	@Override
	public String getDataAttributeUncertainties() {
		return getAttrString(NX_DATA, NX_DATA_ATTRIBUTE_UNCERTAINTIES);
	}

	@Override
	public void setDataAttributeUncertainties(String uncertainties) {
		setAttribute(NX_DATA, NX_DATA_ATTRIBUTE_UNCERTAINTIES, uncertainties);
	}

	@Override
	public String getDataAttributeLong_name() {
		return getAttrString(NX_DATA, NX_DATA_ATTRIBUTE_LONG_NAME);
	}

	@Override
	public void setDataAttributeLong_name(String long_name) {
		setAttribute(NX_DATA, NX_DATA_ATTRIBUTE_LONG_NAME, long_name);
	}

	@Override
	public IDataset getErrors() {
		return getDataset(NX_ERRORS);
	}

	@Override
	public Number getErrorsScalar() {
		return getNumber(NX_ERRORS);
	}

	@Override
	public DataNode setErrors(IDataset errors) {
		return setDataset(NX_ERRORS, errors);
	}

	@Override
	public DataNode setErrorsScalar(Number errors) {
		return setField(NX_ERRORS, errors);
	}

	@Override
	public IDataset getScaling_factor() {
		return getDataset(NX_SCALING_FACTOR);
	}

	@Override
	public Double getScaling_factorScalar() {
		return getDouble(NX_SCALING_FACTOR);
	}

	@Override
	public DataNode setScaling_factor(IDataset scaling_factor) {
		return setDataset(NX_SCALING_FACTOR, scaling_factor);
	}

	@Override
	public DataNode setScaling_factorScalar(Double scaling_factor) {
		return setField(NX_SCALING_FACTOR, scaling_factor);
	}

	@Override
	public IDataset getOffset() {
		return getDataset(NX_OFFSET);
	}

	@Override
	public Double getOffsetScalar() {
		return getDouble(NX_OFFSET);
	}

	@Override
	public DataNode setOffset(IDataset offset) {
		return setDataset(NX_OFFSET, offset);
	}

	@Override
	public DataNode setOffsetScalar(Double offset) {
		return setField(NX_OFFSET, offset);
	}

	@Override
	public IDataset getX() {
		return getDataset(NX_X);
	}

	@Override
	public Double getXScalar() {
		return getDouble(NX_X);
	}

	@Override
	public DataNode setX(IDataset x) {
		return setDataset(NX_X, x);
	}

	@Override
	public DataNode setXScalar(Double x) {
		return setField(NX_X, x);
	}

	@Override
	public IDataset getY() {
		return getDataset(NX_Y);
	}

	@Override
	public Double getYScalar() {
		return getDouble(NX_Y);
	}

	@Override
	public DataNode setY(IDataset y) {
		return setDataset(NX_Y, y);
	}

	@Override
	public DataNode setYScalar(Double y) {
		return setField(NX_Y, y);
	}

	@Override
	public IDataset getZ() {
		return getDataset(NX_Z);
	}

	@Override
	public Double getZScalar() {
		return getDouble(NX_Z);
	}

	@Override
	public DataNode setZ(IDataset z) {
		return setDataset(NX_Z, z);
	}

	@Override
	public DataNode setZScalar(Double z) {
		return setField(NX_Z, z);
	}

}
