/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
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
