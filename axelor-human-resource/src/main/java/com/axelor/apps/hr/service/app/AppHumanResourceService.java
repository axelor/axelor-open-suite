package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.db.AppExpense;
import com.axelor.apps.base.db.AppLeave;
import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public interface AppHumanResourceService extends AppBaseService {
	
	public AppTimesheet getAppTimesheet();
	
	public AppLeave getAppLeave();
	
	public AppExpense getAppExpense();

	public void getHrmAppSettings(ActionRequest request, ActionResponse response);
}
