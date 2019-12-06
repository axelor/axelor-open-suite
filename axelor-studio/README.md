Overview
------

This module adds tools that helps to create or edit a axelor module directly from the Axelor Open Platform web interface. 

The module components like domain, view, report,..etc could be easily created by using those tools.

It also provides a support to create and apply the workflow.

It generate a module code and store it in to the specified source directory.

All changes could be applied to the working instance with just one button click.

Dependencies
------

* axelor-tool
* axelor-exception
* axelor-message

Install
------

* Put the axelor-studio with its dependencies in to the app module's module path.  	
* Set the following properties in the app module.
  - `studio.source.dir:`  A path to the app module's source directory.
  - `studio.adk.dir:` A path to the axelor-open-platform that is required to build a module.
  - `studio.restart.log:` A path to any text file. It will store the log of the backend script that restart the server.
  - `studio.doc.dir:`A path to directory containing screenshots of the form views. It will be used to generate a doc from the excel sheet.
  - `context.action = com.axelor.studio.utils.ActionHelper`. Put this as it is in the properties file.
* Environment variables to set.
 - `JAVA_HOME:` A path to JDK used by the running app instance. It must be a JDK path and not the JRE path.
 - `CATALINA_HOME:` It is a well known tomcat environment variable. A path to the tomcat server directory used by the current running instance.
 - `PGDATA:` A path to the postgreql installation directory. For linux(ubuntu) its mostly a /usr/lib/postgresql/{postgres version}.
