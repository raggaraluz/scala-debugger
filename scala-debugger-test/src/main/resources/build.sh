#!/bin/sh

BASE_DIR=${1-.}
CLASS_DIR=${BASE_DIR}/classes

echo "Using ${BASE_DIR} as base directory to build resources"

# Compile the source files
sbt compile

# Create the jar
jar -cvf TestJar.jar ${CLASS_DIR}/org/senkbeil/debugger/test/jar/*.class

