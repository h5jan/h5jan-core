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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import io.github.h5jan.core.DataFrame;

/**
 * Class that saves data from DataHolder using native Java ImageIO library
 * with built-in image reader/writer
 * 
 * If the DataHolder contains more than one dataset then multiple image
 * files are saved with names labelled from 00001 to 99999.
 */
class JavaImageSaver implements IStreamSaver {

	static {
		ImageIO.scanForPlugins(); // in case the ImageIO jar has not been loaded yet 
	}

	@Override
	public boolean save(OutputStream stream, Configuration conf, DataFrame dh, IMonitor monitor) throws IOException {
		
		String fileName = conf.getFileName();
		String fileType = conf.getFileType(); // format name
		int numBits = conf.getNumBits();
		boolean unsigned = conf.getAsUnsigned();
		double maxVal;
		if (numBits <= 32) {
			maxVal = (1L << numBits) - 1.0; // 2^NumBits - 1 (use long in case of overflow)
		} else {
			maxVal = 1.0; // flag to use doubles (TIFF only)
		}

		if (numBits <= 0) {
			throw new IOException(
					"Number of bits specified must be greater than 0");
		}

		for (int i = 0, imax = dh.size(); i < imax; i++) {

			try {
				String name = null;
				if (imax == 1) {
					name = fileName;
				} else {
					try {
						name = fileName.substring(0,
								(fileName.lastIndexOf(".")));
					} catch (Exception e) {
						name = fileName;
					}

					NumberFormat format = new DecimalFormat("00000");
					name = name + format.format(i + 1);
				}

				int l = new File(name).getName().lastIndexOf(".");
				if (l < 0) {
					name = name + "." + fileType.toLowerCase();
				}

				IDataset idata = dh.get(i).getSlice();
				Dataset data = DatasetUtils.convertToDataset(idata);

				if (numBits <= 16) {
					// test to see if the values of the data are within the
					// capabilities of format
					if (maxVal > 0 && data.max().doubleValue() > maxVal) {
						throw new IOException("The value of a pixel exceeds the maximum value that "
										+ fileType + " is capable of handling. To save a "
										+ fileType + " it is recommended to use a ScaledSaver class. File "
										+ fileName + " not written");
					}
					if (unsigned && data.min().doubleValue() < 0) {
						throw new IOException(
								"The value of a pixel is less than 0. Recommended using a ScaledSaver class.");
					}
					
					BufferedImage image = AWTImageUtils.makeBufferedImage(data, numBits);

					if (image == null) {
						throw new IOException("Unable to create a buffered image to save file type");
					}
					
					boolean w = writeImageLocked(image, fileType, stream, fileName, dh);
					if (!w)
						throw new IOException("No writer for '" + fileName + "' of type " + fileType);
				} else {
					TiledImage image = AWTImageUtils.makeTiledImage(data, numBits);

					if (image == null) {
						throw new IOException("Unable to create a tiled image to save file type");
					}
					boolean w = writeImageLocked(image, fileType, stream, fileName, dh);
					if (!w)
						throw new IOException("No writer for '" + fileName + "' of type " + fileType);
				}
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException("Error saving file '" + fileName + "'", e);
			}

		}
		return true;
	}

	protected boolean writeImageLocked(RenderedImage image, String fileType, OutputStream out, String fileName, DataFrame dh) throws Exception {
		
		// Write meta data to image header
		if (image instanceof PlanarImage) {
			PlanarImage pi = (PlanarImage)image;
			if (fileName!=null) {
				pi.setProperty("originalDataSource", fileName);
			}

// TODO Write metadata
//			NxsMetadata meta = dh.getMetadata();
//			if (meta != null) {
//				Collection<String> dNames = meta.getMetaNames();
//				if (dNames!=null) for (String name : dNames) {
//					pi.setProperty(name, meta.getMetaValue(name));
//				}
//			}
		}
		// Separate method added as may add file locking option.
		return ImageIO.write(image, fileType, out);
	}

}
