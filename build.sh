#!/bin/bash

mvn clean package gpg:sign
cd target
jar -cvf bundle.jar datumbox-framework-*.pom datumbox-framework-*.pom.asc datumbox-framework-*.jar datumbox-framework-*.jar.asc datumbox-framework-*-javadoc.jar datumbox-framework-*-javadoc.jar.asc datumbox-framework-*-sources.jar datumbox-framework-*-sources.jar.asc
