#!/bin/sh

echo "==Build Started=="

echo "Installing Mapping Violence Core to maven"

mvn install:install-file \
-Dfile=/usr/share/mappingviolence-database-api/lib/mappingviolence-core.jar \
-DgroupId=org.mappingviolence -DartifactId=core \
-Dversion=0.9.1 \
-Dpackaging=jar

echo "Finished installing Mapping Violence Core"

echo "Deleting previous builds"

mvn clean

echo "Finished deleting previous builds"

echo "Building new project"

mvn install

echo "Finished building new project"

echo "==Build Complete=="
