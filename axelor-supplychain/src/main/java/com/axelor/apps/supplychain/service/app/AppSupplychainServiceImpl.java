package com.axelor.apps.supplychain.service.app;

import com.axelor.apps.base.db.AppPurchase;
import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.repo.AppPurchaseRepository;
import com.axelor.apps.base.db.repo.AppSupplychainRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppSupplychainServiceImpl extends AppBaseServiceImpl implements AppSupplychainService {
	
	private Long appSupplychainId;
	
	@Inject
	public AppSupplychainServiceImpl() {
		
		AppPurchase appPurchase = Beans.get(AppPurchaseRepository.class).all().fetchOne();
		if (appPurchase != null) {
			appSupplychainId = appPurchase.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppSupplychain getAppSupplychain() {
		return Beans.get(AppSupplychainRepository.class).find(appSupplychainId);
	}

}
