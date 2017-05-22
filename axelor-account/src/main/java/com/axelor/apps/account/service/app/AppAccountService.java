package com.axelor.apps.account.service.app;

import com.axelor.apps.base.db.AppAccount;
import com.axelor.apps.base.db.AppBudget;
import com.axelor.apps.base.db.AppInvoice;
import com.axelor.apps.base.service.app.AppBaseService;

public interface AppAccountService extends AppBaseService {
	
	public AppAccount getAppAccount();
	
	public AppBudget getAppBudget();
	
	public AppInvoice getAppInvoice();
	
}
