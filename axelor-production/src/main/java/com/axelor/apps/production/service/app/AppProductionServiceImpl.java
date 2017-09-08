package com.axelor.apps.production.service.app;

import com.axelor.apps.base.db.AppProduction;
import com.axelor.apps.base.db.repo.AppProductionRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppProductionServiceImpl extends AppBaseServiceImpl implements AppProductionService {
	
	@Inject
	private AppProductionRepository appProductionRepo;
	
	@Override
	public AppProduction getAppProduction() {
		return appProductionRepo.all().fetchOne();
	}

}
