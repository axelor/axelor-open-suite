# Drop and create database and restart tomcat server. 

if [ $# -eq 3 ] 
then
  echo "Tomcat path: "$1
  echo "Tomcat webapp: "$2
  echo "War file: "$3
  $1/bin/shutdown.sh
  rm $2.war
  rm -fr $2/*
  (cd $2 && jar -xf $3)
  $1/bin/startup.sh
elif [ $# -eq 6 ] 
then
   echo "Tomcat path: "$1
   echo "Tomcat webapp: "$2
   echo "War file: "$3
   echo "Database: "$4
   echo "DB user: "$5
   echo "DB password: "$6
   $1/bin/shutdown.sh
   rm $2.war
   rm -fr $2/*
   (cd $2 && jar -xf $3)
   PGPASSWORD=$6
   export PGPASSWORD
   psql -U $5 -d template1 -c "SELECT pg_terminate_backend(pg_stat_activity.pid)  FROM pg_stat_activity  WHERE datname = '"$4"';"
   dropdb -h localhost -U $5 -w $4 
   createdb -h localhost -U $5 -w $4  
   $1/bin/startup.sh
else
    echo "Invalid arguments"
fi


