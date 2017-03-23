#!/bin/bash

rm -r bin/*
# Compile project to ./bin folder
javac -d bin src/main/java/aos/*.java\
             src/main/java/clock/*.java \
             src/main/java/snapshot/*.java \
             src/main/java/helpers/*.java \
             src/main/java/socket/*.java
