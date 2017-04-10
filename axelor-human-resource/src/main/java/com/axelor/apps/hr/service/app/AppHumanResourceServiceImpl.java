package com.axelor.apps.hr.service.app;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.AppLeave;
import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.base.db.repo.AppLeaveRepository;
import com.axelor.apps.base.db.repo.AppTimesheetRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AppHumanResourceServiceImpl extends AppBaseServiceImpl implements AppHumanResourceService {
	
	private Long appTimesheetId;
	private Long appLeaveId;
	
	@Inject
	public AppHumanResourceServiceImpl() {
		
		AppTimesheet appTimesheet = Beans.get(AppTimesheetRepository.class).all().fetchOne();
		AppLeave appLeave = Beans.get(AppLeaveRepository.class).all().fetchOne();
		
		if (appTimesheet != null) {
			appTimesheetId = appTimesheet.getId();
			appLeaveId = appLeave.getId();
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
	public AppLeave getAppLeave() {
		return Beans.get(AppLeaveRepository.class).find(appLeaveId);
	}

	@Override
	public void getHrmAppSettings(ActionRequest request, ActionResponse response) {
		
		try {
			
			Map<String, Object> map = new HashMap<>();
			
			map.put("hasInvoicingAppEnable", isApp("invoice"));
			map.put("hasLeaveAppEnable", isApp("leave"));
			map.put("hasExpenseAppEnable", isApp("expense"));
			map.put("hasTimesheetAppEnable", isApp("timesheet"));
			map.put("hasProjectAppEnable", isApp("project"));

			response.setData(map);
			response.setTotal(map.size());
			
		} catch(Exception e) {
			e.printStackTrace();
			response.setException(e);
		}
	}

}
