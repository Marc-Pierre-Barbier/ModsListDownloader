#!/bin/sh
mkdir -p objs
cd src

javac \
    -d ../objs\
    -cp ../lib/commons-compress-1.20.jar:../lib/gson-2.8.7.jar:../lib/gson-2.8.6.jar\
    downloader/*.java downloader/forgeSvc/*.java downloader/helper/*.java

cd ..
