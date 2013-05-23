package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.PaymentSchedule
import com.axelor.apps.account.service.IrrecoverableService
import com.axelor.apps.account.service.PaymentScheduleService
import com.axelor.apps.base.db.IAdministration
import com.axelor.apps.base.service.administration.SequenceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector

@Slf4j
class PaymentScheduleController {

//	@Inject
//	private SequenceService sgs
//	
//	@Inject
//	private PaymentScheduleService pss
//	
//	@Inject
//	private IrrecoverableService is
	
	@Inject
	private Injector injector
	
	// Validation button
	def void validate(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.id)
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class)
		
		try {
			pss.validatePaymentSchedule(paymentSchedule)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	// Cancel button
	def void cancel(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.id)
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class)
		
		try {
			pss.toCancelPaymentSchedule(paymentSchedule)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}

	//Called on onSave event
	def void paymentScheduleScheduleId(ActionRequest request, ActionResponse response){

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule
		
		SequenceService sgs = injector.getInstance(SequenceService.class)
		
		if (!paymentSchedule.scheduleId){
		
			def num = sgs.getSequence(IAdministration.PAYMENT_SCHEDULE, paymentSchedule.company, false)
		
			if (!num) {
				
				response.flash = "Veuillez configurer une séquence Echéancier pour la société "+paymentSchedule.company.name
				
			}
			else {
				
				response.values = [
					"scheduleId" : num	
				]
				
			}
		}
	}
	
	// Creating payment schedule lines button
	def void createPaymentScheduleLines(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.id)
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class)
		
		pss.createPaymentScheduleLines(paymentSchedule)
		response.reload = true
		
	}
	
	
	def void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.id)
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class)
		
		try  {
			
			is.passInIrrecoverable(paymentSchedule)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
	def void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.context as PaymentSchedule		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.id)
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class)
		
		try  {
			
			is.notPassInIrrecoverable(paymentSchedule)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
}
