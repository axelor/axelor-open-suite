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
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.CashRegister;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherCreateService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherCreateService.class); 
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private PaymentInvoiceToPayService paymentInvoiceToPayService;
	
	@Inject
	private PaymentVoucherConfirmService paymentVoucherConfirmService;

	@Inject
	private PaymentVoucherSequenceService paymentVoucherSequenceService;

	private DateTime todayTime;

	@Inject
	public PaymentVoucherCreateService() {

		this.todayTime = GeneralService.getTodayDateTime();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher createPaymentVoucherIPO(Invoice invoice, DateTime dateTime, BigDecimal amount, PaymentMode paymentMode) throws AxelorException  {
		MoveLine customerMoveLine = moveService.getCustomerMoveLineByQuery(invoice);
		
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - facture : {}",invoice.getInvoiceId());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - mode de paiement : {}",paymentMode.getCode());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - société : {}",invoice.getCompany().getName());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - tiers payeur : {}",invoice.getPartner().getName());  }
		
		PaymentVoucher paymentVoucher = this.createPaymentVoucher(
				invoice.getCompany(), 
				null, 
				null,
				paymentMode, 
				dateTime, 
				invoice.getPartner(), 
				amount, 
				null,
				invoice,
				null, 
				null, 
				null);
		
		paymentVoucher.setHasAutoInput(true);
		
		List<PaymentInvoiceToPay> lines = new ArrayList<PaymentInvoiceToPay>();
	
		lines.add(paymentInvoiceToPayService.createPaymentInvoiceToPay(paymentVoucher, 
				1, 
				invoice, 
				customerMoveLine, 
				customerMoveLine.getDebit(), 
				customerMoveLine.getAmountRemaining(),
				amount));
		
		paymentVoucher.setPaymentInvoiceToPayList(lines);
		
		paymentVoucher.save();
		
		paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher, false);
		return paymentVoucher;
	}
	
	
	/**
	 * Generic method to create a payment voucher
	 * @param seq
	 * @param pm
	 * @param partner
	 * @return
	 * @throws AxelorException 
	 */
	public PaymentVoucher createPaymentVoucher(Company company, CashRegister cashRegister, UserInfo userInfo, PaymentMode paymentMode, DateTime dateTime, Partner partner,
			BigDecimal amount, MoveLine moveLine, Invoice invoiceToPay, MoveLine rejectToPay,
			PaymentScheduleLine scheduleToPay, PaymentSchedule paymentScheduleToPay) throws AxelorException  {

		LOG.debug("\n\n createPaymentVoucher ....");
		DateTime dateTime2 = dateTime;
		if(dateTime2 == null)  {
			dateTime2 = this.todayTime;
		}

		BigDecimal amount2 = amount;
		if(amount2 == null )  {
			amount2 = BigDecimal.ZERO;
		}

		//create the move
		PaymentVoucher paymentVoucher= new PaymentVoucher();
		if (company != null && paymentMode != null && partner != null)  {
			paymentVoucher.setCompany(company);
			paymentVoucher.setCashRegister(cashRegister);
			paymentVoucher.setUserInfo(userInfo);
			paymentVoucher.setPaymentDateTime(dateTime2);

			paymentVoucher.setPaymentMode(paymentMode);
			paymentVoucher.setPartner(partner);

			paymentVoucher.setInvoiceToPay(invoiceToPay);
			paymentVoucher.setRejectToPay(rejectToPay);

			paymentVoucher.setPaidAmount(amount2);
			paymentVoucher.setMoveLine(moveLine);

			paymentVoucherSequenceService.setReference(paymentVoucher);

			return paymentVoucher;
		}

		return null;
	}

	
}
