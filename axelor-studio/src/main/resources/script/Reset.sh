# Drop and create database and restart tomcat server. 

if [ $# -eq 4 ] 
then
   echo "Tomcat path: "$1
   echo "Database: "$2
   echo "DB user: "$3
   echo "DB password: "$4 
   $1/bin/shutdown.sh
   PGPASSWORD=$4
   export PGPASSWORD
   dropdb -h localhost -U $3 -w $2 
   createdb -h localhost -U $3 -w $2  
   unset PGPASSWORD
   $1/bin/startup.sh
else
    echo "Invalid arguments"
fi


