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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class PaymentVoucherSequenceService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherSequenceService.class); 
	
	@Inject
	private SequenceService sequenceService;
	
	
	
	public void setReference(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		if (paymentVoucher.getRef() == null || paymentVoucher.getRef().equals("")){
			
			paymentVoucher.setRef(this.getReference(paymentVoucher));
		}
		
	}
	
	
	public String getReference(PaymentVoucher paymentVoucher) throws AxelorException  {
			
		PaymentMode paymentMode = paymentVoucher.getPaymentMode();
		
		if(paymentMode.getBankJournal() == null)  {
			throw new AxelorException(String.format("%s :\n Merci de paramétrer un journal pour le mode de paiement {}", 
					GeneralService.getExceptionAccountingMsg(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}
		
		String sequence = sequenceService.getSequence(IAdministration.PAYMENT_VOUCHER, paymentVoucher.getCompany(), paymentMode.getBankJournal(), false);
		if(sequence != null)  {
			return sequence;
		}
		else  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de saisie paiement pour la société %s et le journal %s.", 
					GeneralService.getExceptionAccountingMsg(), paymentVoucher.getCompany().getName(), paymentMode.getBankJournal().getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public void setReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)  {
		
		if(journal.getEditReceiptOk())  {
			
			paymentVoucher.setReceiptNo(this.getReceiptNo(paymentVoucher, company, journal));
		
		}
	}
	
	public String getReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)  {
		
		return sequenceService.getSequence(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company, false);
	
	}
	
	
	public void checkReceipt(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		Company company = paymentVoucher.getCompany();
		
		String seq = sequenceService.getSequence(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company, true);
		
		if(seq == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Numéro de reçu (Saisie paiement) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
}
