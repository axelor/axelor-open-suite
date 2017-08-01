package com.axelor.apps.account.service.app;

import com.axelor.apps.base.db.AppAccount;
import com.axelor.apps.base.db.AppBudget;
import com.axelor.apps.base.db.AppInvoice;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.db.repo.AppBudgetRepository;
import com.axelor.apps.base.db.repo.AppInvoiceRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppAccountServiceImpl extends AppBaseServiceImpl implements AppAccountService {
	
	@Inject
	private AppAccountRepository appAccountRepo;
	
	@Inject
	private AppBudgetRepository appBudgetRepo;
	
	@Inject
	private AppInvoiceRepository appInvoiceRepo;
	
	@Override
	public AppAccount getAppAccount() {
		return appAccountRepo.all().fetchOne();
	}

	@Override
	public AppBudget getAppBudget() {
		return appBudgetRepo.all().fetchOne();
	}

	@Override
	public AppInvoice getAppInvoice() {
		return appInvoiceRepo.all().fetchOne();
	}


}
