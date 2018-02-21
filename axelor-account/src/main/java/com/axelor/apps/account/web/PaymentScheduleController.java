/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PaymentScheduleController {
	
	@Inject
	PaymentScheduleService paymentScheduleService;
	
	@Inject
	PaymentScheduleRepository paymentScheduleRepo;
	
	// Validation button
	public void validate(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
		paymentSchedule = paymentScheduleRepo.find(paymentSchedule.getId());
		
		try {
			paymentScheduleService.validatePaymentSchedule(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Cancel button
	public void cancel(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
		paymentSchedule = paymentScheduleRepo.find(paymentSchedule.getId());
		
		try {
			paymentScheduleService.toCancelPaymentSchedule(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
    // Called on onSave event
    public void paymentScheduleScheduleId(ActionRequest request, ActionResponse response) {
        try {
            PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
            paymentScheduleService.checkTotalLineAmount(paymentSchedule);

            if (Strings.isNullOrEmpty(paymentSchedule.getPaymentScheduleSeq())) {

                String num = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.PAYMENT_SCHEDULE,
                        paymentSchedule.getCompany());

                if (Strings.isNullOrEmpty(num)) {
                    response.setError(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_5),
                            paymentSchedule.getCompany().getName()));
                } else {
                    response.setValue("paymentScheduleSeq", num);
                }
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
    }

	// Creating payment schedule lines button
	public void createPaymentScheduleLines(ActionRequest request, ActionResponse response) {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
		paymentSchedule = paymentScheduleRepo.find(paymentSchedule.getId());
		
		paymentScheduleService.createPaymentScheduleLines(paymentSchedule);
		response.setReload(true);
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = paymentScheduleRepo.find(paymentSchedule.getId());
		
		try  {
			Beans.get(IrrecoverableService.class).passInIrrecoverable(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);		
		paymentSchedule = paymentScheduleRepo.find(paymentSchedule.getId());
		
		try  {
			Beans.get(IrrecoverableService.class).notPassInIrrecoverable(paymentSchedule);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
