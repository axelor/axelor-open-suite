/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.paymentvoucher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
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
		
		Journal bankJournal = paymentMode.getBankJournal();
		
		if(bankJournal == null)  {
			throw new AxelorException(String.format("%s :\n Merci de paramétrer un journal pour le mode de paiement {}", 
					GeneralServiceAccount.getExceptionAccountingMsg(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}
		
		if(bankJournal.getSequence() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de saisie paiement pour la société %s et le journal %s.", 
					GeneralServiceAccount.getExceptionAccountingMsg(), paymentVoucher.getCompany().getName(), paymentMode.getBankJournal().getName()), IException.CONFIGURATION_ERROR);
		}
		
		return sequenceService.getSequenceNumber(bankJournal.getSequence(), false);
	}
	
	
	public void setReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)  {
		
		if(journal.getEditReceiptOk())  {
			
			paymentVoucher.setReceiptNo(this.getReceiptNo(paymentVoucher, company, journal));
		
		}
	}
	
	public String getReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)  {
		
		return sequenceService.getSequenceNumber(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company);
	
	}
	
	
	public void checkReceipt(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		Company company = paymentVoucher.getCompany();
		
		if(!sequenceService.hasSequence(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company))  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Numéro de reçu (Saisie paiement) pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
}
