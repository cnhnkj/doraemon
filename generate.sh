#!/bin/bash

# 1.0.0
# bash generate.sh coins,userVip,hookService,paradise

# 1.1.0
# bash generate.sh supply,order-query,order-create,hn-live

# 1.2.0
# bash generate.sh phoenix,shop-mono

# 1.3.0
# bash generate.sh communication,wechat-foreground,imdatacenter-mono

# mvn clean package
java -jar doraemon-generator/target/doraemon-generator.jar $1

# mvn clean deploy 
# mvn clean install 


