#!/bin/sh
ls objs/* > /dev/null 2> /dev/null

HASOBJS=$?
if [ $HASOBJS -eq 0 ]; then
        rm objs/* -r
fi

if test -f "mcDownloader.jar"; then
    rm mcDownloader.jar
fi
