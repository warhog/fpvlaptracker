#!/bin/sh

sudo update-java-alternatives -s java-11-oracle
export JAVA_HOME=/usr/lib/jvm/java-11-oracle/
export PATH=$JAVA_HOME:$PATH