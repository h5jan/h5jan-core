![](https://github.com/h5jan/h5jan-core/blob/master/eclipsesci.png) h5jan
=====

## What is it
h5jan is a Java(TM) API for reading and writing [Eclipse January*](https://github.com/eclipse/january) datasets to HDF5.
It writes the HDF5 to a self describing format
which is easily readable from Python as DataFrames using h5py. (The format is: [NeXus](http://lns00.psi.ch/NeXus/))

It allows the reading and writing of:
1. <b>Datasets</b> to/from HDF5 files (numpy nD array and pandas DataFrame)
2. <b>Lazy datasets</b> to/from HDF5 files and working with slices. (Larger data than will fit in memory).

Why are these things useful? Well it means that binary data structures can be built in Java and
saved as HDF5. That then means that you can use Java in the middleware or middle
microservice, here it shines with tools like [Spring Boot](https://spring.io/projects/spring-boot) and [Grails](https://grails.org) available.
Then if a parallel execution of python process such as 
machine learning run are required, h5jan allows you to write scalable h5 files which can be loaded
as dataframes or numpy arrays in python using h5py and pandas.

The reading and writing API are based around slicable lazy data. This allows code to be
written which interacts with very large datasets without loading all the data into memory. You can write 
on the fly using ILazyWritableDataset and read slices as required for data analysis. This means that instead
of holding large datasets in memory on vendor cloud and paying the cost, relatively cheap solutions around
data slicing can be created - where that would work with your design of course!

 \* Eclipse January has a page on the [Eclipse Foundation](https://www.eclipse.org/january/) web site.
 
## Examples

### DataFrame Examples
#### Write DataFrame

```java

	// Write in memory
	// We create a place to put our data
	IDataset someData = Random.rand(256, 3);
	someData.setName("fred");
		
	// Make a test frame
	DataFrame frame = new DataFrame(someData, 1, Arrays.asList("a", "b", "c"), Dataset.FLOAT32);
		
	// Save to HDF5
	frame.to_hdf("test-scratch/write_example/inmem_data_frame.h5", "/entry1/myData");
```

### Reading DataFrame in Python
```python

	# Make a reader
	reader = DataFrameReader()
	
	# Read the frame
	print(reader.read('test-scratch/write_example/lazy_data_frame-2d.h5'))
	
```
The python class DataFrameReader is not yet available on pypi
it is here: [DataFrameReader](https://github.com/h5jan/h5jan-core/blob/master/read_example.py)

#### Write Data Frame as Slices - save memory!
```java

	// Write as slices, not all frame in memory at one time.
	// We create a place to put our data
	DataFrame frame = new DataFrame("data", Dataset.FLOAT32, new int[] { 256 });
		
	// Save to HDF5, columns can be large, these are not it's a test
	try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_data_frame.h5", "/entry1/myData")) {
			
		// Add the columns incrementally without them all being in memory
		for (int i = 0; i < 10; i++) {
			app.append("slice_"+i, Random.rand(256));
		}
	}

```

### Dataset Examples
#### Read
```java

	// Read a slice
	try(NxsFile nfile = NxsFile.open("i05-4859.h5")) {
	
		// Data *not* read in
		ILazyDataset lz = nfile.getDataset("/entry1/instrument/analyser/data");
		
		// Read in a slice and squeeze it into an image. *Data now in memory*
		IDataset    mem = lz.getSlice(new Slice(), new Slice(100, 600), new Slice(200, 700));
		mem.squeeze();
	}
```
#### Write nD array
```java

	// Write nD data to block not holding it in memory
	try(NxsFile nfile = NxsFile.open("my_example.h5")) {
	
		// We create a place to put our data
		ILazyWriteableDataset data = new LazyWriteableDataset("data", Dataset.FLOAT64, new int[] { 10, 1024, 1024 }, null, null, null);
	
		// We have all the data in memory, it might be large at this point!
		nfile.createData("/entry1/acme/experiment1/", data, true);
			
		// Make an image to write
		IDataset random = Random.rand(1, 1024, 1024);
			
		// Write one image, others may follow
		data.setSlice(new IMonitor.Stub(), random, new SliceND(random.getShape(), new Slice(0,1), new Slice(0,1024), new Slice(0,1024)));
	}
```

#### Write stream of images
```java

	// Write stream of images in above example.
	for (int i = 0; i < 10; i++) {
		// Make an image to write
		IDataset random = Random.rand(1, 1024, 1024);
				
		// Write one image, others may follow. We use the int args for adding the randoms here.
		data.setSlice(new IMonitor.Stub(), random, SliceND.createSlice(data, new int[] {i,0,0}, new int[] {i+1,1024,1024}, new int[] {1,1,1}));
				
		// Optionally flush
		nfile.flush();
	}

```

Example if the images come from a directory structure, as many detectors and microscopes do.
Each directory contains tiles of a larger image then each directory above that is a represents the stack.
We write the HDF5 stack directly to a single file in a lazy way such that the whole thing is never in memory.

```java

  // Make a writing frame, the tiled image is this size.
  // Each tile is 96,128 so as we are making 3x3 stitching, we need 288,384
  DataFrame frame = new DataFrame("scope_image", Dataset.FLOAT32, new int[] { 288,384 });
		
  // Make an object to read other formats, in this case TIFF
  DataFrameReader reader = new DataFrameReader();

  // Save to HDF5, columns can be large, these are not it's a test
  try (Appender app = frame.open_hdf("test-scratch/write_example/lazy_microscope_image.h5", "/entry1/myData")) {
			
  // Directory structure is "microscope/0" image0, "microscope/1" image1 and in each
  // directory are nine images which need to be stitched.
  File[] dirs = JPaths.getTestResource("microscope").toFile().listFiles();
			
  for (int i = 0; i < dirs.length; i++) {
				
	// Directory of tiles
	File dir = dirs[i];

	// Read tiles, assuming their file name order is also their tile order.
	DataFrame tiles = reader.read(dir, Configuration.GREYSCALE, new IMonitor.Stub());
				
	// Stitch to make image based on a 3x3 matrix of tiles.
	Dataset image = tiles.stitch(new int[] {3,3});
	assertArrayEquals(new int[] {288,384}, frame.getColumnShape());
	image = DatasetUtils.cast(image, Dataset.FLOAT32);
				
	// Add the image - note they are not all in memory
	// so this process should scale reasonably well.
	app.append("image_"+i, image);
    }
  }
```

## Additional Examples
The [other examples](https://github.com/h5jan/h5jan-core/tree/master/src/test/java/io/github/h5jan/core/examples) are run as part of the unit tests.
[Eclipse January Examples](https://github.com/eclipse/january/tree/master/org.eclipse.january.examples/src/org/eclipse/january/examples/dataset)

When the image stack nxs file is opened in [DAWN](http://www.dawnsci.org) you get:
![stack](https://github.com/h5jan/h5jan-core/blob/master/dawn.png)

## Repackaging
This project is only possible by repackaging some code released on github using an EPL license.
Unfortunately you cannot use this code in its current locations over several bundles in [DAWN Science](https://github.com/DawnScience)
which are not repackaged for reuse in a normal gradle/maven manner (they are OSGi bundles).

h5jan uses those libraries and adds a data frame library on top.

## Taking Part
Drop a message as to why you want to be part of the project or submit a merge request.

We support the [Eclipse Science](https://science.eclipse.org/) working group.<br>
![Eclipse Science](https://github.com/h5jan/h5jan-core/blob/master/eclipsesci.png)
NOTE: This project *has not* been proposed as an eclipse science project yet, however
it is made up of code from the dawnsci project and depends on January. 

 
## Setting PATH and LD_LIBRARY_PATH
This library will load its native parts automatically.
If you hit problems you can set PATH on windows or LD_LIBRARY_PATH on linux to your part of the libs folder.
The code attempts to use [load](https://stackoverflow.com/questions/2937406/how-to-bundle-a-native-library-and-a-jni-library-inside-a-jar) automatically and usually works.

How to use h5jan from a Gradle Build
====================================

Build
-----
This guide assumes that you are familiar with gradle and have a build.gradle file or other named gradle where you would like to add a dependency into your build.

Steps
-----
Still working publishing to maven central. If it does not work then binaries are on github release.

1. Make sure  
~~~~ 
mavenCentral()
~~~~  
is in your repositories{} block


2. Add dependency 
Gradle
~~~~ 
compile "io.github.h5jan:h5jan-core:0.4.3" 
~~~~ 

Maven
```xml
<dependency>
	<groupId>io.github.h5jan</groupId>
	<artifactId>h5jan-core</artifactId>
	<version>0.4.3</version>
</dependency>
```


When you rebuild now january will be available to your gradle project.

## Setup
To run the Java build you will need Java8 or 9. Developers on the project
currently use 9 to develop with an 8 minimum source code version.

## Code Format Guidelines
1.	Follow the code format guidelines
2.	Do not reformat where you are not the primary author/committer without the author's agreement (including auto formatting).
	a.	Unless the primary author/committer has left the organisation
	b.	Even if you think they are not following the guidelines
	c. 	Do not mix up formatting changes which functional changes, there are hard to review reliably.
The @author tag specifies the primary author/committer with whom you agree if auto formatting.


Build
-----

Run `gradlew` (`gradlew.bat` on Windows) from the root directory.

        $ git clone git@github.com:h5jan/h5jan-core.git
        $ cd h5jan-core
        $ ./gradlew
        :compileJava UP-TO-DATE
        :processResources UP-TO-DATE
        :classes UP-TO-DATE
        :jar UP-TO-DATE

        BUILD SUCCESSFUL

If the test coverage is not at least 60%, this project will fail to build.


Proxy:
If you get something like:

        $ ./gradlew
        Downloading https://services.gradle.org/distributions/gradle-2.10-bin.zip

        Exception in thread "main" java.net.UnknownHostException: services.gradle.org

You need to properly configure your machine with the HAL proxy.

You can create a file at %USERPROFILE%\.gradle\gradle.properties.  Inside this file add:

        systemProp.http.proxyHost=<your proxy host>
        systemProp.http.proxyPort=80
        systemProp.https.proxyHost=<your proxy host>
        systemProp.https.proxyPort=80

Idea setup:

        $ ./gradlew idea

* Launch Idea
* File -> Open...
* Navigate to data-connector directory and select `data-connector.ipr`
* Build -> Rebuild project

Eclipse setup:

        $ ./gradlew eclipse

* Launch Eclipse
* Close Welcome dialog
* Import as Gradle project or as java project

Project layout
--------------

- `build/` - Build artifacts
- `build/reports` - Source analysis reports
- `src`- Source code
- `doc`- Documentation
- `src/main/` - Shipped code
- `src/test/` - Test Non Shipped code
- `lib` - Temporarily holds some of the libraries used for communication with DSIS.  Hopefully will be removed in the future.


Running tests
--------------
Right click on project in Eclipse and choose run as junit tests.

Run all tests using:

        $ ./gradlew test

Get more test output:

        $ ./gradlew test -i

One single test using:

        $ ./gradlew test --tests "xx.xx.XXTest"

debug tests on localhost:5005 using:

        $ gradle test --debug-jvm

Generate Test Coverage Report:

        $ ./gradlew test testCoverageReport

Look in build\reports\coverage\ for the generated file.
	

Changing Log Levels in a Running Docker Image
---------------------------------------------

You can change the log levels for an installed Docker image, by using the \_JAVA\_OPTIONS environment variable:
* Edit the Configuration for the Marathon service
* Click on "Environment"
* Click on "ADD ENVIRONMENT VARIABLE"
* Type in "\_JAVA\_OPTIONS" for the key
* Type in "-DHAL\_LOG\_LEVEL=DEBUG" for the value
* Click on "REVIEW & RUN"
* It may be necessary to set more than one level for instance:
 
        -DROOT_LOG_LEVEL=DEBUG -DSTDOUT_LOG_LEVEL=DEBUG -DHAL_LOG_LEVEL=DEBUG -DFILE_LOG_LEVEL=DEBUG
 
## License
See [LICENSE](https://github.com/h5jan/h5jan-core/blob/master/LICENSE.md)
