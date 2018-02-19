/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePaymentCreateServiceImpl  implements  InvoicePaymentCreateService {
	
	protected InvoicePaymentRepository invoicePaymentRepository;
	protected InvoicePaymentToolService invoicePaymentToolService;
	protected CurrencyService currencyService;
	protected GeneralService generalService;
	
	@Inject
	public InvoicePaymentCreateServiceImpl(InvoicePaymentRepository invoicePaymentRepository, InvoicePaymentToolService invoicePaymentToolService, 
			CurrencyService currencyService, GeneralService generalService)  {
		
		this.invoicePaymentRepository = invoicePaymentRepository;
		this.invoicePaymentToolService = invoicePaymentToolService;
		this.currencyService = currencyService;
		this.generalService = generalService;
		
	}
	
	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public InvoicePayment createInvoicePayment(Invoice invoice, BankDetails bankDetails) {
		InvoicePayment invoicePayment = createInvoicePayment(
				invoice,
				invoice.getInTaxTotal().subtract(invoice.getAmountPaid()),
				generalService.getTodayDate(),
				invoice.getCurrency(),
				invoice.getPaymentMode(),
				InvoicePaymentRepository.TYPE_PAYMENT);
		invoicePayment.setBankDetails(bankDetails);
		return invoicePaymentRepository.save(invoicePayment);
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
		int typePaymentMove = this.determineType(paymentMove);
		Currency currency = paymentMove.getCurrency();
		PaymentMode paymentMode;
		InvoicePayment invoicePayment;
		if(typePaymentMove == InvoicePaymentRepository.TYPE_REFUND_INVOICE || typePaymentMove == InvoicePaymentRepository.TYPE_INVOICE){
			paymentMode = null;
		}
		else{
			paymentMode = paymentMove.getPaymentMode();
		}
		invoicePayment = this.createInvoicePayment(invoice, amountConverted, paymentDate, currency, paymentMode, typePaymentMove);
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
