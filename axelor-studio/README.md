Overview
------

This module add tools to create or edit module directly from interface without writing codes. 

It will helps to create essential parts of module like model, views, menus, actions,report..etc.

Also it build app and display result immediately on interface. 
  
Dependencies
------

* axelor-too
* axelor-exception
* axelor-studio

Install
------

* Put axelor-studio with its dependencies in to your module path. 
* Set following properties in your app module. 
  - `build.dir:`  Path to app module. 
  - `axelor.home:` AXELOR_HOME variable path. 
  - `tomcat.webapp:` Path to webapp directory of tomcat instance where you run your app. 
  - `context.action = com.axelor.studio.utils.ActionHelper`. Put this as it is in properties file.
