/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class PaymentVoucherController {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherController.class);
	
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
		PaymentVoucherLoadService paymentVoucherLoadService = Beans.get(PaymentVoucherLoadService.class); 
		paymentVoucher = paymentVoucherLoadService.find(paymentVoucher.getId());
		
		try {
			paymentVoucherLoadService.loadMoveLines(paymentVoucher);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Filling lines to pay (2nd O2M)
	public void loadSelectedLines(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucherContext = request.getContext().asType(PaymentVoucher.class);
		PaymentVoucherLoadService paymentVoucherLoadService = Beans.get(PaymentVoucherLoadService.class);
		PaymentVoucher paymentVoucher = paymentVoucherLoadService.find(paymentVoucherContext.getId());
			
		try {
			paymentVoucherLoadService.loadSelectedLines(paymentVoucher,paymentVoucherContext);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Confirm the payment voucher
	public void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = Beans.get(PaymentVoucherLoadService.class).find(paymentVoucher.getId());
		
		try{				
			Beans.get(PaymentVoucherConfirmService.class).confirmPaymentVoucher(paymentVoucher, false);
			response.setReload(true);	
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public void printPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		StringBuilder url = new StringBuilder();
		
		url.append(new ReportSettings(IReport.PAYMENT_VOUCHER)
					.addParam("PaymentVoucherId", paymentVoucher.getId().toString())
					.getUrl());
		
		LOG.debug("Follow the URL: "+url);
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", I18n.get(IExceptionMessage.PAYMENT_VOUCHER_1)+" "+paymentVoucher.getReceiptNo());
		mapView.put("resource", url);
		mapView.put("viewType", "html");
		response.setView(mapView);	
	}	
}
