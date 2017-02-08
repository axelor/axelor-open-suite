package com.axelor.apps.hr.service.app;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.base.db.repo.AppTimesheetRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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

	@Override
	public void getHrmAppSettings(ActionRequest request, ActionResponse response) {
		
		try {
			
			Map<String, Object> map = new HashMap<>();
			
			map.put("hasInvoicingEnable", isApp("invoice"));
			
			map.put("hasLeaveAppEnable", isApp("leave"));
			map.put("hasExpenseKilometricEnable", isApp("expense"));
			map.put("hasExpenseAppEnable", isApp("expense"));
			map.put("hasTimesheetAppEnable", isApp("timesheet"));
			map.put("hasTaskEnable", isApp("project"));

			response.setData(map);
			response.setTotal(map.size());
			
		} catch(Exception e) {
			e.printStackTrace();
			response.setException(e);
		}
	}

}
