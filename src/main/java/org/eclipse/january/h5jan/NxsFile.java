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
package org.eclipse.january.h5jan;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;

import hdf.hdf5lib.H5;

/**
 * A simple delegate class which wraps the Dawnsci classes
 * for easy reading and writing of datasets.
 * 
 * You may also use the dawnsci classes directly
 * however this API will be backwardsly compatible
 * and not leak details of HDF5.
 * 
 * @author Matthew Gerring
 *
 */
public class NxsFile implements AutoCloseable{
	
	// We load the libraries so that the Junit
	// tests give good time comparisons. It is *not* required.
	static {
		H5.loadH5Lib(); // Not required in other code!
	}

	/**
	 * Create a new H5 file (overwriting any existing one)
	 * @param path
	 * @return Nexus formatted H5 file
	 * @throws NexusException
	 * @throws IOException 
	 */
	public static NxsFile create(String path) throws NexusException, IOException {
		createPath(path);
		return new NxsFile(NexusFileHDF5.createNexusFile(path));
	}

	/**
	 * Create a new Nexus file (overwriting any existing one)
	 * @param path
	 * @param enableSWMR
	 * @return Nexus file
	 * @throws NexusException
	 * @throws IOException 
	 */
	public static NxsFile create(String path, boolean enableSWMR) throws NexusException, IOException {
		createPath(path);
		return new NxsFile(NexusFileHDF5.createNexusFile(path, enableSWMR));
	}
	

	private static void createPath(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
	}

	/**
	 * Open an existing Nexus file to modify
	 * @param path
	 * @return Nexus file
	 * @throws NexusException
	 */
	public static NxsFile open(String path) throws NexusException {
		return new NxsFile(NexusFileHDF5.openNexusFile(path));
	}

	/**
	 * Open an existing file read only
	 * @param path
	 * @return Nexus file
	 * @throws NexusException
	 */
	public static NxsFile reference(String path) throws NexusException {
		return new NxsFile(NexusFileHDF5.openNexusFileReadOnly(path));
	}


	private final NexusFile file;
	
	private NxsFile(NexusFile file) {
		this.file = file;
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return file.equals(obj);
	}

	/**
	 * 
	 * @return path to file
	 */
	public String getFilePath() {
		return file.getFilePath();
	}

	/**
	 * Print more debuggined
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		file.setDebug(debug);
	}

	/**
	 * Path of node
	 * @param node
	 * @return path
	 */
	public String getPath(Node node) {
		return file.getPath(node);
	}

	/**
	 * Root node path
	 * @return root
	 */
	public String getRoot() {
		return file.getRoot();
	}

	@Override
	public String toString() {
		return file.toString();
	}

	/**
	 * Get/Create a group node.
	 * @param augmentedPath
	 * @param createPathIfNecessary
	 * @return
	 * @throws NexusException
	 */
	public GroupNode getGroup(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		return file.getGroup(augmentedPath, createPathIfNecessary);
	}

	/**
	 * Get any node of any type.
	 * @param augmentedPath
	 * @return
	 * @throws NexusException
	 */
	public Node getNode(String augmentedPath) throws NexusException {
		return file.getNode(augmentedPath);
	}

	/**
	 * Get group in group with given name and create path if necessary
	 * @param group
	 * @param name
	 * @param nxClass
	 * @param createPathIfNecessary
	 * @return node or null 
	 * @throws NexusException if node is not a group at specified path or group does not exist and not creating path
	 */
	public GroupNode getGroup(GroupNode group, String name, String nxClass, boolean createPathIfNecessary) throws NexusException {
		return file.getGroup(group, name, nxClass, createPathIfNecessary);
	}

	/**
	 * Get data node with given path
	 * @param path
	 * @return node or null if data does not exist at specified path
	 */
	public DataNode getData(String path) throws NexusException {
		return file.getData(path);
	}

	public DataNode getData(GroupNode group, String name) throws NexusException {
		return file.getData(group, name);
	}

	/**
	 * Get data node in group with given name
	 * @param group
	 * @param name
	 * @return node or null if data does not exist in group
	 */
	public DataNode createData(String path, ILazyWriteableDataset data, int compression, boolean createPathIfNecessary)
			throws NexusException {
		return file.createData(path, data, compression, createPathIfNecessary);
	}

	/**
	 * Create data node with given name and path to its group and create path if necessary
	 * @param path to parent group
	 * @param name name within parent group
	 * @param data dataset
	 * @param compression
	 * @param createPathIfNecessary
	 * @return node or null if data does not exist at specified path
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(String path, String name, ILazyWriteableDataset data, int compression,
			boolean createPathIfNecessary) throws NexusException {
		return file.createData(path, name, data, compression, createPathIfNecessary);
	}

	/**
	 * Create data node with given path to its group and create path if necessary
	 * The name of the dataset is used as the name of the data node within the parent group.
	 * @param path to group
	 * @param data
	 * @param createPathIfNecessary
	 * @return node or null if data does not exist at specified path
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(String path, ILazyWriteableDataset data, boolean createPathIfNecessary)
			throws NexusException {
		return file.createData(path, data, createPathIfNecessary);
	}

	/**
	 * Create data node with given name and path to its group and create path if necessary.
	 * @param path to parent group
	 * @param name name within parent group
	 * @param data
	 * @param createPathIfNecessary
	 * @return node or null if data does not exist at specified path
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(String path, String name, ILazyWriteableDataset data, boolean createPathIfNecessary)
			throws NexusException {
		return file.createData(path, name, data, createPathIfNecessary);
	}

	/**
	 * Create data node in given group
	 * @param group
	 * @param data
	 * @param compression
	 * @return node
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(GroupNode group, ILazyWriteableDataset data, int compression) throws NexusException {
		return file.createData(group, data, compression);
	}

	/**
	 * Create data node with given name in given group
	 * @param group parent group
	 * @param data dataset
	 * @param compression
	 * @return node
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(GroupNode group, String name, ILazyWriteableDataset data, int compression)
			throws NexusException {
		return file.createData(group, name, data, compression);
	}

	/**
	 * Create data node in given group
	 * @param group parent group
	 * @param data dataset
	 * @return node
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(GroupNode group, ILazyWriteableDataset data) throws NexusException {
		return file.createData(group, data);
	}

	/**
	 * Create data node with given name in given group
	 * @param group parent group
	 * @param name name within group
	 * @param data dataset
	 * @return node
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(GroupNode group, String name, ILazyWriteableDataset data) throws NexusException {
		return file.createData(group, name, data);
	}

	/**
	 * Create data node with given path to its group and create path if necessary
	 * @param path to group
	 * @param data dataset
	 * @param createPathIfNecessary
	 * @return node or null if data does not exist at specified path
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(String path, IDataset data, boolean createPathIfNecessary) throws NexusException {
		return file.createData(path, data, createPathIfNecessary);
	}

	/**
	 * Create data node with given path to its group and create path if necessary
	 * @param path to group
	 * @param name name within parent group
	 * @param data dataset
	 * @param createPathIfNecessary
	 * @return node or null if data does not exist at specified path
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(String path, String name, IDataset data, boolean createPathIfNecessary)
			throws NexusException {
		return file.createData(path, name, data, createPathIfNecessary);
	}

	/**
	 * Create data node in given group
	 * @param group parent group
	 * @param data dataset
	 * @return node
	 * @throws NexusException when node already exists
	 */
	public DataNode createData(GroupNode group, IDataset data) throws NexusException {
		return file.createData(group, data);
	}

	/**
	 * Create data node with given name in given group
	 * @param group parent group
	 * @param name name within group
	 * @param data dataset
	 * @return
	 * @throws NexusException
	 */
	public DataNode createData(GroupNode group, String name, IDataset data) throws NexusException {
		return file.createData(group, name, data);
	}

	/**
	 * Add node to group. This will recursively add other nodes if the given node is a group node.
	 * @param group
	 * @param name
	 * @param node
	 * @throws NexusException if node already exists in group with name
	 */
	public void addNode(GroupNode group, String name, Node node) throws NexusException {
		file.addNode(group, name, node);
	}

	/**
	 * Add node to given path. This will recursively add other nodes if the given node is a group node.
	 * @param path
	 * @param node
	 * @throws NexusException if node already exists at given path or parts of path does not exist
	 */
	public void addNode(String path, Node node) throws NexusException {
		file.addNode(path, node);
	}

	/**
	 * Remove node from given group.
	 * <p>
	 * Note, this will <b>fail</b> if the file is in SWMR node
	 * @param group parent group of node
	 * @param name name of node in parent group
	 * @throws NexusException
	 */
	public void removeNode(GroupNode group, String name) throws NexusException {
		file.removeNode(group, name);
	}

	/**
	 * Remove node from given path.
	 * <p>
	 * Note, this will <b>fail</b> if the file is in SWMR node
	 * @param path path to parent group of node
	 * @param name name of node in parent group
	 * @throws NexusException
	 */
	public void removeNode(String path, String name) throws NexusException {
		file.removeNode(path, name);
	}

	/**
	 * Create attribute
	 * @param attr
	 * @return attribute
	 */
	public Attribute createAttribute(IDataset attr) {
		return file.createAttribute(attr);
	}

	/**
	 * Add (and write) attribute(s) to given path
	 * @param path
	 * @param attribute
	 */
	public void addAttribute(String path, Attribute... attribute) throws NexusException {
		file.addAttribute(path, attribute);
	}

	/**
	 * Add (and write) attribute(s) to given node
	 * @param node
	 * @param attribute
	 */
	public void addAttribute(Node node, Attribute... attribute) throws NexusException {
		file.addAttribute(node, attribute);
	}

	/**
	 * The full attribute key is: <node full path>@<attribute name>
	 * e.g. /entry1/data@napimount
	 * @param fullAttributeKey
	 * @return
	 * @throws NexusException
	 */
	public String getAttributeValue(String fullAttributeKey) throws NexusException {
		return file.getAttributeValue(fullAttributeKey);
	}

	/**
	 * Link source to a destination. If the destination ends in {@value Node#SEPARATOR}
	 * then the source name is added to the destination.
	 * @param source
	 * @param destination
	 */
	public void link(String source, String destination) throws NexusException {
		file.link(source, destination);
	}

	/**
	 * Link source in another file to a destination. If the destination ends in {@value Node#SEPARATOR}
	 * then the source name is added to the destination.
	 * @param source
	 * @param destination
	 * @param isGroup
	 */
	public void linkExternal(URI source, String destination, boolean isGroup) throws NexusException {
		file.linkExternal(source, destination, isGroup);
	}

	/**
	 * Activates SWMR mode. The file must have been created as
	 * with SWMR enabled.
	 * @throws NexusException if SWMR mode could not be activated
	 */
	public void activateSwmrMode() throws NexusException {
		file.activateSwmrMode();
	}

	/**
	 * Flush data to filesystem
	 * @return the nexus int code for the flush or -1 if unsucessfull.
	 */
	public int flush() throws NexusException {
		return file.flush();
	}

	/**
	 * Flush datasets cached when {@link #setCacheDataset(boolean)} is set true
	 */
	public void flushAllCachedDatasets() {
		file.flushAllCachedDatasets();
	}

	@Override
	public void close() throws NexusException {
		file.close();
	}

	/**
	 * Checks if a path within the file exists
	 * @param path
	 * @return true if path exists
	 */
	public boolean isPathValid(String path) {
		return file.isPathValid(path);
	}

	/**
	 * Sets the datasets written to to be held open until the file is closed
	 * 
	 * With this value set to true, datasets will not be flushed until the file is closed,
	 * call {@link #flushAllCachedDatasets()} to flush
	 * @param cacheDataset
	 */
	public void setCacheDataset(boolean cacheDataset) {
		file.setCacheDataset(cacheDataset);
	}

	/**
	 * Get the complete tree of data.
	 * @return tree or null if none exists.
	 * @throws NexusException
	 */
	public Tree getTree() throws NexusException {
		return ((NexusFileHDF5)file).getTree();
	}

	/**
	 * Read an ILazyDataset
	 * @param path to read
	 * @return lazy dataset which does not use local memory.
	 * @throws NexusException - if cannot read.
	 */
	public ILazyDataset getDataset(String path) throws NexusException {
		return getData(path).getDataset();
	}
}
