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

import java.math.BigDecimal;

import com.axelor.apps.account.db.*;
import org.joda.time.LocalDate;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class InvoicePaymentCreateServiceImpl  implements  InvoicePaymentCreateService {
	
	protected InvoicePaymentRepository invoicePaymentRepository;
	protected InvoicePaymentToolService invoicePaymentToolService;
	protected CurrencyService currencyService;
	
	@Inject
	public InvoicePaymentCreateServiceImpl(InvoicePaymentRepository invoicePaymentRepository, InvoicePaymentToolService invoicePaymentToolService, 
			CurrencyService currencyService)  {
		
		this.invoicePaymentRepository = invoicePaymentRepository;
		this.invoicePaymentToolService = invoicePaymentToolService;
		this.currencyService = currencyService;
		
	}
	
	/**
	 * 
	 * @param amount
	 * @param paymentDate
	 * @param currency
	 * @param paymentMode
	 * @param invoice
	 * @param typeSelect
	 * 			1 : Advance Payment
	 * 			2 : Payment
	 * 			3 : Refund invoice
	 * 			4 : Other
	 * @return
	 */
	public InvoicePayment createInvoicePayment(Invoice invoice, BigDecimal amount, LocalDate paymentDate, Currency currency, PaymentMode paymentMode,  int typeSelect)  {
		
		return new InvoicePayment(amount, paymentDate, currency, paymentMode, invoice, typeSelect, InvoicePaymentRepository.STATUS_DRAFT);

	}
	
	/**
	 * @param invoice
	 * @param amount
	 * @param paymentDate
	 * @param paymentMove
	 * @return
	 */
	public InvoicePayment createInvoicePayment(Invoice invoice, BigDecimal amount, Move paymentMove) throws AxelorException  {
		
		LocalDate paymentDate = paymentMove.getDate();
		BigDecimal amountConverted = currencyService.getAmountCurrencyConvertedAtDate(paymentMove.getCompanyCurrency(), paymentMove.getCurrency(), amount, paymentDate);
		InvoicePayment invoicePayment = this.createInvoicePayment(invoice, amountConverted, paymentDate, paymentMove.getCurrency(), paymentMove.getPaymentMode(), this.determineType(paymentMove));
		invoicePayment.setMove(paymentMove);
		invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		PaymentVoucher paymentVoucher = paymentMove.getPaymentVoucher();
		if (paymentVoucher != null) {
			invoicePayment.setBankDetails(paymentVoucher.getCompanyBankDetails());
		}
		invoice.addInvoicePaymentListItem(invoicePayment);
		invoicePaymentToolService.updateAmountPaid(invoice);
		invoicePaymentRepository.save(invoicePayment);
		
		return invoicePayment;
	}
	
	
	protected int determineType(Move move)  {
		
		Invoice invoice = move.getInvoice();
		if(invoice != null)  {
			if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)   {
				return InvoicePaymentRepository.TYPE_INVOICE;
			}
			else  {
				return InvoicePaymentRepository.TYPE_REFUND_INVOICE;
			}
		}
		else if (move.getPaymentVoucher() != null)  {
			return InvoicePaymentRepository.TYPE_PAYMENT;
		}
		else  {
			return InvoicePaymentRepository.TYPE_OTHER;
		}
	
	}
	
	
}
