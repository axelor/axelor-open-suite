package com.axelor.apps.account.web

import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.MoveLine
import com.axelor.apps.account.service.IrrecoverableService
import com.axelor.apps.account.service.MoveLineService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector;


@Slf4j
class MoveLineController {
	
//	@Inject
//	private MoveLineService mls
//	
//	@Inject
//	private IrrecoverableService is
	
	@Inject
	private Injector injector
	
	
	def void usherProcess(ActionRequest request, ActionResponse response) {
		
		MoveLine moveLine = request.context as MoveLine
		moveLine = MoveLine.find(moveLine.id)
		
		MoveLineService mls = injector.getInstance(MoveLineService.class)
		
		try {
			mls.usherProcess(moveLine)
		}
		catch (Exception e){ TraceBackService.trace(response, e) }
	}
	
	def void passInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.context as MoveLine
		moveLine = MoveLine.find(moveLine.id)
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class)
		
		try  {
			is.passInIrrecoverable(moveLine, true, true)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	def void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.context as MoveLine
		moveLine = MoveLine.find(moveLine.id)
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class)
		
		try  {
			is.notPassInIrrecoverable(moveLine, true)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
}