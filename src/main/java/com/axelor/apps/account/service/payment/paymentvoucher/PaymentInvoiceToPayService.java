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
package com.axelor.apps.account.service.payment.paymentvoucher;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;

public class PaymentInvoiceToPayService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentInvoiceToPayService.class); 
	
	/**
	 * Generic method for creating invoice to pay lines (2nd O2M in the view)
	 * @param pv
	 * @param seq
	 * @return
	 */
	public PaymentInvoiceToPay createPaymentInvoiceToPay(PaymentVoucher pv,int seq,Invoice invoice,MoveLine ml,BigDecimal totalAmount,BigDecimal remainingAmount,BigDecimal amountToPay){
		
		LOG.debug("In  createPaymentInvoiceToPay....");
		
		if (pv != null && ml != null){
			PaymentInvoiceToPay piToPay= new PaymentInvoiceToPay();
			piToPay.setSequence(seq);
			piToPay.setMoveLine(ml);
			piToPay.setTotalAmount(totalAmount);
			piToPay.setRemainingAmount(remainingAmount);
			piToPay.setAmountToPay(amountToPay);
			piToPay.setPaymentVoucher(pv);
			
			LOG.debug("End createPaymentInvoiceToPay IF.");
			
			return piToPay;
		}
		else{
			LOG.debug("End createPaymentInvoiceToPay ELSE.");
			return null;
		}
	}
	
}
