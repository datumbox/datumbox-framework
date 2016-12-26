Datumbox Machine Learning Framework
===================================
[![Build Status](https://api.travis-ci.org/datumbox/datumbox-framework.svg)](https://travis-ci.org/datumbox/datumbox-framework) [![Windows Build status](https://ci.appveyor.com/api/projects/status/2aqkak8kmt8ooj4i?svg=true)](https://ci.appveyor.com/project/datumbox/datumbox-framework) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.datumbox/datumbox-framework-lib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.datumbox/datumbox-framework-lib) [![License](https://img.shields.io/:license-apache-brightgreen.svg)](./LICENSE)

[![Datumbox](http://www.datumbox.com/img/logo.png)](http://www.datumbox.com/)

The Datumbox Machine Learning Framework is an open-source framework written in Java which allows the rapid development Machine Learning and Statistical applications. The main focus of the framework is to include a large number of machine learning algorithms & statistical methods and to be able to handle large sized datasets. 

Copyright & License
-------------------

Copyright (C) 2013-2016 [Vasilis Vryniotis](http://blog.datumbox.com/author/bbriniotis/). 

The code is licensed under the [Apache License, Version 2.0](./LICENSE).

Version
-------

The latest version is 0.8.0-SNAPSHOT (Build 20161226).

The [devel branch](https://github.com/datumbox/datumbox-framework/tree/devel) is the development branch (default github branch). The [master branch](https://github.com/datumbox/datumbox-framework/tree/master) contains the latest stable version of the framework. All the stable releases are marked with [tags](https://github.com/datumbox/datumbox-framework/releases).

The releases of the framework follow the [Semantic Versioning](http://semver.org/) approach. For detailed information about the various releases check out the [Changelog](./CHANGELOG.md).

Installation
------------

Datumbox Framework is available on [Maven Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.datumbox%22%20AND%20a%3A%22datumbox-framework-lib%22). 

Maven:
```
    <dependency>
        <groupId>com.datumbox</groupId>
        <artifactId>datumbox-framework-lib</artifactId>
        <version>0.8.0-SNAPSHOT</version>
    </dependency>
```

Documentation and Code Examples
-------------------------------

All the public methods and classes of the Framework are documented with Javadoc comments. Moreover for every model there is a JUnit Test which clearly shows how to train and use the models. Finally for more examples on how to use the framework checkout the [Code Examples](https://github.com/datumbox/datumbox-framework-examples/) or the [official Blog](http://blog.datumbox.com/).

Technical Details
-----------------

The framework requires Java 8, it uses a Maven Project Structure and it is separated in different modules. 

Which methods/algorithms are supported?
---------------------------------------

The Framework currently supports performing multiple Parametric & non-parametric Statistical tests, calculating descriptive statistics on censored & uncensored data, performing ANOVA, Cluster Analysis, Dimension Reduction, Regression Analysis, Timeseries Analysis, Sampling and calculation of probabilities from the most common discrete and continues Distributions. In addition it provides several implemented algorithms including Max Entropy, Naive Bayes, SVM, Bootstrap Aggregating, Adaboost, Kmeans, Hierarchical Clustering, Dirichlet Process Mixture Models, Softmax Regression, Ordinal Regression, Linear Regression, Stepwise Regression, PCA and several other techniques that can be used for feature selection, ensemble learning, linear programming solving and recommender systems.

Bug Reports
-----------

Despite the fact that parts of the Framework have been used in commercial applications, not all classes are equally used/tested. Currently the framework is in Alpha version, so you should expect some changes on the public APIs on future versions. If you spot a bug please [submit it as an Issue](https://github.com/datumbox/datumbox-framework/issues) on the official Github repository. 

Contributing
------------

The Framework can be improved in many ways and as a result any contribution is welcome. By far the most important feature missing from the Framework is the ability to use it from command line or from other languages such as Python. Other important enhancements include improving the documentation, the test coverage and the examples, improving the architecture of the framework and supporting more Machine Learning and Statistical Models. If you make any useful changes on the code, please consider contributing them by sending a pull request.

Acknowledgements
----------------

Many thanks to [Eleftherios Bampaletakis](http://gr.linkedin.com/pub/eleftherios-bampaletakis/39/875/551) for his invaluable input on improving the architecture of the Framework. Also many thanks to ej-technologies GmbH for providing a license for their [Java Profiler](http://www.ej-technologies.com/products/jprofiler/overview.html) and to JetBrains for providing a license for their [Java IDE](https://www.jetbrains.com/idea/).

Useful Links
------------

- [Code Examples](https://github.com/datumbox/datumbox-framework-examples/)
- [Project Description](http://blog.datumbox.com/new-open-source-machine-learning-framework-written-in-java/)
- [Datumbox.com](http://www.datumbox.com/)
- [Machine Learning Blog](http://blog.datumbox.com/)

