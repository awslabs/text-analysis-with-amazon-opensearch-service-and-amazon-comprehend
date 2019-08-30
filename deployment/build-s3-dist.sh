#!/bin/bash

# This assumes all of the OS-level configuration has been completed and git repo has already been cloned
#sudo yum-config-manager --enable epel
#sudo yum update -y
#sudo pip install --upgrade pip
#alias sudo='sudo env PATH=$PATH'
#sudo  pip install --upgrade setuptools
#sudo pip install --upgrade virtualenv

# This script should be run from the repo's deployment directory
# cd deployment
# ./build-s3-dist.sh source-bucket-base-name version-code
#
# Paramenters:
#  - source-bucket-base-name: Name for the S3 bucket location where the template will source the Lambda
#    code from. The template will append '-[region_name]' to this bucket name.
#    For example: ./build-s3-dist.sh solutions v1.0
#    The template will then expect the source code to be located in the solutions-[region_name] bucket
#
#  - version-code: version of the package

# Check to see if input has been provided:
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Please provide the base source bucket name and version where the lambda code will eventually reside."
    echo "For example: ./build-s3-dist.sh solutions v1.0.0"
    exit 1
fi

SOLUTION_NAME="analyzing-text-with-amazon-elasticsearch-service-and-amazon-comprehend"

# Build source
echo "Staring to build distribution"
# Create variable for deployment directory to use as a reference for builds
echo "export deployment_dir=`pwd`"
export deployment_dir=`pwd`

# Make deployment/dist folder for containing the built solution
echo "mkdir -p $deployment_dir/dist"
mkdir -p $deployment_dir/dist

# Copy project CFN template(s) to "dist" folder and replace bucket name with arg $1
echo "cp -f ${SOLUTION_NAME}.template $deployment_dir/dist"
cp -f ${SOLUTION_NAME}.template $deployment_dir/dist
echo "Updating code source bucket in template with $1"
replace="s/%%BUCKET_NAME%%/$1/g"
echo "sed -i '' -e $replace $deployment_dir/dist/${SOLUTION_NAME}.template"
sed -i '' -e $replace $deployment_dir/dist/${SOLUTION_NAME}.template

echo "Updating code source version in template with $2"
replace="s/%%VERSION%%/$2/g"
echo "sed -i '' -e $replace $deployment_dir/dist/${SOLUTION_NAME}.template"
sed -i '' -e $replace $deployment_dir/dist/${SOLUTION_NAME}.template

# Build java CFN Lambda
echo "Building java CFN Lambda function"
mvn -f $deployment_dir/../source/pom.xml clean package

# Copy packaged Lambda function to $deployment_dir/dist
cp $deployment_dir/../source/target/${SOLUTION_NAME}-1.0-SNAPSHOT.jar $deployment_dir/dist/${SOLUTION_NAME}.jar
echo "Clean up build material"
mvn -f $deployment_dir/../source/pom.xml clean
echo "Completed building distribution"
cd $deployment_dir
