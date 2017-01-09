package com.axelor.apps.purchase.service.app;

import com.axelor.apps.base.db.AppPurchase;
import com.axelor.apps.base.db.repo.AppPurchaseRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppPurchaseServiceImpl extends AppBaseServiceImpl implements AppPurchaseService {
	
	private Long appPurchaseId;
	
	@Inject
	public AppPurchaseServiceImpl() {
		
		AppPurchase appPurchase = Beans.get(AppPurchaseRepository.class).all().fetchOne();
		if (appPurchase != null) {
			appPurchaseId = appPurchase.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppPurchase getAppPurchase() {
		return Beans.get(AppPurchaseRepository.class).find(appPurchaseId);
	}
	
	
}
