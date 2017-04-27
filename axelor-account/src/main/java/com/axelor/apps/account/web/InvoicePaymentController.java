/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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


import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoicePaymentController  {
	

	@Inject
	private InvoicePaymentCancelService invoicePaymentCancelService;
	
	@Inject
	private InvoiceRepository invoiceRepo;


	
	public void cancelInvoicePayment(ActionRequest request, ActionResponse response)
	{
		InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);
		
		invoicePayment = Beans.get(InvoicePaymentRepository.class).find(invoicePayment.getId());
		try{
			invoicePaymentCancelService.cancel(invoicePayment);
		}
		catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		response.setReload(true);
	}
	
	//filter the payment mode depending on the target invoice
	
	public void filterPaymentMode(ActionRequest request, ActionResponse response) {
		Map<String, Object> partialInvoice = 
				(Map<String, Object>) request.getContext().get("_invoice");
		Invoice invoice = invoiceRepo.find( ((Integer) partialInvoice.get("id")).longValue());
		PaymentMode paymentMode = invoice.getPaymentMode();
		if (invoice != null && paymentMode != null) {
			if (paymentMode.getInOutSelect() != null) {
				response.setAttr("paymentMode", "domain", "self.inOutSelect = " + paymentMode.getInOutSelect());
			}
		}
	}

}
