package com.axelor.apps.supplychain.service.app;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.repo.AppSupplychainRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppSupplychainServiceImpl extends AppBaseServiceImpl implements AppSupplychainService {
	
	@Inject
	private AppSupplychainRepository appSupplychainRepo;
	
	@Override
	public AppSupplychain getAppSupplychain() {
		return appSupplychainRepo.all().fetchOne();
	}

}
