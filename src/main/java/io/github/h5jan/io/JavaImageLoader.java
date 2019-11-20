/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.github.h5jan.io;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.h5jan.core.DataFrame;

/**
 * Class that loads in data from an image file using native Java ImageIO
 * library with built-in image reader/writer.
 * <p>
 * A Raster object comprises a SampleModel and a DataBuffer where we have the
 * model of an image comprising bands of samples so that each pixel is a tuple
 * of samples (e.g. R, G, B) and the SampleModel maps to/from a sample (of a
 * pixel) to information held by the DataBuffer. A BufferedImage object
 * comprises a Raster and a ColorModel. The reader/writer handles BufferImages
 * so access the image data via the BufferedImage's Raster attribute.
 */
class JavaImageLoader extends AbstractFileLoader {

	protected static final Logger logger = LoggerFactory.getLogger(JavaImageLoader.class);

	/**
	 * Image format PNG, JPG, TIFF etc.
	 */
	private String fileType;
	protected boolean asGrey;
	protected boolean keepBitWidth = false;
	
	protected void configure(Configuration configuration) {
		if (configuration.containsKey("asGrey")) this.asGrey = (Boolean)configuration.get("asGrey");
		if (configuration.containsKey("keepBitWidth")) this.keepBitWidth = (Boolean)configuration.get("keepBitWidth");
		if (configuration.containsKey("fileType")) this.fileType = (String)configuration.get("fileType");
	}

	@Override
	public void setFile(final File file) {
		super.setFile(file);
		fileType = FilenameUtils.getExtension(file.getName()).toUpperCase();
	}

	@Override
	public DataFrame load(File file, Configuration configuration, IMonitor mon) throws IOException {
		
		configure(configuration);
		
		// Check for file
		if (!file.exists()) {
			logger.warn("File, {}, did not exist. Now trying to replace suffix", file.getName());
			file = findCorrectSuffix(file);
		}
		setFile(file);

		DataFrame output = new DataFrame(file.getName());
		output.setDtype(Dataset.RGB);
		mon.worked(1);
		
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(file);
		} catch (Exception e) {
			logger.error("Problem creating input stream for file " + file.getName(), e);
			throw new IOException("Problem creating input stream for file " + file.getName(), e);
		}
		if (iis == null) {
			logger.error("File format in '{}' cannot be read", file.getName());
			throw new IOException("File format in '" + file.getName() + "' cannot be read");
		}
		mon.worked(1);
		
		Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
		boolean loaded = false;
		while (it.hasNext()) {
			ImageReader reader = it.next();
			reader.setInput(iis, false, true);
			loaded = createLazyDatasets(output, reader);
			mon.worked(1);
		}
		if (!loaded) {
			logger.error("File format in '{}' cannot be read", file.getName());
			throw new IOException("File format in '" + file.getName() + "' cannot be read");
		}

		return output;
	}

	protected boolean createLazyDatasets(DataFrame output, ImageReader reader) {
		for (int i = 0; true; i++) {
			try {
				int[] shape = new int[] {reader.getHeight(i), reader.getWidth(i)};
				Iterator<ImageTypeSpecifier> it = reader.getImageTypes(i);
				SampleModel sm = it.next().getSampleModel();
				Class<? extends Dataset> clazz = AWTImageUtils.getInterface(sm, keepBitWidth);
				final String name = String.format(IMAGE_NAME_FORMAT, i + 1);
				final int num = i;
				LazyDataset lazy = createLazyDataset(new LazyLoaderStub() {
					@Override
					public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
						Dataset data = loadDataset(file, name, num, asGrey, keepBitWidth);
						return data == null ? null : data.getSliceView(slice);
					}
				}, name, clazz, shape);
				lazy.setName(name);
				output.add(lazy);
			} catch (IndexOutOfBoundsException e) {
				break;
			} catch (Exception e) {
				logger.warn("Could not get height or width for image {}", i);
				continue;
			}
		}
		return output.getColumnNames().size() > 0;
	}

	private static Dataset loadDataset(File file, String name, int num, boolean asGrey, boolean keepBitWidth) throws IOException {
		

		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(file);
		} catch (Exception e) {
			logger.error("Problem creating input stream for file " + file, e);
			throw new IOException("Problem creating input stream for file " + file, e);
		}
		if (iis == null) {
			logger.error("File format in '{}' cannot be read", file);
			throw new IOException("File format in '" + file + "' cannot be read");
		}
		Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
		while (it.hasNext()) {
			ImageReader reader = it.next();
			reader.setInput(iis, false, true);
			Dataset data;
			try {
				data = createDataset(reader.read(num), asGrey, keepBitWidth);
				data.setName(name);
				return data;
			} catch (IndexOutOfBoundsException e) {
				throw new IOException("Image number is incorrect");
			} catch (IOException e) {
				logger.error("Problem reading file", e);
				
			} catch (Exception e) {
				logger.error("Problem creating dataset", e);
			}
		}

		return null;
	}

	protected Dataset createDataset(BufferedImage input) throws IOException {
		return createDataset(input, asGrey, keepBitWidth);
	}

	protected static Dataset createDataset(BufferedImage input, boolean asGrey, boolean keepBitWidth) throws IOException {
		Dataset data = null;
		try {
			Dataset[] channels = AWTImageUtils.makeDatasets(input, keepBitWidth);
			final int bands = channels.length;
			if (bands == 1) {
				data = channels[0];
			} else {
				if (input.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB) {
					throw new IOException("File does not contain RGB data");
				}
				if (bands < 3) {
					throw new IOException("Number of colour channels is less than three so cannot load and convert");
				} else if (bands==3) {
					data = DatasetUtils.createCompoundDataset(RGBDataset.class, channels);
					
				} else if (bands==4) { // Ignore alpha
					// TODO When RGBDataset supports alpha, 
					data = DatasetUtils.createCompoundDataset(RGBDataset.class, channels[0], channels[1], channels[2]);

				} else {
					// Will probably cause an exception depending on what 
					// is supported by DatasetUtils.createCompoundDataset(...)
					data = DatasetUtils.createCompoundDataset(RGBDataset.class, channels);
				}

				if (asGrey)
					data = ((RGBDataset) data).createGreyDataset(channels[0].getClass());
			}
		} catch (Exception e) {
			throw new IOException("There was a problem loading the image", e);
		}
		return data;
	}

	protected File findCorrectSuffix(File file) throws IOException {
		
		String fileName = file.getName();
		String[] suffixes = ImageIO.getReaderFileSuffixes();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

		File f = null;
		testforsuffix: {
			if (!extension.equals(fileName)) { // there is a suffix
				for (String s : suffixes) {
					if (extension.equalsIgnoreCase(s)) {
						break testforsuffix;
					}
				}
			}
			// try standard suffix first then all supported suffixes
			String name = fileName + "." + fileType;
			f = new File(name);
			if (f.exists()) {
				fileName = name;
				break testforsuffix;
			}
			for (String s : suffixes) {
				name = fileName + "." + s;
				f = new File(name);
				if (f.exists()) {
					fileName = name;
					break testforsuffix;
				}
			}
		}
		if (f == null || !f.exists()) {
			throw new IOException("Does not exist",
					new FileNotFoundException(fileName));
		}
		return f;
	}

	public void setAsGrey(boolean asGrey) {
		this.asGrey = asGrey;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	/**
	 * @return true if loader keeps bit width of pixels
	 */
	public boolean isKeepBitWidth() {
		return keepBitWidth;
	}

	/**
	 * set loader to keep bit width of pixels
	 * @param keepBitWidth
	 */
	public void setKeepBitWidth(boolean keepBitWidth) {
		this.keepBitWidth = keepBitWidth;
	}

}
