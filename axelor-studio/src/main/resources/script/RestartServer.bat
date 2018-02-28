@echo off
set JSSE_OPTS=
call shutdown.bat
ping -n 3 127.0.0.1>null
rmdir %CATALINA_APP% /S /Q 
mkdir %CATALINA_APP%
cd %CATALINA_APP%
jar -xf %1
if exist "..\..\..\etc\application.properties" (
	copy /y ..\..\..\etc\application.properties WEB-INF\classes\
) 
if not "%2" == ""  (	
	psql -d template1 -c "SELECT pg_terminate_backend(pg_stat_activity.pid)  FROM pg_stat_activity  WHERE datname = '"%PGDATABASE%"';" 
	echo "DB Connection stopped"  
	dropdb %PGDATABASE% 
	echo "DB Dropped"
	createdb 
	echo "DB Created" 	
)
ping -n 3 127.0.0.1>null
call startup.bat

exit /B

