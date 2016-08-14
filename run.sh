#!/bin/sh

TARGET=$(ls target/*.jar 2>/dev/null)
if [ -z "$TARGET" ]; then
    echo "No jar file in target/, run build.sh first"
    exit 1;
fi

if [ ! .mvn-classpath -nt pom.xml ]; then
  mvn dependency:build-classpath -Dmdep.outputFile=.mvn-classpath -q
fi

CP=$(cat .mvn-classpath)

if [ ! -e ./config/package.txt -o -z $(cat ./config/package.txt 2>/dev/null) ]; then
     echo "No package definition found. Make sure the package of" \
          "runnable class is provided in ./config/package.txt"
     exit 1;
fi

PACKAGE=$(cat ./config/package.txt)

if [ ! -e ./config/runnable_class.txt -o -z $(cat ./config/runnable_class.txt 2>/dev/null) ]; then
    echo "No runnable class found. Using Main"
    MAIN="Main"
else
    MAIN=$(cat ./config/runnable_class.txt)
fi

PROJECT=$(basename $(pwd))

java -cp $TARGET:$CP $PACKAGE.$MAIN "$@"
