package com.axelor.apps.purchase.service.app;

import com.axelor.apps.base.db.AppPurchase;
import com.axelor.apps.base.db.repo.AppPurchaseRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppPurchaseServiceImpl extends AppBaseServiceImpl implements AppPurchaseService {
	
	@Inject
	private AppPurchaseRepository appPurchaseRepo;
	
	@Override
	public AppPurchase getAppPurchase() {
		return appPurchaseRepo.all().fetchOne();
	}
	
	
}
