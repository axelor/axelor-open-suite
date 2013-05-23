package com.axelor.apps.base.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.AlarmEngine
import com.axelor.apps.base.service.alarm.AlarmEngineService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class AlarmEngineController {
	
	@Inject
	private AlarmEngineService aes
	
	def void validateQuery(ActionRequest request, ActionResponse response) {
		
		AlarmEngine alarmEngine = request.context as AlarmEngine

		try {
			
			if (alarmEngine.query) { aes.results(alarmEngine.query, Class.forName(alarmEngine.metaModel.fullName)) }
			
		
		} catch (Exception e){
			
			response.values = ["query": alarmEngine.id ? AlarmEngine.find(alarmEngine.id)?.query : null ]
			
			TraceBackService.trace(response, e)
			
		}
		
	}
}
