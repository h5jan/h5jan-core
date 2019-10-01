h5jan
=====

## What is it
h5jan is a Java API for writing an hdf5 pandas dataframe. It is also useful for
saving and loading Eclipse January datasets. There is some connectivity with 
tablesaw tables as well, these may be converted to dataframes and saved to 
pandas h5 format.

Why are these things useful we hear you ask? Well it means that numpy-like data structures can be built in Java and
saved as HDF5. That then means that you can use Java in the middleware or middle
microservice, here it shines with tools like Spring Boot available and its many 
multi-threaded APIs. Then if a parallel execution of python process such as 
machine learning run are required, h5jan allows you to write h5 dataframes to redis
keys for example and have them picked up when the python process runs, for instance
when Kubernetes Jobs are fired off.

How to use h5jan from a Gradle Build
====================================

Build
-----
This guide assumes that you are familiar with gradle and have a build.gradle file or other named gradle where you would like to add a dependency into your build.

Steps
-----
1. Make sure  
~~~~ 
mavenCentral()
~~~~  
is in your repositories{} block


2. Add dependency 
~~~~ 
compile "org.eclipse.january:h5jan-core:0.1.0" 
~~~~ 
to your file.

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

        $ git clone git@git.openearth.community:GeoToolbox/codeanalysis.git
        $ cd codeanalysis
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
 


Running Locally
---------------
The class "Application" contains a main method to run the application.

