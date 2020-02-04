/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.crystallography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.NonSI;

/**
 * Since we cannot have eclipse preferences in this plugin, but
 * we should access them here, we use the user.home to store the 
 * properties and to register them here.
 * 
 * This class is a bean which is persisted to disk and can be deep
 * cloned by the UI when the user is creating another calibrant list.
 */
public class CalibrationStandards implements Serializable, Cloneable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cal2peaks == null) ? 0 : cal2peaks.hashCode());
		result = prime * result + ((selectedCalibrant == null) ? 0 : selectedCalibrant.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		CalibrationStandards other = (CalibrationStandards) obj;
		if (cal2peaks == null) {
			if (other.cal2peaks != null)
				return false;
		} else if (!cal2peaks.equals(other.cal2peaks))
			return false;
		if (selectedCalibrant == null) {
			if (other.selectedCalibrant != null)
				return false;
		} else if (!selectedCalibrant.equals(other.selectedCalibrant))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	private static final Unit<Length> NANOMETRE = UnitUtils.NANOMETRE;
	private static final Unit<Length> ANGSTROM = NonSI.ANGSTROM;

	private Map<String, CalibrantSpacing> cal2peaks;
	private String version;
	private String selectedCalibrant;
	
	@Override
	public CalibrationStandards clone() {
		final CalibrationStandards ret = new CalibrationStandards();
		ret.cal2peaks = new HashMap<String, CalibrantSpacing>(cal2peaks.size());
		for (String key : cal2peaks.keySet()) {
			ret.cal2peaks.put(key, cal2peaks.get(key).clone());
		}
		ret.version           = version;
		ret.selectedCalibrant = selectedCalibrant;
		return ret;
	}

	/**
	 * Used for bean contract, use CalibrationStandards.getInstance() instead.
	 */
	public CalibrationStandards() {	
		version = null;
		cal2peaks    = null;
	}

	/**
	 * @return list of calibrants' names
	 */
	public List<String> getCalibrantList() {
		// cal2peaks must be a LinkedHashMap for this to work.
		return new ArrayList<String>(cal2peaks.keySet());
	}

	/**
	 * Get calibrant from given name
	 * @param calibrant
	 * @return calibrant
	 */
	public CalibrantSpacing getCalibrationPeakMap(String calibrant) {
		return cal2peaks.get(calibrant);
	}

	/**
	 * 
	 * @param cs
	 * @return calibrant
	 */
	public CalibrantSpacing addCalibrant(CalibrantSpacing cs) {
		return cal2peaks.put(cs.getName(), cs);
	}

	public CalibrantSpacing removeCalibrant(String calibrantName) {
		return cal2peaks.remove(calibrantName);
	}

	/**
	 * Calling this method saves this CalibrationStandards as the persisted one
	 * for all of Dawn. Only call when sure that this is the required standards.
	 * @throws Exception
	 */
	public void save() throws Exception {
		CalibrationFactory.saveCalibrationStandards(this);
	}

	// **Update** this when default calibrants have changed 
	static final String CURRENT_VERSION = "1.0.5";

	/**
	 * Default list of calibrants TODO add whatever Alun needs.
	 * 
	 * @return map
	 */
	static Map<String, CalibrantSpacing> createDefaultCalibrants() {
		LinkedHashMap<String, CalibrantSpacing> tmp = new LinkedHashMap<String, CalibrantSpacing>();

		CalibrantSpacing calibrant = new CalibrantSpacing("Collagen Wet"); // FIXME reference
		calibrant.addHKL(new HKL(0, 0, 1,  UnitUtils.getQuantity(67.0, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 2,  UnitUtils.getQuantity(33.5, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 3,  UnitUtils.getQuantity(22.3, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 4,  UnitUtils.getQuantity(16.75, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 5,  UnitUtils.getQuantity(13.4, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 6,  UnitUtils.getQuantity(11.6, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 7,  UnitUtils.getQuantity(9.6,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 8,  UnitUtils.getQuantity(8.4,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 9,  UnitUtils.getQuantity(7.4,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 10, UnitUtils.getQuantity(6.7,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 11, UnitUtils.getQuantity(6.1,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 12, UnitUtils.getQuantity(5.6,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 13, UnitUtils.getQuantity(5.15, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 15, UnitUtils.getQuantity(4.46, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 20, UnitUtils.getQuantity(3.35, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 21, UnitUtils.getQuantity(3.2,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 22, UnitUtils.getQuantity(3.05, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 30, UnitUtils.getQuantity(2.2,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 35, UnitUtils.getQuantity(1.9,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 41, UnitUtils.getQuantity(1.6,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 52, UnitUtils.getQuantity(1.3,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 71, UnitUtils.getQuantity(0.95, NANOMETRE)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("Collagen Dry"); // FIXME reference
		calibrant.addHKL(new HKL(0, 0, 1,  UnitUtils.getQuantity(65.3, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 2,  UnitUtils.getQuantity(32.7, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 3,  UnitUtils.getQuantity(21.8, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 4,  UnitUtils.getQuantity(16.3, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 5,  UnitUtils.getQuantity(13.1, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 6,  UnitUtils.getQuantity(10.9, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 7,  UnitUtils.getQuantity(9.33, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 8,  UnitUtils.getQuantity(8.16, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 9,  UnitUtils.getQuantity(7.26, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 10, UnitUtils.getQuantity(6.53, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 11, UnitUtils.getQuantity(5.94, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 12, UnitUtils.getQuantity(5.44, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 13, UnitUtils.getQuantity(5.02, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 14, UnitUtils.getQuantity(4.66, NANOMETRE)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("Ag Behenate"); // Huang, Toraya, Blanton & Wu, 1993 (58.380)
		calibrant.addHKL(new HKL(0, 0, 1,  UnitUtils.getQuantity(5.8380,  NANOMETRE))); 
		calibrant.addHKL(new HKL(0, 0, 2,  UnitUtils.getQuantity(2.9190,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 3,  UnitUtils.getQuantity(1.9460,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 4,  UnitUtils.getQuantity(1.4595,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 5,  UnitUtils.getQuantity(1.1676,  NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 6,  UnitUtils.getQuantity(0.97300, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 7,  UnitUtils.getQuantity(0.83400, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 8,  UnitUtils.getQuantity(0.72975, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 9,  UnitUtils.getQuantity(0.64867, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 10, UnitUtils.getQuantity(0.58380, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 11, UnitUtils.getQuantity(0.53073, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 12, UnitUtils.getQuantity(0.48650, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 13, UnitUtils.getQuantity(0.44908, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 0, 14, UnitUtils.getQuantity(0.417,   NANOMETRE)));
		//doublet from gisax.com
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(0.458996, NANOMETRE)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(0.45300,  NANOMETRE)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("HDPE"); // FIXME reference
		calibrant.addHKL(new HKL(1, 1, 0, UnitUtils.getQuantity(0.4166, NANOMETRE)));
		calibrant.addHKL(new HKL(2, 0, 0, UnitUtils.getQuantity(0.378 , NANOMETRE)));
		calibrant.addHKL(new HKL(2, 1, 0, UnitUtils.getQuantity(0.3014, NANOMETRE)));
		calibrant.addHKL(new HKL(0, 2, 0, UnitUtils.getQuantity(0.249 , NANOMETRE)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("Silicon"); // 2010 CODATA (5.4310205, Diamond FCC)
		calibrant.addHKL(new HKL(1, 1, 1, UnitUtils.getQuantity(3.1356011,  ANGSTROM)));
		calibrant.addHKL(new HKL(2, 2, 0, UnitUtils.getQuantity(1.9201557,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 1, 1, UnitUtils.getQuantity(1.6375143,  ANGSTROM)));
		calibrant.addHKL(new HKL(4, 0, 0, UnitUtils.getQuantity(1.3577551,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 1, UnitUtils.getQuantity(1.2459616,  ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 2, UnitUtils.getQuantity(1.1086024,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 3, UnitUtils.getQuantity(1.0452004,  ANGSTROM))); // 511
		calibrant.addHKL(new HKL(4, 4, 0, UnitUtils.getQuantity(0.96007786, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 3, 1, UnitUtils.getQuantity(0.91801002, ANGSTROM)));
		calibrant.addHKL(new HKL(6, 2, 0, UnitUtils.getQuantity(0.85871974, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 3, 3, UnitUtils.getQuantity(0.82822286, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 4, 4, UnitUtils.getQuantity(0.78390029, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 5, 1, UnitUtils.getQuantity(0.76049498, ANGSTROM))); // 711
		calibrant.addHKL(new HKL(6, 4, 2, UnitUtils.getQuantity(0.72575064, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 5, 3, UnitUtils.getQuantity(0.70705864, ANGSTROM))); // 731
		calibrant.addHKL(new HKL(8, 0, 0, UnitUtils.getQuantity(0.67887756, ANGSTROM)));
		calibrant.addHKL(new HKL(7, 3, 3, UnitUtils.getQuantity(0.66350476, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 5, 5, UnitUtils.getQuantity(0.62712023, ANGSTROM))); // 751
		calibrant.addHKL(new HKL(8, 4, 0, UnitUtils.getQuantity(0.60720655, ANGSTROM)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("Cr2O3"); // NIST SRM 674 (4.95916(12), 13.5972(6),
		// trigonal - hexagonal scalenohedral; IUCR space group #166)
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(3.645, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(2.672, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(2.487, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(2.181, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(1.819, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(1.676, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(1.467, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(1.433, ANGSTROM)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("CeO2"); // NIST SRM 674 (5.41129, FCC)
		calibrant.addHKL(new HKL(1, 1, 1,  UnitUtils.getQuantity(3.12421,  ANGSTROM)));
		calibrant.addHKL(new HKL(2, 0, 0,  UnitUtils.getQuantity(2.70565,  ANGSTROM)));
		calibrant.addHKL(new HKL(2, 2, 0,  UnitUtils.getQuantity(1.91318,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 1, 1,  UnitUtils.getQuantity(1.63157,  ANGSTROM)));
		calibrant.addHKL(new HKL(2, 2, 2,  UnitUtils.getQuantity(1.56210,  ANGSTROM)));
		calibrant.addHKL(new HKL(4, 0, 0,  UnitUtils.getQuantity(1.35282,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 1,  UnitUtils.getQuantity(1.24144,  ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 0,  UnitUtils.getQuantity(1.21000,  ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 2,  UnitUtils.getQuantity(1.10457,  ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 3,  UnitUtils.getQuantity(1.04140,  ANGSTROM))); // 511
		calibrant.addHKL(new HKL(4, 4, 0,  UnitUtils.getQuantity(0.956590, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 3, 1,  UnitUtils.getQuantity(0.914675, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 4, 2,  UnitUtils.getQuantity(0.901882, ANGSTROM))); // 600
		calibrant.addHKL(new HKL(6, 2, 0,  UnitUtils.getQuantity(0.855600, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 3, 3,  UnitUtils.getQuantity(0.825214, ANGSTROM)));
		calibrant.addHKL(new HKL(6, 2, 2,  UnitUtils.getQuantity(0.815783, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 5, 1,  UnitUtils.getQuantity(0.757732, ANGSTROM))); // 711
		calibrant.addHKL(new HKL(6, 4, 0,  UnitUtils.getQuantity(0.750411, ANGSTROM)));
		calibrant.addHKL(new HKL(6, 4, 2,  UnitUtils.getQuantity(0.723114, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 5, 3,  UnitUtils.getQuantity(0.704490, ANGSTROM))); // 731
		calibrant.addHKL(new HKL(8, 0, 0,  UnitUtils.getQuantity(0.676411, ANGSTROM)));
		calibrant.addHKL(new HKL(7, 3, 3,  UnitUtils.getQuantity(0.661094, ANGSTROM)));
		calibrant.addHKL(new HKL(6, 4, 4,  UnitUtils.getQuantity(0.656215, ANGSTROM))); // 820
		calibrant.addHKL(new HKL(6, 6, 0,  UnitUtils.getQuantity(0.637727, ANGSTROM))); // 822
		calibrant.addHKL(new HKL(7, 5, 1,  UnitUtils.getQuantity(0.624842, ANGSTROM)));
		calibrant.addHKL(new HKL(6, 6, 2,  UnitUtils.getQuantity(0.620718, ANGSTROM)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("Bees Wax"); // FIXME reference
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(3.6, ANGSTROM)));
		calibrant.addHKL(new HKL(UnitUtils.getQuantity(2.4, ANGSTROM)));
		tmp.put(calibrant.getName(), calibrant);

		calibrant = new CalibrantSpacing("LaB6"); // NIST SRM 660a (4.1569162, Cubic)
		calibrant.addHKL(new HKL(1, 0, 0,  UnitUtils.getQuantity(4.156916, ANGSTROM)));
		calibrant.addHKL(new HKL(1, 1, 0,  UnitUtils.getQuantity(2.939383, ANGSTROM)));
		calibrant.addHKL(new HKL(1, 1, 1,  UnitUtils.getQuantity(2.399996, ANGSTROM)));
		calibrant.addHKL(new HKL(2, 0, 0,  UnitUtils.getQuantity(2.078458, ANGSTROM)));
		calibrant.addHKL(new HKL(2, 1, 0,  UnitUtils.getQuantity(1.859029, ANGSTROM)));
		calibrant.addHKL(new HKL(2, 1, 1,  UnitUtils.getQuantity(1.697053, ANGSTROM)));
		calibrant.addHKL(new HKL(2, 2, 0,  UnitUtils.getQuantity(1.469691, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 0, 0,  UnitUtils.getQuantity(1.385638, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 1, 0,  UnitUtils.getQuantity(1.314532, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 1, 1,  UnitUtils.getQuantity(1.253357, ANGSTROM)));
		calibrant.addHKL(new HKL(2, 2, 2,  UnitUtils.getQuantity(1.199998, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 2, 0,  UnitUtils.getQuantity(1.152921, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 2, 1,  UnitUtils.getQuantity(1.110982, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 0, 0,  UnitUtils.getQuantity(1.039229, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 1, 0,  UnitUtils.getQuantity(1.008200, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 0,  UnitUtils.getQuantity(0.979794, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 1,  UnitUtils.getQuantity(0.953661, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 0,  UnitUtils.getQuantity(0.929514, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 1,  UnitUtils.getQuantity(0.907113, ANGSTROM)));
		calibrant.addHKL(new HKL(3, 3, 2,  UnitUtils.getQuantity(0.886257, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 2,  UnitUtils.getQuantity(0.848526, ANGSTROM)));
		calibrant.addHKL(new HKL(4, 2, 2,  UnitUtils.getQuantity(0.848526, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 0, 0,  UnitUtils.getQuantity(0.831383, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 0, 0,  UnitUtils.getQuantity(0.815238, ANGSTROM)));
		calibrant.addHKL(new HKL(5, 1, 1,  UnitUtils.getQuantity(0.799998, ANGSTROM)));
		tmp.put(calibrant.getName(), calibrant);

		return tmp;
	}

	public Map<String, CalibrantSpacing> getCal2peaks() {
		return cal2peaks;
	}
	public String getVersion() {
		return version;
	}
	public void setCal2peaks(Map<String, CalibrantSpacing> cal2peaks) {
		this.cal2peaks = cal2peaks;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return name of selected calibrant (can be null)
	 */
	public String getSelectedCalibrant() {
		return selectedCalibrant;
	}

	/**
	 * Set name of selected calibrant
	 * @param selectedCalibrant
	 */
	public void setSelectedCalibrant(String selectedCalibrant) {
		setSelectedCalibrant(selectedCalibrant, false);
	}

	/**
	 * Set name of selected calibrant
	 * @param selCal
	 * @param fireListeners
	 */
	public void setSelectedCalibrant(String selCal, boolean fireListeners) {
		this.selectedCalibrant = selCal;
		if (fireListeners) CalibrationFactory.fireCalibrantSelectionListeners(this, selectedCalibrant);
	}

	/**
	 * @return the current selected calibrant 
	 */ 
	public CalibrantSpacing getCalibrant() {
		return getCalibrationPeakMap(getSelectedCalibrant());
	}
	
	/**
	 * Default get default calibrant spacing for named calibrant
	 * If not a default calibrant null returned.
	 * @param name
	 * @return calibrant
	 */
	public CalibrantSpacing getDefaultSpacing(String name) {
		
		Map<String, CalibrantSpacing> cal = createDefaultCalibrants();
		
		if (cal.containsKey(name)) {
			return cal.get(name);
		}
		
		return null;
	}

	/**
	 * Set modifiability of standard
	 * @param isModifiable if true then adding more calibrants will throw an exception
	 */
	public void setModifiable(boolean isModifiable) {
		if (isModifiable) {
			cal2peaks = new LinkedHashMap<String, CalibrantSpacing>(cal2peaks);
		} else {
		    cal2peaks = Collections.unmodifiableMap(cal2peaks);
		}
	}

	public boolean isEmpty() {
		return cal2peaks==null || cal2peaks.isEmpty();
	}
}
