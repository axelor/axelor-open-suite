#!/bin/bash

echo "App Path: " $1
echo "Reset app: " $2 
echo "Catalina home: "$CATALINA_HOME
echo "Catalina app: "$CATALINA_APP
#echo "PGHOME: "$PGHOME

#pid=$(ps x | grep "${CATALINA_HOME}" | grep -v grep | cut -d ' ' -f 1)

#echo "Pid: "$pid

#$CATALINA_HOME/bin/shutdown.sh

#while [ -d "/proc/$pid" ]
#do
#    sleep 2
#done

#echo "Process $pid has finished"

echo "Removing old app directory"
rm -fr $CATALINA_APP*

sleep 5

echo "Creating new app directory"
mkdir -v $CATALINA_APP

cd $CATALINA_APP

echo "Extracting app"
jar -xvf $1

#if [ -f ../../../etc/application.properties ]; then
#	cp ../../../etc/application.properties META_INF/classes/
#fi

#if [ $# -eq 2 ]; then   
#	echo "Reseting app...."
#	if psql -d template1 -c "SELECT pg_terminate_backend(pg_stat_activity.pid)  FROM pg_stat_activity  WHERE datname = '"$PGDATABASE"';"; then
#	  dropdb $PGDATABASE
#	  createdb
#	else 
#	  echo error
#	  exit
#	fi 
#fi

#echo "Starting tomcat server"
#$CATALINA_HOME/bin/startup.sh
