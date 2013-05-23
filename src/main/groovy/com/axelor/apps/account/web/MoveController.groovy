package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Move
import com.axelor.apps.account.service.MoveService
import com.axelor.apps.account.service.PeriodService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector;

@Slf4j
class MoveController {
	
//	@Inject
//	private MoveService ms
	
//	@Inject
//	private PeriodService ps
	
	@Inject
	private Injector injector
	
	def void validate(ActionRequest request, ActionResponse response) {

		Move move = request.context as Move
		move = Move.find(move.id)
		
		MoveService ms = injector.getInstance(MoveService.class)
		
		try {
			ms.validate(move)
			response.reload = true
		}
		catch (Exception e){ TraceBackService.trace(response, e) }
	}
	
	
	def void getPeriod(ActionRequest request, ActionResponse response) {
		
		Move move = request.context as Move
	
		try {
			
			if(move.date && move.company)  {
				
				PeriodService ps = injector.getInstance(PeriodService.class)
				
				response.values = [
					"period" : ps.rightPeriod(move.date, move.company)	
				]
				
			}
			else  {
				
				response.values = [
					"period" : null
				]
				
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e) }
	}
	
}