package com.axelor.apps.production.service.app;

import com.axelor.apps.base.db.AppProduction;
import com.axelor.apps.base.service.app.AppBaseService;

public interface AppProductionService extends AppBaseService {
	
	public AppProduction getAppProduction();
	
	public void generateProductionConfigurations();

}
