package com.axelor.apps.supplychain.service.app;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.repo.AppSupplychainRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppSupplychainServiceImpl extends AppBaseServiceImpl implements AppSupplychainService {
	
	private Long appSupplychainId;
	
	@Inject
	public AppSupplychainServiceImpl() {
		
		AppSupplychain appSupplychain = Beans.get(AppSupplychainRepository.class).all().fetchOne();
		if (appSupplychain != null) {
			appSupplychainId = appSupplychain.getId();
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
