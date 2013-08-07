package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AlarmEngineController {

	@Inject
	private AlarmEngineService aes;
	
	public void validateQuery(ActionRequest request, ActionResponse response) {
		
		AlarmEngine alarmEngine = request.getContext().asType(AlarmEngine.class);

		try {
			if (alarmEngine.getQuery() != null) { 
				aes.results(alarmEngine.getQuery(), Class.forName(alarmEngine.getMetaModel().getFullName())); 
			}
		} catch (Exception e){
			response.setValue("query", alarmEngine.getId() != null ? AlarmEngine.find(alarmEngine.getId()).getQuery() : null);
			TraceBackService.trace(response, e);
		}
	}
}
