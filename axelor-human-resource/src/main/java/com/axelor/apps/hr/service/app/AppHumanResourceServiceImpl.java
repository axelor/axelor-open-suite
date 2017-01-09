package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.base.db.repo.AppTimesheetRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AppHumanResourceServiceImpl extends AppBaseServiceImpl implements AppHumanResourceService {
	
	private Long appTimesheetId;
	
	@Inject
	public AppHumanResourceServiceImpl() {
		
		AppTimesheet appTimesheet = Beans.get(AppTimesheetRepository.class).all().fetchOne();
		if (appTimesheet != null) {
			appTimesheetId = appTimesheet.getId();
		}
		else {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}
	}
	
	@Override
	public AppTimesheet getAppTimesheet() {
		return Beans.get(AppTimesheetRepository.class).find(appTimesheetId);
	}

}
