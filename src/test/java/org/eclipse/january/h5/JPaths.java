/*-
 *******************************************************************************
 * Copyright (c) 2017 Halliburton International, Inc.
 * All rights reserved. This program and the accompanying materials
 * are private intellectual property.
 *
 *******************************************************************************/
package org.eclipse.january.h5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class attempts to resolve paths for Java similar to the 'paths'
 * module does in the python API.
 * 
 * @author Matthew Gerring
 *
 */
public class JPaths {
	
	/**
	 * A constant for searching backwards from the current working directory.
	 */
    private static final List<String> FOUR_BACK = Arrays.asList("", "../", "../../", "../../../", "../../../../");

	/**
	 * Get the path to the python source code for wireline.
	 * @return the path or null
	 * @throws IOException if the path cannot be found
	 */
	public static final Path getSource() throws IOException {
		return getHome().resolve("src");
	}
	
	/**
	 * Get the path to the python source code for wireline.
	 * @return the path or null
	 * @throws IOException if the path cannot be found
	 */
	public static final Path getTestResource(String testPath) {
		Path res = getHome().resolve("src/test/resources");
		return res.resolve(testPath);
	}


	/**
	 * Get the home of the wireline installation
	 * @return path of the directory
	 * @throws NullPointerException if path cannot be found
	 * @throws IOException if path cannot be found
	 */
	public static final Path getHome() {
		Path home = search("H5JAN_DIR", Arrays.asList("h5jan-core/", "h5jan-core.git/", "h5jan/", "h5jan.git/"), FOUR_BACK, true);
		return home.normalize();
	}

	/**
	 * Search for a path
	 * @param names - names of subpaths to try
	 * @return Path or null if unfound
	 */
	public static Path search(String... names) {
		return search(null, Arrays.asList(names), FOUR_BACK, true);
	}

	private static Path search(String envName, List<String> names, List<String> prefixes, boolean isDir) {
		
		String envValue = envName!=null ? System.getenv(envName) : null;
		if (envValue!=null && !envValue.isEmpty()) {
			Path path = Paths.get(envValue);
			if (Files.exists(path)) return path.toAbsolutePath();
			
			// Search the env too
			List<String> tmp = new ArrayList<>();
			tmp.add(envValue);
			tmp.addAll(names);
			names = tmp;
		}
		
		for (String prefix : prefixes) {  
			for (String name : names) {
				Path place = Paths.get(prefix,name);
				boolean isType = isDir ? Files.isDirectory(place) : Files.exists(place);
				if (Files.exists(place) && isType) {
					return place.toAbsolutePath();
				}
			}
		}
		return null;
	}
	
	/**
	 * This method allows for the fact that gradle may be run from one directory and
	 * junit inside the IDE run from another.
	 * 
	 * @param paths the paths to check, the first of which found existing is
	 *            returned.
	 * @return The path
	 * @throws IOException
	 *             - If the paths are all invalid.
	 */
	public static Path get( String... paths ) throws IOException {
		for (String path : paths) {
			if (path==null) continue; // Ignore null paths
			try {
				Path testPath = Paths.get( path );
				if (Files.exists( testPath )) {
					return testPath.toAbsolutePath();
				}
			} catch ( Exception ne ) {
				continue;
			}
		}
		throw new IOException( "Cannot find paths: " + Arrays.toString( paths ) );
	}

}
