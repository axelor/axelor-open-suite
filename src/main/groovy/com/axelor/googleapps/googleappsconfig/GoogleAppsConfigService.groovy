package com.axelor.googleapps.googleappsconfig

import com.axelor.apps.base.db.GoogleAppsConfig;

class GoogleAppsConfigService {
	
     public GoogleAppsConfig onNewAppsConfig() {
		 
		  GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		  
		  if(googleAppsConfig != null) {
			return googleAppsConfig 
		  } else {
			  GoogleAppsConfig newGoogleAppsConfig=new GoogleAppsConfig()
			  return newGoogleAppsConfig
		  } 
	  }
}
