#!/bin/bash

echo "App Path: " $1
echo "Reset app: " $2 
echo "Catalina home: "$CATALINA_HOME
echo "PGHOME: "$PGHOME

#$CATALINA_HOME/bin/shutdown.sh

pid=$(ps x | grep "${CATALINA_HOME}" | grep -v grep | cut -d ' ' -f 1)

echo "Pid: "$pid

#while [ -d "/proc/$pid" ]
#do
#    sleep 10
#done

echo "Process $pid has finished"

rm -fr $CATALINA_APP*
mkdir $CATALINA_APP

cd $CATALINA_APP
jar -xf $1

if [ -f ../../../etc/application.properties ]; then
	cp ../../../etc/application.properties META_INF/classes/
fi

if [ $# -eq 2 ]; then   
	echo "Reseting app...."
	if psql -d template1 -c "SELECT pg_terminate_backend(pg_stat_activity.pid)  FROM pg_stat_activity  WHERE datname = '"$PGDATABASE"';"; then
	  dropdb $PGDATABASE
	  createdb
	else 
	  echo error
	  exit
	fi 
fi

echo "Starting tomcat server"
$CATALINA_HOME/bin/startup.sh
