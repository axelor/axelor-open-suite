Overview
------

This module add tools to create or edit axelor module directly from web interface without writing codes. 

It will helps to create essential elements of module like model, views, menus, actions,report..etc.

Also provides tools to create workflow (business process). Build application and display result with one click. 
  
Dependencies
------

* axelor-tool
* axelor-exception
* axelor-message

Install
------

* Put axelor-studio with its dependencies in to your module path. 
* Set following properties in your app module. 
  - `studio.source.dir:`  Path to main app module. 
  - `studio.webapp.dir:` Path to 'webapp'(name of your app instance) located under 'webapps' of tomcat server. 
  - `studio.catalina.home:` Path to tomcat server.
  - `studio.adk.dir:` Path to axelor-development-kit required to build the module.
  - `studio.restart.log:` Path to any text file that will store log of tomcat server restart. 
  - `context.action = com.axelor.studio.utils.ActionHelper`. Put this as it is in properties file.
