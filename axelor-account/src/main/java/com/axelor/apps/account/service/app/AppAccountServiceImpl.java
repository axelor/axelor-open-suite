package com.axelor.apps.account.service.app;

import com.axelor.apps.base.db.AppAccount;
import com.axelor.apps.base.db.AppBudget;
import com.axelor.apps.base.db.AppInvoice;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.db.repo.AppBudgetRepository;
import com.axelor.apps.base.db.repo.AppInvoiceRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppAccountServiceImpl extends AppBaseServiceImpl implements AppAccountService {
	
	private Long appAccountId;
	
	private Long appBudgetId;
	
	private Long appInvoiceId;
	
	@Inject
	public AppAccountServiceImpl() {
		AppAccount appAccount = Beans.get(AppAccountRepository.class).all().fetchOne();
		AppBudget appBudget = Beans.get(AppBudgetRepository.class).all().fetchOne();
		AppInvoice appInvoice = Beans.get(AppInvoiceRepository.class).all().fetchOne();
		
		if(appAccount != null  && appBudget != null)  {
			appAccountId = appAccount.getId();
			appBudgetId = appBudget.getId();
			appInvoiceId = appInvoice.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppAccount getAppAccount() {
		return Beans.get(AppAccountRepository.class).find(appAccountId);
	}

	@Override
	public AppBudget getAppBudget() {
		return Beans.get(AppBudgetRepository.class).find(appBudgetId);
	}

	@Override
	public AppInvoice getAppInvoice() {
		return Beans.get(AppInvoiceRepository.class).find(appInvoiceId);
	}


}
