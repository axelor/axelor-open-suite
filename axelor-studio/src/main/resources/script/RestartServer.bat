::Drop and create database and restart tomcat server. 
@echo off

if "%6" == ""  (
 cd %1
 if exist  ..\..\bin\stop.cmd (
  cd ..\..\
  echo "Installer exist"
  java -jar bin\axelor-ctrl.jar stop
  cd %2
  jar -xvf %3
  cd ..\..\..\
  java -jar bin\axelor-ctrl.jar "start"
 ) else ( 
    call %1\bin\shutdown.bat
	cd %2
	jar -xvf %3
	%1\bin\startup.bat
 )
) else (
 cd %1
 if exist  ..\..\bin\stop.cmd (
  cd ..\..\
  echo "Installer exist"
  cd %2
  java -jar bin\axelor-ctrl.jar stop
  %JAVA_HOME%\bin\jar -xvf %3
  cd ..\..\..\
  java -jar bin\axelor-ctrl.jar "start"
 ) else ( 
    call %1\bin\shutdown.bat
	cd %2
	%JAVA_HOME%\bin\jar -xvf %3
	set PGPASSWORD = %6
	psql -U %5 -d template1 -c "SELECT pg_terminate_backend(pg_stat_activity.pid)  FROM pg_stat_activity  WHERE datname = '"%4"';"
	dropdb -h localhost -U %5 -w %4
    createdb -h localhost -U %5 -w %4  
	%1\bin\startup.bat
 )
)

exit /B

