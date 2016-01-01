CHANGELOG
=========

Version 0.6.1 - Build 20151231
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

