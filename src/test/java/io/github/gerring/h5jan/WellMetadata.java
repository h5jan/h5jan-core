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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Well metadata which contains the las metadata as a map
 * of maps. Each meta section name is added to the map with
 * a map of its key value pairs.
 * 
 * @author Matthew Gerring
 *
 */
public class WellMetadata implements NxsMetadata {
	
	public static final String CURVE_INFO = "CURVE INFORMATION";

	/**
	 * 
	 */
	private static final long serialVersionUID = 3736866692859597682L;

	public static final WellMetadata EMPTY = new WellMetadata(Collections.emptyList());
	
	private List<MetaValue> metadata;

	/**
	 * Create the metadata with a map of maps. The data is
	 * <pre>{@code Map<section name -> Map<key,value>>} </pre>
	 * 
	 */
	public WellMetadata() {
		super();
	}

	/**
	 * Create the metadata with a map of maps. The data is
	 * <pre>{@code Map<section name -> Map<key,value>>} </pre>
	 * 
	 * @param metadata the data to use
	 */
	public WellMetadata(List<MetaValue> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Size of meta records found
	 * @return size
	 */
	public int size() {
		if (metadata==null) return 0;
		return metadata.size();
	}

	/**
	 * Attempts to find any key of the give name.
	 * This is an inefficient find because there a a small number of keys.
	 * @param name - name to find anywhere in the metadata, case insensitive (a case sensitive find is done first)
	 * @return value or null.
	 */
	public MetaValue find(String name) {
		return find(null, name);
	}

	/**
	 * Get a value by section and name
	 * @param sectionName e.g. VERSION INFORMATION
	 * @param name e.g. VERS names is checked for exact case, then any case.  
	 * NOTE if there is a "." at the end of the metadata name, it is removed.
	 * @return the value
	 */
	public MetaValue find(String sectionName, String name) {
		MetaValue value = find(sectionName, metaValue->metaValue.getName().equals(name));
		if (value!=null) return value;
		value = find(sectionName, metaValue->metaValue.getName().equalsIgnoreCase(name));
		return value;
	}
	
	/**
	 * Find by searching the records and evaluating a predicate to see if the 
	 * match is found.
	 * 
	 * @param sectionName - may be null
	 * @param predicate - to evaluate passing in the MetaValue
	 * @return the value if found or null.
	 */
	public MetaValue find(String sectionName, Predicate<MetaValue> predicate) {
		for (MetaValue metaValue : metadata) {
			if (!matches(metaValue.getSectionName(), sectionName)) {
				continue;
			}
			if (predicate.test(metaValue)) {
				return metaValue;
			}
		}
		return null;
	}

	/**
	 * Find by searching the records and evaluating a predicate to see if the 
	 * match is found.
	 * 
	 * @param sectionName - may be null
	 * @param predicate - to evaluate passing in the MetaValue
	 * @return the value(s) if found or empty set otherwise (not null).
	 */
	public Set<MetaValue> findAll(String sectionName, Predicate<MetaValue> predicate) {
		
		Set<MetaValue> matches  = new HashSet<>();
		for (MetaValue metaValue : metadata) {
			if (!matches(metaValue.getSectionName(), sectionName)) {
				continue;
			}
			if (predicate.test(metaValue)) {
				matches.add(metaValue);
			}
		}
		return matches;
	}

	/**
	 * Inefficient contains method.
	 * @param name to find if contains
	 * @return true if something there
	 */
	public boolean contains(String name) {
		return find(name)!=null;
	}
	

	/**
	 * Inefficient contains method. Returns if this metadata
	 * contains this name.
	 * @param sectionName - the name of the section
	 * @param name to find if contains, case not sensitive.
	 * @return true if something there, in the section with this name.
	 */
	public boolean contains(String sectionName, String name) {
		return find(sectionName, name)!=null;
	}
	
	private boolean matches(String metaName, String nameOrRegex) {
		if (nameOrRegex!=null) {
			
			if (!metaName.equals(nameOrRegex)) {
				if (!metaName.matches(nameOrRegex)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public List<MetaValue> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<MetaValue> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public WellMetadata clone() {
		return new WellMetadata(metadata);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
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
		WellMetadata other = (WellMetadata) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		return true;
	}
}
