#!/usr/bin/env bash

#
# install DSPOT
#

cd ..
git clone http://github.com/danglotb/dspot.git -b spoon-5.9.0
cd dspot
~/apache-maven-3.3.9/bin/mvn clean package install -DskipTests
cd ../dspot-experiments