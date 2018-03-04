# sirh-kiosque-j2ee

[![Build Status](https://travis-ci.org/DSI-Ville-Noumea/sirh-kiosque-j2ee.svg?branch=master)](https://travis-ci.org/DSI-Ville-Noumea/sirh-kiosque-j2ee) 
[![Coverage Status](https://coveralls.io/repos/github/DSI-Ville-Noumea/sirh-kiosque-j2ee/badge.svg)](https://coveralls.io/github/DSI-Ville-Noumea/sirh-kiosque-j2ee)


#### Installation

Ajout des dépendances dans le repo maven local.


```
mvn install:install-file -DgroupId=com.PDFjet -DartifactId=PDFjet -Dversion=1.0 -Dfile=./lib/PDFjet-1.0.jar -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -DgroupId=com.javaplanner -DartifactId=javaplanner -Dversion=1.5 -Dfile=./lib/javaplanner-1.5.jar -Dpackaging=jar -DgeneratePom=true
```
