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
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherCreateService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveToolService moveToolService;
	protected PaymentInvoiceToPayService paymentInvoiceToPayService;
	protected PaymentVoucherConfirmService paymentVoucherConfirmService;
	protected PaymentVoucherSequenceService paymentVoucherSequenceService;
	protected PaymentVoucherRepository paymentVoucherRepository;
	protected DateTime todayTime;

	@Inject
	public PaymentVoucherCreateService(GeneralService generalService, MoveToolService moveToolService, PaymentInvoiceToPayService paymentInvoiceToPayService, 
			PaymentVoucherConfirmService paymentVoucherConfirmService, PaymentVoucherSequenceService paymentVoucherSequenceService,
			PaymentVoucherRepository paymentVoucherRepository) {

		this.moveToolService = moveToolService;
		this.paymentInvoiceToPayService = paymentInvoiceToPayService;
		this.paymentVoucherConfirmService = paymentVoucherConfirmService;
		this.paymentVoucherSequenceService = paymentVoucherSequenceService;
		this.paymentVoucherRepository = paymentVoucherRepository;
		this.todayTime = generalService.getTodayDateTime();

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher createPaymentVoucherIPO(Invoice invoice, DateTime dateTime, BigDecimal amount, PaymentMode paymentMode) throws AxelorException  {
		MoveLine customerMoveLine = moveToolService.getCustomerMoveLineByQuery(invoice);

		log.debug("Création d'une saisie paiement par TIP ou TIP chèque - facture : {}",invoice.getInvoiceId());
		log.debug("Création d'une saisie paiement par TIP ou TIP chèque - mode de paiement : {}",paymentMode.getCode());
		log.debug("Création d'une saisie paiement par TIP ou TIP chèque - société : {}",invoice.getCompany().getName());
		log.debug("Création d'une saisie paiement par TIP ou TIP chèque - tiers payeur : {}",invoice.getPartner().getName());

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

		paymentVoucherRepository.save(paymentVoucher);

		paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher);
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
	public PaymentVoucher createPaymentVoucher(Company company, CashRegister cashRegister, User user, PaymentMode paymentMode, DateTime dateTime, Partner partner,
			BigDecimal amount, MoveLine moveLine, Invoice invoiceToPay, MoveLine rejectToPay,
			PaymentScheduleLine scheduleToPay, PaymentSchedule paymentScheduleToPay) throws AxelorException  {

		log.debug("\n\n createPaymentVoucher ....");
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
			paymentVoucher.setUser(user);
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
