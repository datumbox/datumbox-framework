CHANGELOG
=========

Version 0.7.0-SNAPSHOT - Build 20160117
---------------------------------------

- Rename the erase() method to delete() in all interfaces.
- None of the ML methods relies on recordIds for any calculation. Even the algorithms that use Matrixes are now patched.
- Major refactoring of the Dataset class:
    - It now implements the Collection<Record> interface and it is renamed as Dataframe.
    - It allows remove operations.
    - It always stores xDataTypes in memory.
    - It stores in memory an index LinkedList which is used to return Records in insertion order.
    - Renamed methods:
        - getRecordNumber() -> size()
        - extractXColumnValues() -> getXColumn()
        - extractYValues() -> getYColumn()
        - removeColumns() -> dropXColumns()
        - getVariableNumber() -> xColumnSize()
        - erase() -> delete()
        - generateNewSubset() -> getSubset()
        - _set() -> _unsafe_set()
    - Removed methods:
        - extractXColumnValuesByY()
    - New methods:
        - addRecord()
        - indexOf()
        - index()
        - values()
        - entries()
        - Plus all others required for the Collection interface.
    - Modified methods:
        - iterator() no longer returns Integers but Records.
        - add() no longer returns the ID of the record but boolean. To get the ID use addRecord().
- Fixing a bug on Adaboost which resulted in mapping incorrectly the recordIds.
- Added serialVersionUID in every serializable class.
- Modified the algorithms that require Matrixes to use the sparse OpenMapRealMatrix instead of BlockRealMatrix.
- Improved Exceptions and Exception messages.
- Add support of Treemaps in the DatabaseConnector. Dataframe stores the records in TreeMap.
- Use _unsafe_set() where possible to speed up the calculations.
- BaseMLrecommender now inherits from BaseMLmodel.
- CollaborativeFiltering algorithm modified to support more generic User-user CF and to return Validation Metrics.
- Tests will now fail if the external datasets can't be downloaded.
- Changed the Validation Metrics policy to exclude from macro averages the classes that did not exist in validation set.
- Implemented StorageHints on the DatabaseConnectors and MapDB can use a hybrid approach of storing some data IN_MEMORY and some IN_DISK. Also support IN_CACHE for LRU cached maps.
- The AutoCloseable interface is implemented in all close() methods.
- Remove duplicate Javadocs comments.
- Changed the way that single vars are stored with DatabaseConnectors. Multiple vars can now be stored. Also the save() and load() methods were renamed to saveObject() and loadObject().
- Changed names of constants and methods that were not compatible with Java's naming conventions.
- Changed the behaviour and the public methods of the DatabaseConnector interface. The dropDatabase() is replaced with a clear() method that deletes all the data but does not close the connection with the database.
- KnowledgeBase is no longer serializable. Its serializable fields are stored individually into the Database.
- Restructuring the framework to remove all FindBug warnings.
- KnowledgeBase is now an interface, while the implementation moved to StandardKnowledgeBase. The interface contains a static factory method to produce any KB. This enable us to define the knowledgeBase field of BaseTrainable private and final.
- All methods that have copy() now implement the Copyable interface.
- Added the multi-thread support on the predict method of most ML models.
- Updated the MapDBConnector to call compact on primary db before close.
- Updated the DatabaseConnector.getBigMap() support thread-safe maps.
- Simplify the architecture. Massive restructuring of packages and class names. Change the structure of Interfaces and inheritance in TP, MP, VM and Algorithms.
- On Dataframe we now can set records on keys that do not already exist.
- The NgramsExtractor was rewritten to remove its internal state.
- Added a concurrency package on common with useful helper classes.
- Added threads on the Builder of Dataframes, on the DataTransformation and FeatureSelection algorithms (both fit and transform).
- Converted the big test Datasets to files stored as resources in the project. This includes all the files that we downloaded from the web.
- Added the TrainParallelizable for the models that use parallelism during training.
- Updated all Wrappers to inherit from Parallelizable and set the parallelized flags of the individual components before training and predict/transform.
- Modified Dataframe to be usable without a synchronized block.
- Created a basic feature switching component for the framework.
- Removed the DOUBLE_ACCURACY_LOW property from the tests since it is no longer used and increased the accuracy of DOUBLE_ACCURACY_MEDIUM.
- Added a skip/limit option on the Dataframe.Builder.parseCSVFile().
- Added multithreading support on the training for many ML models.
- Fixed the limitation on the clustering algorithms which forced us to store the clusters in memory.
- Forced the headerDataTypes parameter of Dataframe.Builder.parseCSVFile() to be a LinkedHashMap.
- Added custom thread pool in the parallel stream execution. Created the ForkJoinStream class and enhanced the ThreadMethods class.
- Created a new Configuration mechanism and added the ability to configure the concurrency.
- Updated the NgramsExtractor algorithm to export more keywords.
- Removed the transacted option of MapDB.
- The configuration of Tests is now controlled using a properties file.

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

