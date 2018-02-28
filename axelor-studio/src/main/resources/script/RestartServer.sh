#!/bin/bash

echo "App Path: " $1
echo "Reset app: " $2 
echo "Catalina home: "$CATALINA_HOME
echo "PGHOME: "$PGHOME

if shutdown.sh; then
        rm -fr $CATALINA_APP*
	mkdir $CATALINA_APP
        cd $CATALINA_APP
	jar -xf $1
	if [ -f ../../../etc/application.properties]; then
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
 	startup.sh
fi

