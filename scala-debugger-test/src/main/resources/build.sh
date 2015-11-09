#!/bin/sh

BASE_DIR=${1-.}

echo "Using ${BASE_DIR} as base directory to build resources"

# Compile the source files
scalac ${BASE_DIR}/org/senkbeil/debugger/test/jar/*.scala

# Create the jar
jar -cvf TestJar.jar ${BASE_DIR}/org/senkbeil/debugger/test/jar/*.class

