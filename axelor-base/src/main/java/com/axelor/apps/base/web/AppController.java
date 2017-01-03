package com.axelor.apps.base.web;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.service.AppService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AppController {
	
	@Inject
	private AppService appService;
	
	public void importDataDemo(ActionRequest request, ActionResponse response) {
		App app = request.getContext().asType(App.class);
		response.setFlash(appService.importDataDemo(app));
		response.setReload(true);
	}
	
	public void importDataInit(ActionRequest request, ActionResponse response) {
		App app = request.getContext().asType(App.class);
		if (!app.getInitDataLoaded()) {
			appService.importDataInit(app);
			response.setValue("initDataLoaded", true);
		}
	}
	

}
