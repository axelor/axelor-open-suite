package com.axelor.googleapps.googleappsconfig

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class GoogleAppsConfigController {
	
	@Inject GoogleAppsConfigService googleAppsConfigServiceObj
	void onNewGoogleAppsConfig(ActionRequest request, ActionResponse response) {
		response.values = googleAppsConfigServiceObj.onNewAppsConfig()
	}
}
