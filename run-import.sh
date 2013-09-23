#!/bin/sh
#
# Copyright (c) 2012-2013 Axelor. All Rights Reserved.
#
# The contents of this file are subject to the Common Public
# Attribution License Version 1.0 (the “License”); you may not use
# this file except in compliance with the License. You may obtain a
# copy of the License at:
#
# http://license.axelor.com/.
#
# The License is based on the Mozilla Public License Version 1.1 but
# Sections 14 and 15 have been added to cover use of software over a
# computer network and provide for limited attribution for the
# Original Developer. In addition, Exhibit A has been modified to be
# consistent with Exhibit B.
#
# Software distributed under the License is distributed on an “AS IS”
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
# the License for the specific language governing rights and limitations
# under the License.
#
# The Original Code is part of "Axelor Business Suite", developed by
# Axelor exclusively.
#
# The Original Developer is the Initial Developer. The Initial Developer of
# the Original Code is Axelor.
#
# All portions of the code written by Axelor are
# Copyright (c) 2012-2013 Axelor. All Rights Reserved.
#


DATE=`/bin/date "+%Y%m%d"`
PATHDUMP="$HOME/dump"
PATHLOG="$HOME/logs"
DATABASE="axelor-import"

backupDb ()
{
	database=$1
	path=$2
	a=0
	fullpath="$path.sql"
	
	until  test ! -f "$fullpath";
	do
		a=`expr $a + 1`
		fullpath=$path'_'$a'.sql'
	done
	
	echo "Info: Making backup of database $database : $fullpath"
	
	/usr/bin/pg_dump $database -f $fullpath

	return 0
}

if [ ! -z $2 ]; then
	DATABASE=`echo "$2"`
fi
echo "Using $DATABASE"

if test ! -d "$PATHDUMP"; then
   echo "Error: source directory '$PATHDUMP' does not exist or is not a dir"
   exit 1
fi

if test ! -d "$PATHLOG"; then
   echo "Error: source directory '$PATHLOG' does not exist or is not a dir"
   exit 1
fi

if [ $(psql -A postgres -c "select count(*) from pg_database where datname in ('$DATABASE')") = 0 ]; then
	echo "Error: Database $DATABASE not exist"
	exit 1
fi

if [ $1 = "base" ]; then
	echo "Run base import"
	args=`echo "-c src/main/resources/config_files/csv-config_base.xml -d src/main/resources/data/base/ "`
	(exec mvn -q exec:java -Dexec.mainClass="com.axelor.erp.data.Main" -Dexec.args="$args" > $PATHLOG/axelor-base_$DATE.log)
	backupDb $DATABASE $PATHDUMP/axelor_base_$DATE
elif [ $1 = "demo" ]; then
	echo "Run base import"
	args=`echo "-c src/main/resources/config_files/csv-config_demo.xml -d src/main/resources/data/demo/ "`
	(exec mvn -q exec:java -Dexec.mainClass="com.axelor.erp.data.Main" -Dexec.args="$args" > $PATHLOG/axelor_demo_$DATE.log)
	backupDb $DATABASE $PATHDUMP/axelor_demo_$DATE
fi

echo "Import Done !"

