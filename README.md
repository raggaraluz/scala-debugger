Scala Debugger
==============

A simple, naive standalone debugger for Scala. Goal is to connect to Apache
Spark executors to examine the code as it is executed.

Potential Gotchas
-----------------

- When moving from Mac OS X's IntelliJ to the IntelliJ of Linux Mint, the
  tools.jar of OpenJDK/Oracle JDK was not picked up. I needed to manually open
  up the SDK being used and add the tools.jar of 
  `/usr/lib/jvm/java-7-openjdk-amd64/lib/` to the classpath. This allows me to
  use the Sun-based `com.sun.jdi` package for a Java debugger interface rather
  than C++.

