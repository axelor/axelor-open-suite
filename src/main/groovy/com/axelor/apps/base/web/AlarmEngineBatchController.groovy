package com.axelor.apps.base.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.AlarmEngineBatch
import com.axelor.apps.base.service.alarm.AlarmEngineBatchService
import com.axelor.exception.AxelorException
import com.axelor.exception.db.IException
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class AlarmEngineBatchController {
	
	@Inject
	private AlarmEngineBatchService alarmEngineBatchService;
	
	def void launch(ActionRequest request, ActionResponse response) {
		
		AlarmEngineBatch alarmEngineBatch = request.context as AlarmEngineBatch
		alarmEngineBatch = AlarmEngineBatch.find(alarmEngineBatch.id)
		
		response.flash = alarmEngineBatchService.run( alarmEngineBatch )?.comment
		response.reload = true
		
	}

	// WS
	def void run(ActionRequest request, ActionResponse response) {
		
		AlarmEngineBatch alarmEngineBatch = AlarmEngineBatch.all().filter("self.code = ?1", request.context.code).fetchOne();
		
		if ( alarmEngineBatch == null ) {
			TraceBackService.trace( new AxelorException("Batch d'alarme ${request.context.code}"), IException.CONFIGURATION_ERROR );
		}
		else {
			response.data = [
				"anomaly":alarmEngineBatchService.run( alarmEngineBatch )?.anomaly
			]
		}
		
	}
}
