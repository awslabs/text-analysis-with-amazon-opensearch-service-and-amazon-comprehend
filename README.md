# Analyzing Text with Amazon Elasticsearch Service and Amazon Comprehend
The package contains the cloudFormation template and implementation source code for AWS solution - analyzing text with Amazon Elasticsearch Service and Amazon Comprehend

For more detail about deploying solution, please refer to solution [deployment guide page](https://aws.amazon.com/solutions/analyzing-text-with-amazon-elasticsearch-service-and-amazon-comprehend/)

## File Structure
```
|-deployment/ [folder containing templates and build scripts]
|-source/
  |-/main [source code]
  |-/test [unit test of the source code]
```

## Getting Started
The following steps instruct you on how to build this solution from source code and run sample integration tests

#### 01. Prerequisites

* [AWS Command Line Interface](https://aws.amazon.com/cli/)
* Create the bucket with following naming convention: {base-bucket-name}-{aws-region}
  * base-bucket-name: any unique name
  * aws-region: us-east-1, us-west-2, etc

#### 02. Clone repository
Clone the analyzing-text-with-amazon-elasticsearch-service-and-amazon-comprehend GitHub repository:

```bash
git clone https://github.com/awslabs/analyzing-text-with-amazon-elasticsearch-service-and-amazon-comprehend.git
```

#### 03. Declare environment variables:
```
export AWS_REGION="us-east-1"
export SOLUTION_NAME="analyzing-text-with-amazon-elasticsearch-service-and-amazon-comprehend"
export VERSION=v1.0
export SOURCE_BUCKET_BASE_NAME=[CHANGE_ME]
export ELASTICSEARCH_DOMAIN_NAME=[CHANGE_ME]
```
- **AWS_REGION**: AWS region code. Ex: ```us-east-1```.
- **VERSION**: version of the package. EX: ```v1.0```.
- **SOURCE_BUCKET_BASE_NAME**: Name for the S3 bucket location where the template will source the Lambda code from. The template will append ```-[aws-region-code]``` to this bucket name. For example: ```./build-s3-dist.sh solutions v1.1.0```, the template will then expect the source code to be located in the ```solutions-[aws-region-code]``` bucket.

#### 04. Build the Comprehend Elasticsearch solution for deployment:
```bash
cd ./${SOLUTION_NAME}/deployment
chmod +x build-s3-dist.sh
./build-s3-dist.sh ${SOURCE_BUCKET_BASE_NAME} ${VERSION}
```

#### 05. Upload deployment assets to your Amazon S3 bucket:
```
aws --region $AWS_REGION s3 cp ./dist s3://$SOURCE_BUCKET_BASE_NAME-$AWS_REGION/$SOLUTION_NAME/$VERSION --recursive
```

## 06. [AWS CLI] Create CloudFormation Stack with default optional parameters
Disable VPC
```bash
aws --region ${AWS_REGION} cloudformation create-stack --stack-name ${SOLUTION_NAME} --template-url https://s3.amazonaws.com/${SOURCE_BUCKET_BASE_NAME}-${AWS_REGION}/${SOLUTION_NAME}/${VERSION}/${SOLUTION_NAME}.template  --parameters ParameterKey=DomainName,ParameterValue=${ELASTICSEARCH_DOMAIN_NAME} ParameterKey=ESServiceRoleExists,ParameterValue=true --capabilities "CAPABILITY_IAM"
```
Enable VPC
```bash
aws --region ${AWS_REGION} cloudformation create-stack --stack-name ${SOLUTION_NAME} --template-url https://s3.amazonaws.com/${SOURCE_BUCKET_BASE_NAME}-${AWS_REGION}/${SOLUTION_NAME}/${VERSION}/${SOLUTION_NAME}.template  --parameters ParameterKey=DomainName,ParameterValue=${ELASTICSEARCH_DOMAIN_NAME} ParameterKey=ESServiceRoleExists,ParameterValue=true ParameterKey=EnableVPC,ParameterValue=true --capabilities "CAPABILITY_IAM"
```

***

Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the MIT-0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    https://spdx.org/licenses/MIT-0.html

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific language governing permissions and limitations under the License.
