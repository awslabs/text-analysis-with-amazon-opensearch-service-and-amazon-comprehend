# Text Analysis with Amazon OpenSearch Service and Amazon Comprehend
The package contains the CloudFormation template and implementation source code for AWS solution - text analysis with Amazon OpenSearch Service and Amazon Comprehend

For more detail about deploying solution, please refer to solution [deployment guide page](https://aws.amazon.com/solutions/implementations/text-analysis-with-amazon-opensearch-service-and-amazon-comprehend/)

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
* It is recommended to use a randomized bucket name for your base-bucket-name, ensure your bucket is not public, and verify bucket ownership prior to uploading deployment assets to your s3 bucket

#### 02. Clone repository
Clone the text-analysis-with-amazon-opensearch-service-and-amazon-comprehend GitHub repository:

```bash
git clone https://github.com/awslabs/text-analysis-with-amazon-opensearch-service-and-amazon-comprehend.git
```

#### 03. Declare environment variables:
```
export AWS_REGION="us-east-1"
export SOLUTION_NAME="text-analysis-with-amazon-opensearch-service-and-amazon-comprehend"
export VERSION=v1.0.0
export SOURCE_BUCKET_BASE_NAME=[CHANGE_ME]
export OPENSEARCH_DOMAIN_NAME=[CHANGE_ME]
```
- **AWS_REGION**: AWS region code. Ex: ```us-east-1```.
- **VERSION**: version of the package. EX: ```v1.0.0```.
- **SOURCE_BUCKET_BASE_NAME**: Name for the S3 bucket location where the template will source the Lambda code from. The template will append ```-[aws-region-code]``` to this bucket name. For example: ```./build-s3-dist.sh solutions ${SOLUTION_NAME} v1.0.0```, the template will then expect the source code to be located in the ```solutions-[aws-region-code]``` bucket.

#### 04. Build the Comprehend OpenSearchService solution for deployment:
```bash
cd ./${SOLUTION_NAME}/deployment
chmod +x build-s3-dist.sh
./build-s3-dist.sh ${SOURCE_BUCKET_BASE_NAME} ${SOLUTION_NAME} ${VERSION}
```

#### 05. Upload deployment assets to your Amazon S3 bucket:
```
# Deploy the template and source code to an Amazon S3 bucket in your account. Note: you must have the AWS Command Line Interface installed.
aws s3 cp ./global-s3-assets/ s3://$SOURCE_BUCKET_BASE_NAME-$AWS_REGION/$SOLUTION_NAME/$VERSION/ --recursive --acl bucket-owner-full-control --profile aws-cred-profile-name
aws s3 cp ./regional-s3-assets/ s3://$SOURCE_BUCKET_BASE_NAME-$AWS_REGION/$SOLUTION_NAME/$VERSION/ --recursive --acl bucket-owner-full-control --profile aws-cred-profile-name
```

## 06. [AWS CLI] Create CloudFormation Stack with default optional parameters
Disable VPC
```bash
aws --region ${AWS_REGION} cloudformation create-stack --stack-name ${SOLUTION_NAME} --template-url https://s3.amazonaws.com/${SOURCE_BUCKET_BASE_NAME}-${AWS_REGION}/${SOLUTION_NAME}/${VERSION}/${SOLUTION_NAME}.template  --parameters ParameterKey=DomainName,ParameterValue=${OPENSEARCH_DOMAIN_NAME} ParameterKey=OpenSearchServiceRoleExists,ParameterValue=true --capabilities "CAPABILITY_IAM"
```
Enable VPC
```bash
aws --region ${AWS_REGION} cloudformation create-stack --stack-name ${SOLUTION_NAME} --template-url https://s3.amazonaws.com/${SOURCE_BUCKET_BASE_NAME}-${AWS_REGION}/${SOLUTION_NAME}/${VERSION}/${SOLUTION_NAME}.template  --parameters ParameterKey=DomainName,ParameterValue=${OPENSEARCH_DOMAIN_NAME} ParameterKey=OpenSearchServiceRoleExists,ParameterValue=true ParameterKey=EnableVPC,ParameterValue=true --capabilities "CAPABILITY_IAM"
```

***

Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the MIT-0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    https://spdx.org/licenses/MIT-0.html

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific language governing permissions and limitations under the License.
