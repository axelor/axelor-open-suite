package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class PaymentScheduleController {
	
	@Inject
	private Injector injector;
	
	// Validation button
	public void validate(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.getId());
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class);
		
		try {
			pss.validatePaymentSchedule(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Cancel button
	public void cancel(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.getId());
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class);
		
		try {
			pss.toCancelPaymentSchedule(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	//Called on onSave event
	public void paymentScheduleScheduleId(ActionRequest request, ActionResponse response){

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
		
		SequenceService sgs = injector.getInstance(SequenceService.class);
		
		if (paymentSchedule.getScheduleId() == null) {
		
			String num = sgs.getSequence(IAdministration.PAYMENT_SCHEDULE, paymentSchedule.getCompany(), false);
		
			if(num == null || num.isEmpty()) {
				
				response.setFlash("Veuillez configurer une séquence Echéancier pour la société "+paymentSchedule.getCompany().getName()); 
			}
			else {
				response.setValue("scheduleId", num);			
			}
		}
	}
	
	// Creating payment schedule lines button
	public void createPaymentScheduleLines(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.getId());
		
		PaymentScheduleService pss = injector.getInstance(PaymentScheduleService.class);
		
		pss.createPaymentScheduleLines(paymentSchedule);
		response.setReload(true);
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.passInIrrecoverable(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = PaymentSchedule.find(paymentSchedule.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.notPassInIrrecoverable(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
