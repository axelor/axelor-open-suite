package com.axelor.apps.project.service.app;

import com.axelor.apps.base.db.AppProject;
import com.axelor.apps.base.db.repo.AppProjectRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppProjectServiceImpl extends AppBaseServiceImpl implements AppProjectService  {
	
	@Inject
	private AppProjectRepository appProjectRepo;
	
	@Override
	public AppProject getAppProject() {
		return appProjectRepo.all().fetchOne();
	}

}
