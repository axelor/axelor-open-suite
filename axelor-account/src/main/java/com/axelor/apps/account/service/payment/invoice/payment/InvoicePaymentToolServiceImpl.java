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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoicePaymentToolServiceImpl  implements  InvoicePaymentToolService {
	
	protected InvoiceRepository invoiceRepo;
	protected MoveToolService moveToolService;

	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	public InvoicePaymentToolServiceImpl(InvoiceRepository invoiceRepo, MoveToolService moveToolService)  {

		this.invoiceRepo = invoiceRepo;
		this.moveToolService = moveToolService;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateAmountPaid(Invoice invoice) throws AxelorException  {

		invoice.setAmountPaid(computeAmountPaid(invoice));
		invoice.setAmountRemaining(invoice.getInTaxTotal().subtract(invoice.getAmountPaid()));
		invoice.setAmountPendingRemaining(invoice.getInTaxTotal().subtract(computeAmountPaid(invoice, true)));
		invoiceRepo.save(invoice);
		log.debug("Invoice : {}, amount paid : {}", invoice.getInvoiceId(), invoice.getAmountPaid());
		
	}

	protected BigDecimal computeAmountPaid(Invoice invoice) throws AxelorException {
		return computeAmountPaid(invoice, false);
	}

	protected BigDecimal computeAmountPaid(Invoice invoice, boolean withPending) throws AxelorException  {
		
		BigDecimal amountPaid = BigDecimal.ZERO;
		
		if(invoice.getInvoicePaymentList() == null)  {  return amountPaid;  }
		
		CurrencyService currencyService = Beans.get(CurrencyService.class);
		
		Currency invoiceCurrency = invoice.getCurrency();

		List<Integer> statusSelectList = Lists.newArrayList(InvoicePaymentRepository.STATUS_VALIDATED);

		if (withPending) {
			statusSelectList.add(InvoicePaymentRepository.STATUS_PENDING);
		}

		for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList()) {

			if (statusSelectList.contains(invoicePayment.getStatusSelect())) {
				
				log.debug("Amount paid without move : {}", invoicePayment.getAmount());
				
				amountPaid = amountPaid.add(currencyService.getAmountCurrencyConvertedAtDate(invoicePayment.getCurrency(), invoiceCurrency, invoicePayment.getAmount(), invoicePayment.getPaymentDate()));
			}
			
		}
		
		boolean isMinus = moveToolService.isMinus(invoice);
		if(isMinus)  {
			amountPaid = amountPaid.negate();
		}
		
		log.debug("Amount paid total : {}", amountPaid);
		
		return amountPaid;
	}


	/**
	 * @inheritDoc
	 */
	public List<BankDetails> findCompatibleBankDetails(Company company, InvoicePayment invoicePayment){
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		if(company == null || paymentMode == null) { return new ArrayList<BankDetails>(); }
		paymentMode = Beans.get(PaymentModeRepository.class).find(invoicePayment.getPaymentMode().getId());
		return Beans.get(PaymentModeService.class).
				getCompatibleBankDetailsList(paymentMode, company);
    }
}
