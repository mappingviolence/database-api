#!/bin/sh

echo "==Build Started=="

echo "Deleting previous builds"

mvn clean

echo "Finished deleting previous builds"

echo "Building new project"

mvn install

echo "Finished building new project"

echo "==Build Complete=="
