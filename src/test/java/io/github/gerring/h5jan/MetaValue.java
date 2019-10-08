/*-
 *******************************************************************************
 * Copyright (c) 2019 Halliburton International, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package io.github.gerring.h5jan;

import java.io.Serializable;

/**
 * LasMetaValue holds a meta value from a Las file.
 * 
 * @author Matthew Gerring
 *
 */
public class MetaValue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6825658018275744622L;
	
	private String sectionName;
	private String name;
	private String unit;
	private String value;
	private String comment;
	
	/**
	 * No-arg this be a bean
	 */
	public MetaValue() {
		
	}
	
	/**
	 * Create a meta value
	 * @param sectionName - section name
	 * @param name - name
	 * @param unit - unit  can be unset
	 * @param value - value  can be unset
	 * @param comment - comment  can be unset
	 */
	public MetaValue(String sectionName, String name, String unit, String value, String comment) {
		super();
		this.sectionName = sectionName;
		this.name = name;
		this.unit = unit;
		this.value = value;
		this.comment = comment;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sectionName == null) ? 0 : sectionName.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaValue other = (MetaValue) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sectionName == null) {
			if (other.sectionName != null)
				return false;
		} else if (!sectionName.equals(other.sectionName))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	@Override
	public String toString() {
		return sectionName +":"+ name + ", unit=" + unit + ", value=" + value
				+ ", comment=" + comment + "]";
	}
}
