package com.axelor.apps.production.service.app;

import com.axelor.apps.base.db.AppProduction;
import com.axelor.apps.base.db.repo.AppProductionRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppProductionServiceImpl extends AppBaseServiceImpl implements AppProductionService {
	
	private Long appProductionId;
	
	@Inject
	public AppProductionServiceImpl() {
		
		AppProduction appProduction = Beans.get(AppProductionRepository.class).all().fetchOne();
		if (appProduction != null) {
			appProductionId = appProduction.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
		
		
	}
	
	@Override
	public AppProduction getAppProduction() {
		return Beans.get(AppProductionRepository.class).find(appProductionId);
	}

}
