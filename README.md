# proteus-s3upload-example
[![Build Status](https://travis-ci.org/gregwhitaker/proteus-s3upload-example.svg?branch=master)](https://travis-ci.org/gregwhitaker/proteus-s3upload-example)

An example of a streaming file upload to [Amazon S3](https://aws.amazon.com/s3/) using [Netifi Proteus](https://www.netifi.com/proteus).

## Prerequisites
This example requires a running Netifi Proteus Broker.

1. Run the following command to start a Netifi Proteus Broker:

        docker run \
        -p 8001:8001 \
        -p 7001:7001 \
        -p 9000:9000 \
        -e BROKER_SERVER_OPTS="'-Dnetifi.authentication.0.accessKey=9007199254740991'  \
        '-Dnetifi.broker.console.enabled=true' \
        '-Dnetifi.authentication.0.accessToken=kTBDVtfRBO4tHOnZzSyY5ym2kfY=' \
        '-Dnetifi.broker.admin.accessKey=9007199254740991' \
        '-Dnetifi.broker.admin.accessToken=kTBDVtfRBO4tHOnZzSyY5ym2kfY='" \
        netifi/proteus:1.5.2

## Building the Example
Follow the steps below to build the example.

1. Run the following Gradle command to build the application:

        ./gradlew clean build

## Running the Example
Follow the steps below to run the example.

1. Ensure that you have a running Netifi Proteus Broker.

2. Run the following command to start the upload service:

        ./gradlew :service:run
        
3. Run the following command to upload the `cat.jpeg` image to Amazon S3:

## Bugs and Feedback
For bugs, questions, and discussions please use the [Github Issues](https://github.com/gregwhitaker/proteus-s3upload-example/issues).

## Support
Support for Proteus and RSocket is also available on the [Netifi Community Forums](https://community.netifi.com).

## License
Copyright 2018 [Netifi Inc.](https://www.netifi.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.