#!/bin/sh

#sudo update-alternatives --config java
sudo update-java-alternatives --set /usr/lib/jvm/java-1.8.0-openjdk-amd64
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export PATH=$JAVA_HOME:$PATH