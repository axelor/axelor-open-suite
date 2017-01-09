package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.base.service.app.AppBaseService;

public interface AppHumanResourceService extends AppBaseService {
	
	public AppTimesheet getAppTimesheet();
}
