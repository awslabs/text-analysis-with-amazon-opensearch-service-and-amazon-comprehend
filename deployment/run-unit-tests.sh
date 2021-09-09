#!/bin/bash

# This script should be run from the repo's deployment directory
# cd deployment
# ./run-unit-tests.sh

# Run unit tests
echo "Running unit tests"
echo "cd ../source"
export deployment_dir=`pwd`
mvn -f $deployment_dir/../source/pom.xml test
echo "Completed unit tests"
