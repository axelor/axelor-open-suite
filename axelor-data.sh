#!/bin/sh

args=`echo "$@"`
exec mvn -q exec:java -Dexec.mainClass="com.axelor.erp.data.Main" -Dexec.args="$args"

