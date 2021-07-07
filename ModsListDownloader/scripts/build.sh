#!/bin/sh
javac \
    -d objs\
    -cp libs/commons-compress-1.20.jar:libs/gson-2.8.7.jar:libs/gson-2.8.6.jar\
    downloader/*.java downloader/forgeSvc/*.java
