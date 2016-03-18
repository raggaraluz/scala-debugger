Scala Debugger
==============

[![Build Status](http://fommil.com/api/badges/ensime/scala-debugger/status.svg)](http://fommil.com/ensime/scala-debugger)
[![Scaladoc 2.10](https://img.shields.io/badge/Scaladoc-2.10-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.10)
[![Scaladoc 2.11](https://img.shields.io/badge/Scaladoc-2.11-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.11)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ensime/scala-debugger)

A simple debugger library for Scala. Tested on OpenJDK 7.
- Visit the [main site](https://scala-debugger.org/) for more documentation.
- Check out the [wiki](https://github.com/ensime/scala-debugger/wiki) for the current roadmap.

Installing with SBT
-------------------

Hosted on Maven Central and can be installed via the following:

```scala
libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.0.0"
```
    
You also need to install the sbt plugin to make the Java Debugger Interface jar (tools.jar) available
on your classpath when you are compiling and testing:

```scala
addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")
```

License
-------

The Scala debugger API is licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

