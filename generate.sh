#!/bin/bash

# 例子: bash generate.sh coins,paradise,hook-service,user-vip,supply 

mvn clean package
java -jar doraemon-generator/target/doraemon-generator.jar $1

# mvn clean deploy --projects doraemon-api -amd
# mvn clean install --projects doraemon-api -amd


