CHANGELOG
=========

Version 0.8.2-SNAPSHOT - Build 20191016
---------------------------------------

- Bug Fixes:
    - Resolved an issue on ShapiroWilk which led to the incorrect estimation of the p-value.
- Dependencies:
    - Java: The framework is now compiled with Java 11.
    - Build Plugins: Updated Maven Compiler, Maven Javadoc, Maven Source, Maven JAR and Surefire to the latest stable version.
	- Libraries: Updated Commons CSV, SLF4J and LIBSVM to the latest stable official versions.

Version 0.8.1 - Build 20170831
------------------------------

- Dependencies:
	- Updated the Maven Compiler, Nexus Staging, Surefire, SLF4J and Logback Classic plugins to the latest stable versions. 
- Code Improvements & Bug Fixes:
	- FlatDataColletion:
		- The copyCollection2DoubleArray() and copyCollection2Array() methods have been removed.
		- It now implements the Collection Interface instead of the Iterable.
	- Descriptives:
		- New count() method returns the number of non-null elements.
		- All methods can now handle null values. Null values are considered missing and they are ignored from the calculations [#23](https://github.com/datumbox/datumbox-framework/issues/23).
	- TextClassifier:
		- The pipeline steps of the Text Classifier change to Feature Selection, Numerical Standardization and Modeling [#24](https://github.com/datumbox/datumbox-framework/issues/24).
		- The Categorical Encoding step is no longer executed as the Text Extractor already encodes the words as numeric values.

Version 0.8.0 - Build 20170114
------------------------------

- Storage/Persistence:
    - The old database connectors are replaced with a new storage mechanism where every engine is a separate module.
    - The InMemory engine now stores objects independently and keeps track of their references in a catalog.
    - The MapDB engine stores objects in directories and makes the asynchronous writes configurable.
    - The models are no longer persisted automatically after the end of fit(). Instead we have full control over how/when they are stored using the save method.
    - The Dataframes can now be persisted using the save and load methods.
    - All the file-based engines persist models using a hierarchical directory structure, making the sharing of models easier.
- Speed & Memory:
    - All the algorithms that use matrices now support disk-based training.
    - Improved the serialization speed by passing Key/Value information to BigMaps.
    - Faster and more memory efficient methods for Combinations and Arithmetics classes.
    - Addition of the FixedBatchSpliterator class in the concurrency package.
    - Speed optimizations on DPMM algorithms.
- Code Improvements & New Algorithms:
    - New Model Selection package:
        - Added a new metrics submodule with the most important validation metrics for: classification, clustering, regression and recommendation.
        - Added a new splitter submodule that partitions the data into chunks: K-fold & Shuffle splits are supported.
        - Model selection is now performed by combining the splitters and metrics packages with the Validator class.
        - The ValidationMetrics are no longer stored inside the model and k-Fold validation is no longer part of model's methods.
    - New Preprocessing package:
        - The old data transformation package is replaced with a new that decouples numerical scaling from categorical encoding.
        - The following numeric scalers are now supported: MinMaxScaler, StandardScaler, MaxAbsScaler and BinaryScaler.
        - The following categorical encoders are now supported: OneHotEncoder and CornerConstraintsEncoder.
    - Improved Feature Selection package:
        - All feature selection algorithms focus on specific datatypes, making it possible to chain different methods together.
        - Simplified the inheritance of feature selection algorithms.
    - Improved Code Architecture:
        - The common module is now lighter and more reusable. Some of its classes & interfaces were moved to the core module.
        - The Storage mechanism is split into separate modules, enabling the support of new storage engines.
        - The new Tests module allows reusing Test Base classes and configuration files. Moreover using CI, we now test the code using different configurations (one for each storage engine) across all the popular operating systems.
    - Usability:
        - All the algorithms are initialized/loaded using the MLBuilder.
        - The Training Parameters are now provided during the initialization of the algorithms rather than using a setter.
        - The recommendersystem package is renamed to recommendation.
        - The Modeler now receives a list of feature selector parameters, allowing chaining methods together.
        - TextClassifier inherits directly from Modeler.
        - The Configuration objects use separate properties files.
        - Refactored and removed duplicate code, improved naming of packages, classes and configuration entries.
- Dependencies:
    - Reduced the number of dependencies:
        - The lp_solve is removed in favour of a pure Java simplex solver. As a result datumbox does not depend on any system libraries.
        - The commons-lang which was used for HTML parsing is replaced with a faster custom HTMLParser implementation.
    - The libsvm, commons-math, commons-csv, slf4j and logback-classic are updated to the latest stable versions.

Version 0.7.0 - Build 20160319
------------------------------

- Speed & Memory:
	- Added multi-threading support on the majority of algorithms and methods, making the 0.7.0 version several times faster than 0.6.x.
	- Implemented Storage Hints & hybrid strategies which enable the efficient use of LRU cache and faster training for large datasets that don't fit in memory.
	- All the algorithms which require Matrixes now use sparse implementations to reduce the memory footprint.
	- Fixed a limitation on clustering algorithms which forced us to store the list of clusters in memory.
- Algorithms & Methods:
	- Added L1, L2 and ElasticNet regularization in Logistic, Ordinal and Linear Regression algorithms.
	- The Collaborative Filtering algorithm was modified to support more generic User-user CF models.	
	- Updated the NgramsExtractor algorithm to export more keywords and provide better signals for NLP models.
- Framework Architecture: 
	- The framework is now split to separate modules and the main library is renamed to "datumbox-framework-lib".
	- The Dataset class is replaced with the Dataframe class, which implements the Collection interface and enables the processing of the records in parallel. 
	- Major changes on the structure of Interfaces and inheritance to simplify the architecture.
	- BaseMLrecommender now inherits from BaseMLmodel.
- Code Improvements & Bug Fixes:
	- Added serialVersionUID in every serializable class.
	- Improved Exceptions and error messages.
	- Fixed a bug on Adaboost which resulted in mapping incorrectly the recordIds.
	- Improved documentation and javadocs comments.
	- Increased the test-coverage.

Version 0.6.1 - Build 20160102
------------------------------

- Fixed a minor issue related to Unreleased Resources on tests.
- Resolved a memory leak on the AutoCloseConnector class. The shutdown hooks are now removed when close() is called.
- Updated the TypeInference class to reduce memory consumption.
- Refactored the TextClassifier class and improved its speed.
- Updated all dependencies and maven plugins to the latest stable versions.

Version 0.6.0 - Build 20150502
------------------------------

- Added support of [MapDB](http://www.mapdb.org/) database engine.
- Removed MongoDB support due to performance issues.
- Reduced the level of abstraction and simplify framework's architecture.
- Rewrote the persistence mechanisms, remove unnecessary data structures and features that increased the complexity.
- Changed the public methods of the Machine Learning models to resemble Python's Scikit-Learn APIs.
- Added convenience methods to build a dataset from CSV or text files.
- Enhanced the documentation and Javadoc comments of the Framework.
- Changed the software License from "GNU General Public License v3.0" to "Apache License, Version 2.0".

Version 0.5.1 - Build 20141105
------------------------------

- Updated the pom.xml file.
- Submitted the Datumbox Framework and the LPSolve library in Maven Central Repository.
- Resolved issues [#1](https://github.com/datumbox/datumbox-framework/issues/1), [#2](https://github.com/datumbox/datumbox-framework/issues/2) and [#4](https://github.com/datumbox/datumbox-framework/issues/4). All the dependencies are now on Maven.

Version 0.5.0 - Build 20141018
------------------------------

- First open-source version of Datumbox Framework.

