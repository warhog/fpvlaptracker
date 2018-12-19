#!/bin/sh

FIRST_JAR_FILE=`ls -U1 fpvlaptracker-*.jar | head -n 1`
java -jar ${FIRST_JAR_FILE}
