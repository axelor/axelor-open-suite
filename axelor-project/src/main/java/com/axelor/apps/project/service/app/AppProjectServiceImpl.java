package com.axelor.apps.project.service.app;

import com.axelor.apps.base.db.AppProject;
import com.axelor.apps.base.db.repo.AppProjectRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppProjectServiceImpl extends AppBaseServiceImpl implements AppProjectService  {
	
	private Long appProjectId;
	
	@Inject
	public AppProjectServiceImpl() {
		
		AppProject appProject = Beans.get(AppProjectRepository.class).all().fetchOne();
		
		if (appProject != null) {
			appProjectId = appProject.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppProject getAppProject() {
		return Beans.get(AppProjectRepository.class).find(appProjectId);
	}

}
