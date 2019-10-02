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

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXgeometry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXorientation;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.january.dataset.IDataset;

/**
 * legacy class - recommend to use :ref:`NXtransformations` now
 * Description for a general orientation of a component - used by :ref:`NXgeometry`
 * 
 */
public class NXorientationImpl extends NXobjectImpl implements NXorientation {

	private static final long serialVersionUID = 1L;  // no state in this class, so always compatible


	public static final Set<NexusBaseClass> PERMITTED_CHILD_GROUP_CLASSES = EnumSet.of(
		NexusBaseClass.NX_GEOMETRY);

	public NXorientationImpl() {
		super();
	}

	public NXorientationImpl(final long oid) {
		super(oid);
	}
	
	@Override
	public Class<? extends NXobject> getNXclass() {
		return NXorientation.class;
	}
	
	@Override
	public NexusBaseClass getNexusBaseClass() {
		return NexusBaseClass.NX_ORIENTATION;
	}
	
	@Override
	public Set<NexusBaseClass> getPermittedChildGroupClasses() {
		return PERMITTED_CHILD_GROUP_CLASSES;
	}
	

	@Override
	public NXgeometry getGeometry() {
		return getChild("geometry", NXgeometry.class);
	}

	@Override
	public void setGeometry(NXgeometry geometry) {
		putChild("geometry", geometry);
	}

	@Override
	public NXgeometry getGeometry(String name) {
		return getChild(name, NXgeometry.class);
	}

	@Override
	public void setGeometry(String name, NXgeometry geometry) {
		putChild(name, geometry);
	}

	@Override
	public Map<String, NXgeometry> getAllGeometry() {
		return getChildren(NXgeometry.class);
	}
	
	@Override
	public void setAllGeometry(Map<String, NXgeometry> geometry) {
		setChildren(geometry);
	}

	@Override
	public IDataset getValue() {
		return getDataset(NX_VALUE);
	}

	@Override
	public Double getValueScalar() {
		return getDouble(NX_VALUE);
	}

	@Override
	public DataNode setValue(IDataset value) {
		return setDataset(NX_VALUE, value);
	}

	@Override
	public DataNode setValueScalar(Double value) {
		return setField(NX_VALUE, value);
	}

}
