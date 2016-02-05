/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class PaymentVoucherController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private PaymentVoucherRepository paymentVoucherRepo;
	
	@Inject
	private PaymentVoucherLoadService paymentVoucherLoadService; 
	
	
	//Called on onSave event
	public void paymentVoucherSetNum(ActionRequest request, ActionResponse response) throws AxelorException{

		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

		if (Strings.isNullOrEmpty(paymentVoucher.getRef()))  {

			response.setValue("ref", Beans.get(PaymentVoucherSequenceService.class).getReference(paymentVoucher));

		}
	}
	
	// Loading move lines of the selected partner (1st O2M)
	public void loadMoveLines(ActionRequest request, ActionResponse response) {

		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = paymentVoucherRepo.find(paymentVoucher.getId());
		
		try {
			paymentVoucherLoadService.loadMoveLines(paymentVoucher);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Filling lines to pay (2nd O2M)
	public void loadSelectedLines(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucherContext = request.getContext().asType(PaymentVoucher.class);
		PaymentVoucher paymentVoucher = paymentVoucherRepo.find(paymentVoucherContext.getId());
			
		try {
			paymentVoucherLoadService.loadSelectedLines(paymentVoucher,paymentVoucherContext);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Confirm the payment voucher
	public void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = paymentVoucherRepo.find(paymentVoucher.getId());
		
		try{				
			Beans.get(PaymentVoucherConfirmService.class).confirmPaymentVoucher(paymentVoucher);
			response.setReload(true);	
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public void printPaymentVoucher(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		
		String name = I18n.get("Payment voucher")+" "+paymentVoucher.getReceiptNo();
		
		String fileLink = ReportFactory.createReport(IReport.PAYMENT_VOUCHER, name+"-${date}")
				.addParam("PaymentVoucherId", paymentVoucher.getId())
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
		
	}	
	
	
}
