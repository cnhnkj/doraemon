#!/bin/bash

# 例子: bash generate.sh coins,paradise,hook-service,user-vip,supply,order-query,order-create,hn-live,phoenix

mvn clean package
java -jar doraemon-generator/target/doraemon-generator.jar $1

mvn clean deploy 
# mvn clean install 


