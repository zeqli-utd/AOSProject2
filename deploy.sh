#!/bin/bash

rm -r bin/*
# Compile project to ./bin folder
javac -d bin src/main/java/aos/*.java src/main/java/clock/*.java src/main/java/snapshot/*.java src/main/java/helpers/*.java 
#src/test/java/integration/*.java

# Upload to remote virtual machine
scp -r bin zxl165030@dc01.utdallas.edu:~/TestProj
