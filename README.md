Datumbox Machine Learning Framework
===================================

[![Datumbox](http://www.datumbox.com/img/logo.png)](http://www.datumbox.com/)

The Datumbox Machine Learning Framework is an open-source framework written in Java which allows the rapid development Machine Learning and Statistical applications. The main focus of the framework is to include a large number of machine learning algorithms & statistical tests and being able to handle small-medium sized datasets. 

Author: [Vasilis Vryniotis](http://blog.datumbox.com/author/bbriniotis/)

Version: 0.5.1 alpha

Build: 20141105

Installation
------------

Datumbox Framework is available on Maven Central Repository. 

Maven:
```
    <dependency>
        <groupId>com.datumbox</groupId>
        <artifactId>datumbox-framework</artifactId>
        <version>0.5.1</version>
    </dependency>
```

Note: A couple of classes which use Linear Programming require installing an external C library called [lpsolve](http://sourceforge.net/projects/lpsolve/). Most users won't use these classes and thus installing the binary library can be considered optional; please check the [Detailed Installation Guide](http://blog.datumbox.com/how-to-install-and-use-the-datumbox-machine-learning-framework/) for more info.

Technical Details
-----------------

The core part of the project is about 30000 lines of code, uses Java 8 features and uses Maven Project Structure. The code is licensed under the GNU General Public License v3.0 so feel free to clone the repository and experiment with it. If you find a bug or decide to document particular parts of the code, please consider contributing your changes by sending a pull request.

What methods/algorithms are supported?
--------------------------------------

The Framework currently supports performing multiple Parametric & non-parametric Statistical tests, calculating descriptive statistics on censored & uncensored data, performing ANOVA, Cluster Analysis, Dimension Reduction, Regression Analysis, Timeseries Analysis, Sampling and calculation of probabilities from the most common discrete and continues Distributions. In addition it provides several implemented algorithms including Max Entropy, Naive Bayes, SVM, Bootstrap Aggregating, Adaboost, Kmeans, Hierarchical Clustering, Dirichlet Process Mixture Models, Softmax Regression, Ordinal Regression, Linear Regression, Stepwise Regression, PCA and several other techniques that can be used for feature selection, ensemble learning, linear programming solving and recommender systems.

Documentation
-------------

At the moment the Framework has a very poor/non-existent documentation. Fortunately for every model there is a JUnit Test which clearly shows how to train and use the models. If you use the framework or part of it, please consider commenting the methods that you used and contributing your changes by sending a pull request. 

Bug Reports
-----------

Despite the fact that parts of the Framework have been used in commercial applications, not all classes are equally used/tested. Currently the framework is in Alpha version, so you should expect drastic changes on the future version. If you spot a bug please submit it as an Issue on the official Github repository. 

Contributing
------------

By far the most important part missing from the Framework is the Documentation and proper commenting of the code. Other important enhancements include the refactoring of the code, improvement of the way that the models are persisted, adding Thread support (where possible) or adding more Models. Please consider contributing if you want to keep this project alive. 

Useful Links
------------

Project Description: http://blog.datumbox.com/new-open-source-machine-learning-framework-written-in-java/

Datumbox: http://www.datumbox.com/

Machine Learning Blog: http://blog.datumbox.com/
