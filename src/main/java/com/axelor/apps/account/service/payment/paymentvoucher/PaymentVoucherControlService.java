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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class PaymentVoucherControlService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherControlService.class); 
	
	@Inject
	private PaymentVoucherSequenceService paymentVoucherSequenceService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	
	/**
	 * Procédure permettant de vérifier le remplissage et le bon contenu des champs de la saisie paiement et de la société
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 * @param company
	 * 			Une société
	 * @param paymentModeAccount
	 * 			Le compte de trésoreie du mode de règlement
	 * @throws AxelorException
	 */
	public void checkPaymentVoucherField(PaymentVoucher paymentVoucher, Company company, Account paymentModeAccount, Journal journal) throws AxelorException  {
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0)  {
			throw new AxelorException(String.format("%s :\n Attention, saisie paiement n° %s, le total des montants imputés par ligne est supérieur au montant payé par le client", 
					GeneralService.getExceptionAccountingMsg(), paymentVoucher.getRef()), IException.INCONSISTENCY);
		}
		
		// Si on a des lignes à payer (dans le deuxième tableau)
		if(!paymentVoucher.getHasAutoInput() && (paymentVoucher.getPaymentInvoiceToPayList() == null || paymentVoucher.getPaymentInvoiceToPayList().size() == 0))  {
			throw new AxelorException(String.format("%s :\n Aucune ligne à payer.", GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}	
		
		accountConfigService.getCustomerAccount(accountConfigService.getAccountConfig(company));
		
		if(journal == null || paymentModeAccount == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez renseigner un journal et un compte de trésorerie dans le mode de règlement.", 
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		
		if(journal.getEditReceiptOk())  {
			paymentVoucherSequenceService.checkReceipt(paymentVoucher);
		}
	}
	
	
	public void checkPayboxAmount(PaymentVoucher paymentVoucher) throws AxelorException  {
		if(paymentVoucher.getPayboxAmountPaid() != null && paymentVoucher.getPayboxAmountPaid().compareTo(paymentVoucher.getPaidAmount()) != 0)  {
				throw new AxelorException(String.format("%s :\n Le montant de la saisie paiement (%s) est différent du montant encaissé par Paybox (%s)",
						GeneralService.getExceptionAccountingMsg(),paymentVoucher.getPaidAmount(),paymentVoucher.getPayboxAmountPaid()), IException.INCONSISTENCY);
		}
	}
	
	
	/**
	 * Fonction vérifiant si l'ensemble des lignes à payer ont le même compte et que ce compte est le même que celui du trop-perçu
	 * @param paymentInvoiceToPayList
	 * 			La liste des lignes à payer
	 * @param moveLine
	 * 			Le trop-perçu à utiliser
	 * @return
	 */
	public boolean checkIfSameAccount(List<PaymentInvoiceToPay> paymentInvoiceToPayList, MoveLine moveLine)  {
		if(moveLine != null)  {
			Account account = moveLine.getAccount();
			for (PaymentInvoiceToPay paymentInvoiceToPay : paymentInvoiceToPayList)  {
				if(!paymentInvoiceToPay.getMoveLine().getAccount().equals(account))  {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	
}
