/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PaymentVoucherController {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherController.class);
	
	@Inject
	private Provider<PaymentVoucherLoadService> paymentVoucherLoadProvider;
	
	@Inject
	private Provider<PaymentVoucherConfirmService> paymentVoucherConfirmProvider;
	
	@Inject
	private Provider<PaymentVoucherSequenceService> paymentVoucherSequenceProvider;

	
	//Called on onSave event
	public void paymentVoucherSetNum(ActionRequest request, ActionResponse response) throws AxelorException{

		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

		if (Strings.isNullOrEmpty(paymentVoucher.getRef()))  {

			response.setValue("ref", paymentVoucherSequenceProvider.get().getReference(paymentVoucher));

		}
	}
	
	// Loading move lines of the selected partner (1st O2M)
	public void loadMoveLines(ActionRequest request, ActionResponse response) {

		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = PaymentVoucher.find(paymentVoucher.getId());
		
		try {
			paymentVoucherLoadProvider.get().loadMoveLines(paymentVoucher);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Filling lines to pay (2nd O2M)
	public void loadSelectedLines(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucherContext = request.getContext().asType(PaymentVoucher.class);
		PaymentVoucher paymentVoucher = PaymentVoucher.find(paymentVoucherContext.getId());
			
		try {
			paymentVoucherLoadProvider.get().loadSelectedLines(paymentVoucher,paymentVoucherContext);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Confirm the payment voucher
	public void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {
				
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = PaymentVoucher.find(paymentVoucher.getId());
		
		try{				
			paymentVoucherConfirmProvider.get().confirmPaymentVoucher(paymentVoucher, false);
			response.setReload(true);	
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public void printPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		StringBuilder url = new StringBuilder();
		AppSettings appSettings = AppSettings.get();
		url.append(appSettings.get("axelor.report.engine","")+"/frameset?__report=report/PaymentVoucher.rptdesign&__format=pdf&PaymentVoucherId="+paymentVoucher.getId()+AxelorSettings.getAxelorReportEngineDatasource());
		
		LOG.debug("Follow the URL: "+url);
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Reçu saisie paiement "+paymentVoucher.getReceiptNo());
		mapView.put("resource", url);
		mapView.put("viewType", "html");
		response.setView(mapView);	
	}	
}
