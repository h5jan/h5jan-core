/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.github.h5jan.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

import io.github.h5jan.core.DataFrame;

/**
 * This class loads a TIFF image file
 */
public class TIFFImageLoader extends JavaImageLoader {

	@Override
	public DataFrame load(File file, Configuration configuration, IMonitor mon) throws IOException {
		
		// Check for file
		if (!file.exists()) {
			logger.warn("File, {}, did not exist. Now trying to replace suffix", file.getName());
			file = findCorrectSuffix(file);
		}
		setFile(file);
		mon.worked(1);

		DataFrame output = new DataFrame(file.getName());
		output.setDtype(Dataset.RGB);
		mon.worked(1);
		
		ImageReader reader = null;
		try {
			// test to see if the filename passed will load
			ImageInputStream iis = new FileImageInputStream(file);
			mon.worked(1);

			try {
				reader = new TIFFImageReader(new TIFFImageReaderSpi());
				reader.setInput(iis);
				readImages(output, reader); // this raises an exception for 12-bit images when using standard reader
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				logger.debug("Using alternative 12-bit TIFF reader: {}", file.getName());
				reader = new Grey12bitTIFFReader(new Grey12bitTIFFReaderSpi());
				reader.setInput(iis);
				readImages(output, reader);
			}
			mon.worked(1);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Cannot interpret file '" + file.getName() + "'", e);
		} finally {
			if (reader != null)
				reader.dispose();
		}

		return output;
	}

	
	private void readImages(DataFrame output, ImageReader reader) throws Exception {
		
		int n = reader.getNumImages(true);
		if (n == 0) {
			return;
		}

		boolean allSame = true;
		
		int height = -1;
		int width = -1;

		if (height < 0 || width < 0) {
			height = reader.getHeight(0); // this can throw NPE when using 12-bit reader
			width = reader.getWidth(0);
			for (int i = 1; i < n; i++) {
				if (height != reader.getHeight(i) || width != reader.getWidth(i)) {
					allSame = false;
					break;
				}
			}
		}

		final ImageTypeSpecifier its = reader.getRawImageType(0); // this raises an exception for 12-bit images when using standard reader
		if (allSame) {
			for (int i = 1; i < n; i++) {
				if (!its.equals(reader.getRawImageType(i))) {
					throw new IOException("Type of image in stack does not match first");
				}
			}
		}

		Class<? extends Dataset> clazz = AWTImageUtils.getInterface(its.getSampleModel(), keepBitWidth);
		if (n == 1) {
			ILazyDataset image = createLazyDataset(clazz, height, width);
			image.setName(DEF_IMAGE_NAME);
			output.add(image);
		} else if (allSame) {
			ILazyDataset ld = createLazyDataset(clazz, height, width, n);
			output.setData(ld);
		} else {
			createLazyDatasets(output, reader);
		}
	}

	private ILazyDataset createLazyDataset(Class<? extends Dataset> clazz, final int... trueShape) {
		
		LazyLoaderStub l = new LazyLoaderStub() {
			@Override
			public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
				int[] lstart = slice.getStart();
				int[] lstep  = slice.getStep();
				int[] newShape = slice.getShape();
				int[] shape = slice.getSourceShape();
				final int rank = shape.length;

				Dataset d = null;

				if (!Arrays.equals(trueShape, shape)) {
					final int trank = trueShape.length;
					int[] tstart = new int[trank];
					int[] tsize = new int[trank];
					int[] tstep = new int[trank];

					if (rank > trank) { // shape was extended (from left) then need to translate to true slice
						int j = 0;
						for (int i = 0; i < trank; i++) {
							if (trueShape[i] == 1) {
								tstart[i] = 0;
								tsize[i] = 1;
								tstep[i] = 1;
							} else {
								while (shape[j] == 1 && (rank - j) > (trank - i))
									j++;

								tstart[i] = lstart[j];
								tsize[i] = newShape[j];
								tstep[i] = lstep[j];
								j++;
							}
						}
					} else { // shape was squeezed then need to translate to true slice
						int j = 0;
						for (int i = 0; i < trank; i++) {
							if (trueShape[i] == 1) {
								tstart[i] = 0;
								tsize[i] = 1;
								tstep[i] = 1;
							} else {
								tstart[i] = lstart[j];
								tsize[i] = newShape[j];
								tstep[i] = lstep[j];
								j++;
							}
						}
					}

					d = loadData(clazz, mon, file, asGrey, keepBitWidth, shape, tstart, tsize, tstep);
					d.setShape(newShape); // squeeze shape back
				} else {
					d = loadData(clazz, mon, file, asGrey, keepBitWidth, shape, lstart, newShape, lstep);
				}

				return d;
			}

		};

		return createLazyDataset(l, STACK_NAME, clazz, trueShape.clone());
	}

	private static Dataset loadData(Class<? extends Dataset> clazz, IMonitor mon, File file, boolean asGrey, boolean keepBitWidth,
			                        int[] oshape, int[] start, int[] count, int[] step) throws IOException {
		
		ImageInputStream iis = null;
		ImageReader reader = null;

		int rank = start.length;
		boolean is2D = rank == 2;
		int num = is2D ? 0 : start[0];
		int off = rank - 2;
		int[] nshape = Arrays.copyOfRange(oshape, off, rank);
		int[] nstart = Arrays.copyOfRange(start, off, rank);
		int[] nstep = Arrays.copyOfRange(step, off, rank);

		SliceND iSlice = new SliceND(nshape, nstart,
				new int[] {nstart[0] + count[off] * nstep[0], nstart[1] + count[off + 1] * nstep[1]},
				nstep);
		SliceND dSlice = new SliceND(count);

		Dataset d = is2D || count[0] == 1 ? null : DatasetFactory.zeros(clazz, count);

		try {
			// test to see if the filename passed will load
			iis = new FileImageInputStream(file);

			int[] dataStart = dSlice.getStart();
			int[] dataStop  = dSlice.getStop();

			Dataset image;
			try {
				reader = new TIFFImageReader(new TIFFImageReaderSpi());
				reader.setInput(iis);

				image = readImage(file, reader, asGrey, keepBitWidth, num);
			} catch (IllegalArgumentException e) { // catch bad number of bits
				logger.debug("Using alternative 12-bit TIFF reader: {}", file);
				reader = new Grey12bitTIFFReader(new Grey12bitTIFFReaderSpi());
				reader.setInput(iis);

				image = readImage(file, reader, asGrey, keepBitWidth, num);
			}

			while (dataStart[0] < count[0]) {
				if (image == null)
					image = readImage(file, reader, asGrey, keepBitWidth, num);
				image = image.getSliceView(iSlice);
				if (d == null) {
					d = image;
					d.setShape(count);
					break;
				}
				d.setSlice(image, dSlice);
				if (monitorIncrement(mon)) {
					break;
				}
				num += step[0];
				dataStart[0]++;
				dataStop[0] = dataStart[0] + 1;
				image = null;
			} 
		} catch (IOException | IllegalArgumentException e) {
			throw e;
		} finally {
			if (reader != null)
				reader.dispose();
			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
				}
			}
		}

		return d;
	}

	private static Dataset readImage(File file, ImageReader reader, boolean asGrey, boolean keepBitWidth, int num) throws IOException {
		
		int n = reader.getNumImages(true);
		if (num >= n) {
			throw new IOException("Number exceeds images found in '" + file + "'");
		}
		BufferedImage input = reader.read(num);
		if (input == null) {
			throw new IOException("File format in '" + file + "' cannot be read");
		}

		Dataset image = createDataset(input, asGrey, keepBitWidth);
		String imageName = String.format(IMAGE_NAME_FORMAT, num + 1);
		image.setName(imageName);
		return image;
	}
}
