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
package io.github.h5jan.core;

import java.util.Iterator;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.SliceIterator;

/**
 * Stitches tiles.
 * @author Matthew Gerring
 *
 */
class Stitcher {

	private final LazyDatasetList 	data;
	private final int[] 			reps;

	/**
	 * Stitch datasets into an image.
	 * @param data
	 * @param reps
	 */
	public Stitcher(LazyDatasetList data, int[] reps) {
		int size = 1;
		for (int i = 0; i < reps.length; i++) size*=reps[i];
		if (data.size()<size) throw new IllegalArgumentException("The tiled size for that shape is "+size+" however we have "+data.size()+" datasets.");
		this.data = data;
		this.reps = reps;
	}
	
	public Dataset stitch() throws DatasetException {
		
		IDataset template = data.get(0).getSlice();
		int[] tileShape = template.getShape();
		int elementsPerItem = template.getElementsPerItem();
		int dtype = DTypeUtils.getDType(template);
		return stitch(tileShape, elementsPerItem, this.reps, dtype);
	}
	
	private Dataset stitch(int[] shape, int elementsPerItem, int[] reps, int dtype) throws DatasetException {
		
		int rank = shape.length;
		final int rlen = reps.length;

		// expand shape
		if (rank < rlen) {
			int[] newShape = new int[rlen];
			int extraRank = rlen - rank;
			for (int i = 0; i < extraRank; i++) {
				newShape[i] = 1;
			}
			for (int i = 0; i < rank; i++) {
				newShape[i+extraRank] = shape[i];
			}

			shape = newShape;
			rank = rlen;
		} else if (rank > rlen) {
			int[] newReps = new int[rank];
			int extraRank = rank - rlen;
			for (int i = 0; i < extraRank; i++) {
				newReps[i] = 1;
			}
			for (int i = 0; i < rlen; i++) {
				newReps[i+extraRank] = reps[i];
			}
			reps = newReps;
		}

		// calculate new shape
		int[] newShape = new int[rank];
		for (int i = 0; i < rank; i++) {
			newShape[i] = shape[i]*reps[i];
		}

		@SuppressWarnings("deprecation")
		Dataset tdata = DatasetFactory.zeros(elementsPerItem, newShape, dtype);
		tdata.setName("stitched_"+data.getName());

		Iterator<ILazyDataset> tiles = data.iterator();
		
		// generate each start point and put a slice in
		IndexIterator iter = tdata.getSliceIterator(null, null, shape);
		SliceIterator siter = (SliceIterator) tdata.getSliceIterator(null, shape, null);
		final int[] pos = iter.getPos();
		while (iter.hasNext()) {
			siter.setStart(pos);
			IDataset tile = tiles.next().getSlice();
			tdata.setSlice(tile, siter);
		}

		return tdata;
	}

}
