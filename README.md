Z-Java
======

Simple Java quickstart for playing with the Zuora API and simplify your java project(s). You can either:

* `git clone` or fork this project and add your customer-specific code to it
* or **better** you can `git clone` this repository then **declare** it as a **dependency** in your other project

Extract from the `pom.xml` file of your other project:

```xml
<dependency>
    <groupId>com.zuora</groupId>
    <artifactId>z-java</artifactId>
    <version>0.2</version>
</dependency>
```

If you find bugs and/or add functionalities, please create a pull request.


Overview
--------

This is a sample project to build the stub classes for using the Zuora SOAP API. Based on the WSDL
downloaded from your Zuora tenant, you can then generate a JAR file to include in your projects and/or
add this whole project as a **Maven Dependency** in your other projects.


Installation
------------

First, clone the github repository on your computer

    git clone https://github.com/abaenglish/z-java.git
    cd z-java


You need create config files with credentials apisandbox in this path:

    src/main/resources/application.yml

Example:

```
    external:
      zuora:
        soap:
          username: APIUser@abaenglish.com
          password: 1QA2ws2ws3ed
          host: apisandbox.zuora.com
```

Build the JAR file

    mvn package


Using a custom/different WSDL
-----------------------------

1. Add your WSDL to the `src/main/wsdl` folder
2. Edit the `pom.xml` file `line 123` and change the value for your WSDL name
3. `mvn package` and that's it!

## WSDL 79.0

This WSDL has changed some attributes in method **Subscription** (PLEASE MANTAIN THIS CHANGES WHEN UPDATE WSDL):
-  **ContractEffectiveDay**  date -> string
-  **ServiceActivationDate**   date -> string
-  **TermStartDate**   date -> string

This WSDL has changed some attributes in method **Amendment** (PLEASE MANTAIN THIS CHANGES WHEN UPDATE WSDL):
-  **ContractEffectiveDate**  date -> string
-  **CustomerAcceptanceDate**   date -> string
-  **CustomerAcceptanceDate**   date -> string
-  **ServiceActivationDate**   date -> string
-  **SpecificUpdateDate**   date -> string
-  **TermStartDate**   date -> string


## ZuoraStubService

If you have problems with this generate code you change IntelliJ IDEA.

How can I change this properties?

You go terminal and write:

```
nano /Applications/IntelliJ\ IDEA.app/Contents/bin/idea.properties
```

and modify:

```
idea.max.intellisense.filesize=2500
```

for:

```
idea.max.intellisense.filesize=250000
```

You have more info in:

[How can I change idea.properties](https://intellij-support.jetbrains.com/hc/en-us/articles/206544869-Configuring-JVM-options-and-platform-properties)


Installing it locally
---------------------

    mvn install


Usage
-----

```java
ZApi zapi = new ZApi();

// This should be called before any other call
zapi.zLogin();

// Example on how to do a query
QueryResult result = zapi.zQuery("SELECT AccountNumber, Name FROM Account");
```

Note(s)
-------

* This works perfectly with the [Heroku Java Quickstart](https://devcenter.heroku.com/articles/getting-started-with-java) as long as you define `z-java` as an [unmanaged dependency](https://devcenter.heroku.com/articles/local-maven-dependencies)
* Unfortunately, `z-java` is **not** working when embedded in an **Android** application, as _Apache Axis 2_ relies on core libraries (`javax.xml.*`) which are **not** part of the Dalvik virtual machine
