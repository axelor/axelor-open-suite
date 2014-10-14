#!/bin/sh
#
# Axelor Business Solutions
#
# Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
#
# This program is free software: you can redistribute it and/or  modify
# it under the terms of the GNU Affero General Public License, version 3,
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#


DATE=`/bin/date "+%Y%m%d"`
PATHDUMP="$HOME/dump"
PATHLOG="$HOME/logs"
DATABASE="axelor-erp"

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
	args=`echo "-c src/main/resources/config_files/csv-config_demo.xml -d src/main/resources/data/demo_FR/ "`
	(exec mvn -q exec:java -Dexec.mainClass="com.axelor.erp.data.Main" -Dexec.args="$args" > $PATHLOG/axelor_demo_$DATE.log)
	backupDb $DATABASE $PATHDUMP/axelor_demo_$DATE
fi

echo "Import Done !"

