/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.github.h5jan.io;

import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import io.github.h5jan.core.DataFrame;

/**
 * This class saves a DataHolder as a TIFF image file.
 */
class TIFFImageSaver extends JavaImageSaver {

	private static final String FORMAT_NAME = "tiff";

	@Override
	protected boolean writeImageLocked(RenderedImage image, String fileType, File f, DataFrame dh) throws Exception {
		
		// special case to force little endian
		ImageWriter writer = ImageIO.getImageWritersByFormatName(FORMAT_NAME).next();
		IIOMetadata streamMeta = writer.getDefaultStreamMetadata(null);
		String metadataFormatName = streamMeta.getNativeMetadataFormatName();

		// Create the new stream metadata object for new byte order
		IIOMetadataNode tree = new IIOMetadataNode(metadataFormatName);
		IIOMetadataNode endianNode = new IIOMetadataNode("ByteOrder");
		endianNode.setAttribute("value", "LITTLE_ENDIAN");
		tree.appendChild(endianNode);
		streamMeta.setFromTree(metadataFormatName, tree);

		ImageOutputStream stream = ImageIO.createImageOutputStream(f);
		writer.setOutput(stream);
		writer.write(streamMeta, new IIOImage(image, null, null), null);
		return true;
	}
}
