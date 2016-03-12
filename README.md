Datumbox Machine Learning Framework
===================================

[![Datumbox](http://www.datumbox.com/img/logo.png)](http://www.datumbox.com/)

The Datumbox Machine Learning Framework is an open-source framework written in Java which allows the rapid development Machine Learning and Statistical applications. The main focus of the framework is to include a large number of machine learning algorithms & statistical tests and being able to handle medium-large sized datasets. 

Copyright & License
-------------------

Copyright (C) 2013-2016 [Vasilis Vryniotis](http://blog.datumbox.com/author/bbriniotis/). 

The code is licensed under the [Apache License, Version 2.0](https://github.com/datumbox/datumbox-framework/blob/master/LICENSE).

Version
-------

The latest version is 0.7.0-SNAPSHOT (Build 20160310). 

The [master branch](https://github.com/datumbox/datumbox-framework/tree/master) is the latest stable version of the framework. The [devel branch](https://github.com/datumbox/datumbox-framework/tree/devel) is the development branch. All the previous stable versions are marked with [tags](https://github.com/datumbox/datumbox-framework/releases).

The releases of the framework follow the [Semantic Versioning](http://semver.org/) approach. For detailed information about the various releases check out the [Changelog](https://github.com/datumbox/datumbox-framework/blob/master/CHANGELOG.md).

Installation
------------

Datumbox Framework is available on Maven Central Repository. 

Maven:
```
    <dependency>
        <groupId>com.datumbox</groupId>
        <artifactId>datumbox-framework</artifactId>
        <version>0.7.0-SNAPSHOT</version>
    </dependency>
```

Note: A couple of classes which use Linear Programming require installing an external C library called [lpsolve](http://sourceforge.net/projects/lpsolve/). Most users won't use these classes and thus installing the binary library can be considered optional; please check the [Detailed Installation Guide](http://blog.datumbox.com/how-to-install-and-use-the-datumbox-machine-learning-framework/) for more info.

Documentation and Code Examples
-------------------------------

All the public methods and classes of the Framework are documented with Javadoc comments. Moreover for every model there is a JUnit Test which clearly shows how to train and use the models. Finally for more examples on how to use the framework checkout the [Code Examples](https://github.com/datumbox/datumbox-framework-examples/) or the [official Blog](http://blog.datumbox.com/).

Technical Details
-----------------

The core part of the project is about 30000 lines of code, it uses Java 8 features and a Maven Project Structure. If you find a bug or decide to document particular parts of the code, please consider contributing your changes by sending a pull request. 

Which methods/algorithms are supported?
---------------------------------------

The Framework currently supports performing multiple Parametric & non-parametric Statistical tests, calculating descriptive statistics on censored & uncensored data, performing ANOVA, Cluster Analysis, Dimension Reduction, Regression Analysis, Timeseries Analysis, Sampling and calculation of probabilities from the most common discrete and continues Distributions. In addition it provides several implemented algorithms including Max Entropy, Naive Bayes, SVM, Bootstrap Aggregating, Adaboost, Kmeans, Hierarchical Clustering, Dirichlet Process Mixture Models, Softmax Regression, Ordinal Regression, Linear Regression, Stepwise Regression, PCA and several other techniques that can be used for feature selection, ensemble learning, linear programming solving and recommender systems.

Bug Reports
-----------

Despite the fact that parts of the Framework have been used in commercial applications, not all classes are equally used/tested. Currently the framework is in Beta version, so you should expect some changes on the public APIs on future versions. If you spot a bug please [submit it as an Issue](https://github.com/datumbox/datumbox-framework/issues) on the official Github repository. 

Contributing
------------

The Framework can be improved in many ways and as a result any contribution is welcome. By far the most important part missing from the Framework is multithread support and the ability of using the framework from command line. Other important enhancements include improving the documentation, the test coverage and the examples, improving the architecture of the framework and supporting more Machine Learning and Statistical Models. Please consider contributing if you want to keep this project alive. 

Acknowledgements
----------------

Many thanks to [Eleftherios Bampaletakis](http://gr.linkedin.com/pub/eleftherios-bampaletakis/39/875/551) for his invaluable input on improving the architecture of the Framework. Also many thanks to ej-technologies GmbH for providing a license for their [Java Profiler](http://www.ej-technologies.com/products/jprofiler/overview.html) and to JetBrains for providing a license for their [Java IDE](https://www.jetbrains.com/idea/).

Useful Links
------------

- [Code Examples](https://github.com/datumbox/datumbox-framework-examples/)
- [Project Description](http://blog.datumbox.com/new-open-source-machine-learning-framework-written-in-java/)
- [Datumbox.com](http://www.datumbox.com/)
- [Machine Learning Blog](http://blog.datumbox.com/)

