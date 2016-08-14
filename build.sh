#!/bin/sh

echo "Starting build"

echo "Deleting previous builds"

mvn clean

echo "Cleaning finished"

echo "Building new project"

mvn install

echo "Finished building new project"
