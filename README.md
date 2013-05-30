Z-Java
======

Simple Java quickstart for playing with the Zuora API and simplify your java project(s).


Overview
--------

This is a sample project to build the stub classes for using the Zuora SOAP API. Based on the WSDL
downloaded from your Zuora tenant, you can then generate a JAR file to include in your projects.


Installation
------------

First, clone the github repository on your computer

    git clone https://github.com/mpham-zuora/z-java.git
    cd z-java

Copy the sample properties files and edit it to add your credentials

    cp src/main/resources/config.sample.properties src/main/resources/config.properties

Build the JAR file

    mvn package


Using a custom/different WSDL
-----------------------------

1. Add your WSDL to the `src/main/wsdl` folder
2. Edit the `pom.xml` file `line 123` and change the value for your WSDL name
3. `mvn package`


Usage
-----

    ZApi zapi = new ZApi();

    // This should be called before any other call
    zapi.zLogin();

    // Example on how to do a query
    QueryResult result = zapi.zQuery("SELECT AccountNumber, Name FROM Account");

