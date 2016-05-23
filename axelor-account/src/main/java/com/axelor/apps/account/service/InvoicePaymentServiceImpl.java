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
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePaymentServiceImpl  implements  InvoicePaymentService {
	
	protected PaymentModeService paymentModeService;
	protected MoveService moveService;
	protected MoveLineService moveLineService;
	protected CurrencyService currencyService;
	protected AccountConfigService accountConfigService;
	protected InvoicePaymentRepository invoicePaymentRepository;
	protected MoveCancelService moveCancelService;
	protected InvoiceService invoiceService;
	protected ReconcileService reconcileService;
	
	@Inject
	public InvoicePaymentServiceImpl(PaymentModeService paymentModeService, MoveService moveService, MoveLineService moveLineService, 
			CurrencyService currencyService, AccountConfigService accountConfigService, InvoicePaymentRepository invoicePaymentRepository, 
			MoveCancelService moveCancelService, InvoiceService invoiceService, ReconcileService reconcileService)  {
		
		this.paymentModeService = paymentModeService;
		this.moveService = moveService;
		this.moveLineService = moveLineService;
		this.currencyService = currencyService;
		this.accountConfigService = accountConfigService;
		this.invoicePaymentRepository = invoicePaymentRepository;
		this.moveCancelService = moveCancelService;
		this.invoiceService = invoiceService;
		this.reconcileService = reconcileService;
		
	}
	
	/**
	 * Method to validate an invoice Payment
	 * 
	 * Create the eventual move (depending general configuration) and reconcile it with the invoice move
	 * Compute the amount paid on invoice
	 * Change the status to validated
	 * 
	 * @param invoicePayment
	 * 			An invoice payment
	 * 
	 * @throws AxelorException
	 * 		
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(InvoicePayment invoicePayment) throws AxelorException  {
		
		if(invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_DRAFT)  {  return;  }
		
		invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		
		Company company = invoicePayment.getInvoice().getCompany();
				
		if(accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment())  {
			this.createMoveForInvoicePayment(invoicePayment);
		}
		
		invoiceService.updateAmountPaid(invoicePayment.getInvoice());
		invoicePaymentRepository.save(invoicePayment);
	}
	
	
	/**
	 * Method to create a payment move for an invoice Payment
	 * 
	 * Create a move and reconcile it with the invoice move
	 * 
	 * @param invoicePayment
	 * 			An invoice payment
	 * 
	 * @throws AxelorException
	 * 		
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createMoveForInvoicePayment(InvoicePayment invoicePayment) throws AxelorException  {
		
		Invoice invoice = invoicePayment.getInvoice();
		Company company = invoice.getCompany();
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		Partner partner = invoice.getPartner();
		LocalDate paymentDate = invoicePayment.getPaymentDate();
		
		Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company);
		
		boolean isDebitInvoice = moveService.getMoveToolService().isDebitCustomer(invoice, true);
		
		MoveLine invoiceMoveLine = moveService.getMoveToolService().getInvoiceCustomerMoveLineByLoop(invoice);
		
		Move move = moveService.getMoveCreateService().createMove(journal, company, null, partner, paymentDate, paymentMode);
		
		BigDecimal amountConverted = currencyService.getAmountCurrencyConverted(invoicePayment.getCurrency(), invoice.getCurrency(), invoicePayment.getAmount(), paymentDate);
		
		move.addMoveLineListItem(moveLineService.createMoveLine(move, partner, paymentModeService.getPaymentModeAccount(paymentMode, company), 
				amountConverted, isDebitInvoice, paymentDate, null, 1, ""));
		
		MoveLine customerMoveLine = moveLineService.createMoveLine(move, partner, invoiceMoveLine.getAccount(), 
				amountConverted, !isDebitInvoice, paymentDate, null, 2, "");
		
		move.addMoveLineListItem(customerMoveLine);
		
		moveService.getMoveValidateService().validate(move);
		
		Reconcile reconcile = reconcileService.reconcile(invoiceMoveLine, customerMoveLine, true);
		
		invoicePayment.setReconcile(reconcile);
		invoicePayment.setMove(move);
		
		invoicePaymentRepository.save(invoicePayment);
		
		return move;
	}
	
	
	/**
	 * Method to cancel an invoice Payment
	 * 
	 * Cancel the eventual Move and Reconcile
	 * Compute the total amount paid on the linked invoice
  	 * Change the status to cancel
	 * 
	 * @param invoicePayment
	 * 			An invoice payment
	 * 
	 * @throws AxelorException
	 * 		
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(InvoicePayment invoicePayment) throws AxelorException  {
		
		Move paymentMove = invoicePayment.getMove();
		Reconcile reconcile = invoicePayment.getReconcile();
		
		if(reconcile != null && reconcile.getStatusSelect() == ReconcileRepository.STATUS_CONFIRMED)  {
			reconcileService.unreconcile(reconcile);
			if(accountConfigService.getAccountConfig(invoicePayment.getInvoice().getCompany()).getAllowRemovalValidatedMove())  {
				invoicePayment.setReconcile(null);
				Beans.get(ReconcileRepository.class).remove(reconcile);
			}

		}
		
		if(paymentMove != null)  {
			invoicePayment.setMove(null);
			moveCancelService.cancel(paymentMove);
		}
		
		invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
		invoicePaymentRepository.save(invoicePayment);

		invoiceService.updateAmountPaid(invoicePayment.getInvoice());
			
	}
	
}
