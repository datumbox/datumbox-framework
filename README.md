Datumbox Machine Learning Framework
===================================

[![Datumbox](http://www.datumbox.com/img/logo.png)](http://www.datumbox.com/)

The Datumbox Machine Learning Framework is an open-source framework written in Java which allows the rapid development Machine Learning and Statistical applications. The main focus of the framework is to include a large number of machine learning algorithms & statistical tests and being able to handle medium-large sized datasets. 

Author: [Vasilis Vryniotis](http://blog.datumbox.com/author/bbriniotis/)

Version: 1.0.0

Build: 20150415

Installation
------------

Datumbox Framework is available on Maven Central Repository. 

Maven:
```
    <dependency>
        <groupId>com.datumbox</groupId>
        <artifactId>datumbox-framework</artifactId>
        <version>1.0.0</version>
    </dependency>
```

Note: A couple of classes which use Linear Programming require installing an external C library called [lpsolve](http://sourceforge.net/projects/lpsolve/). Most users won't use these classes and thus installing the binary library can be considered optional; please check the [Detailed Installation Guide](http://blog.datumbox.com/how-to-install-and-use-the-datumbox-machine-learning-framework/) for more info.

Technical Details
-----------------

The core part of the project is about 30000 lines of code, it uses Java 8 features and a Maven Project Structure. If you find a bug or decide to document particular parts of the code, please consider contributing your changes by sending a pull request. 

Which methods/algorithms are supported?
---------------------------------------

The Framework currently supports performing multiple Parametric & non-parametric Statistical tests, calculating descriptive statistics on censored & uncensored data, performing ANOVA, Cluster Analysis, Dimension Reduction, Regression Analysis, Timeseries Analysis, Sampling and calculation of probabilities from the most common discrete and continues Distributions. In addition it provides several implemented algorithms including Max Entropy, Naive Bayes, SVM, Bootstrap Aggregating, Adaboost, Kmeans, Hierarchical Clustering, Dirichlet Process Mixture Models, Softmax Regression, Ordinal Regression, Linear Regression, Stepwise Regression, PCA and several other techniques that can be used for feature selection, ensemble learning, linear programming solving and recommender systems.

License
-------

The code is licensed under the [Apache License, Version 2.0](https://github.com/datumbox/datumbox-framework/blob/master/LICENSE).

Documentation
-------------

At the moment the Framework has a very poor documentation. Fortunately for every model there is a JUnit Test which clearly shows how to train and use the models. If you use the framework or part of it, please consider commenting the methods that you used and contributing your changes by sending a pull request. 

Bug Reports
-----------

Despite the fact that parts of the Framework have been used in commercial applications, not all classes are equally used/tested. Currently the framework is in Beta version, so you should expect drastic changes on the future version. If you spot a bug please [submit it as an Issue](https://github.com/datumbox/datumbox-framework/issues) on the official Github repository. 

Contributing
------------

By far the most important part missing from the Framework is the Documentation and comments of the code. Other important enhancements include adding Thread support (where possible), improving the architecture of the framework and supporting more Machine Learning and Statistical Models. Please consider contributing if you want to keep this project alive. 

Acknowledgements
----------------

Many thanks to [Eleftherios Bampaletakis](http://gr.linkedin.com/pub/eleftherios-bampaletakis/39/875/551) for his invaluable input on improving the architecture of the Framework. Also many thanks to ej-technologies GmbH for providing us with a license for their [Java Profiler](http://www.ej-technologies.com/products/jprofiler/overview.html).

Useful Links
------------

Project Description: http://blog.datumbox.com/new-open-source-machine-learning-framework-written-in-java/

Datumbox: http://www.datumbox.com/

Machine Learning Blog: http://blog.datumbox.com/
