package com.axelor.apps.businessproject.service.app;

import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.base.db.repo.AppBusinessProjectRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppBusinessProjectServiceImpl extends AppBaseServiceImpl implements AppBusinessProjectService {
	
	private Long appBusinessProjectId;
	
	@Inject
	public AppBusinessProjectServiceImpl() {
		
		AppBusinessProject appBusinessProject = Beans.get(AppBusinessProjectRepository.class).all().fetchOne();
		
		if (appBusinessProject != null) {
			appBusinessProjectId = appBusinessProject.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppBusinessProject getAppBusinessProject() {
		return Beans.get(AppBusinessProjectRepository.class).find(appBusinessProjectId);
	}
	
}
