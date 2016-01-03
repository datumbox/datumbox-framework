CHANGELOG
=========

Version 0.7.0-SNAPSHOT - Build 20160103
---------------------------------------

- Rename the erase() method to delete() in all interfaces.
- Non of the ML methods relies on recordIds for any calculation. Even the algorithms that use Matrixes are now patched.
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
    - Removed methods:
        - extractXColumnValuesByY()
    - New methods:
        - addRecord()
        - indexOf()
        - index()
        - values()
        - Plus all others required for the Collection interface.
    - Modified methods:
        - iterator() no longer returns Integers but Records.
        - add() no longer returns the ID of the record but boolean. To get the ID use addRecord().
- Fixing a bug on Adaboost which resulted in mapping incorrectly the recordIds.
- Added serialVersionUID in every serializable class.
- Modified the algorithms that require Matrixes to use the sparse OpenMapRealMatrix instead of BlockRealMatrix.
- Improved Exceptions and Exception messages.

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

