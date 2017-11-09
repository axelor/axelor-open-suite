package com.axelor.apps.businessproject.service.app;

import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.base.db.repo.AppBusinessProjectRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppBusinessProjectServiceImpl extends AppBaseServiceImpl implements AppBusinessProjectService {
	
	@Inject
	private AppBusinessProjectRepository appBusinessProjectRepo;
	
	@Override
	public AppBusinessProject getAppBusinessProject() {
		return appBusinessProjectRepo.all().fetchOne();
	}
	
}
