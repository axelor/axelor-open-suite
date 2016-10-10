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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.bankOrder.BankOrderCreateService;
import com.axelor.apps.account.service.bankOrder.BankOrderLineService;
import com.axelor.apps.account.service.bankOrder.BankOrderService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
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
	protected BankOrderCreateService bankOrderCreateService;
	protected BankOrderLineService bankOrderLineService;
	protected BankOrderService bankOrderService;
	protected BankDetailsRepository bankDetailsRepo;
	protected BankOrderRepository bankOrderRepo;
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	@Inject
	public InvoicePaymentServiceImpl(PaymentModeService paymentModeService, MoveService moveService, MoveLineService moveLineService, 
			CurrencyService currencyService, AccountConfigService accountConfigService, InvoicePaymentRepository invoicePaymentRepository, 
			MoveCancelService moveCancelService, InvoiceService invoiceService, ReconcileService reconcileService, BankOrderCreateService bankOrderCreateService,
			BankOrderLineService bankOrderLineService,BankOrderService bankOrderService, BankDetailsRepository bankDetailsRepo)  {
		
		this.paymentModeService = paymentModeService;
		this.moveService = moveService;
		this.moveLineService = moveLineService;
		this.currencyService = currencyService;
		this.accountConfigService = accountConfigService;
		this.invoicePaymentRepository = invoicePaymentRepository;
		this.moveCancelService = moveCancelService;
		this.invoiceService = invoiceService;
		this.reconcileService = reconcileService;
		this.bankOrderCreateService = bankOrderCreateService;
		this.bankOrderLineService = bankOrderLineService;
		this.bankOrderService = bankOrderService;
		this.bankDetailsRepo = bankDetailsRepo;
		
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
		
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		int typeSelect = paymentMode.getTypeSelect();
		int inOutSelect = paymentMode.getInOutSelect();
		if(invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_DRAFT)  {  return;  }
		
		if( (typeSelect == PaymentModeRepository.TYPE_DD || typeSelect == PaymentModeRepository.TYPE_TRANSFER) && inOutSelect == PaymentModeRepository.OUT){
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
		}else{
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		}
		Company company = invoicePayment.getInvoice().getCompany();
				
		if(accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment())  {
			this.createMoveForInvoicePayment(invoicePayment);
		}
		if(invoicePayment.getPaymentMode().getGenerateBankOrder() == true){
			this.createBankOrderForInvoicePayment(invoicePayment);
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
		
		Move move = moveService.getMoveCreateService().createMove(journal, company, null, partner, paymentDate, paymentMode, MoveRepository.AUTOMATIC);
		
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
	 * Method to create a bank order for an invoice Payment
	 * 
	 * 
	 * @param invoicePayment
	 * 			An invoice payment
	 * 
	 * @throws AxelorException
	 * 		
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BankOrder createBankOrderForInvoicePayment(InvoicePayment invoicePayment) throws AxelorException {
		Invoice invoice = invoicePayment.getInvoice();
		Company company = invoice.getCompany();
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		Partner partner = invoice.getPartner();
		LocalDate paymentDate = invoicePayment.getPaymentDate();
		int orderType = paymentMode.getOrderType();
		BigDecimal amount = invoicePayment.getAmount();
		User signatory= accountConfigService.getAccountConfig(company).getDefaultSignatoryUser();
		
		int partnerType = 0;
		int statusSelect = 0;
		BankDetails senderBankDetails = new BankDetails();
		if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
			partnerType = BankOrderRepository.CUSTOMER;
		}else{
			partnerType = BankOrderRepository.SUPPLIER;
		}
		if (orderType == BankOrderRepository.SEPA_CREDIT_TRANSFER || orderType == BankOrderRepository.INTERNATIONAL_CREDIT_TRANSFER){
			statusSelect = BankOrderRepository.STATUS_AWAITING_SIGNATURE;
		}
		else{
			statusSelect = BankOrderRepository.STATUS_DRAFT;
		}
		if(invoice.getBankDetails() != null){
			senderBankDetails = invoice.getBankDetails() ;
		}else{
			senderBankDetails = company.getDefaultBankDetails();
		}
		BankOrder bankOrder = bankOrderCreateService.
				createBankOrder( 
						orderType , 
						paymentMode,
						partnerType,
						paymentDate,
						statusSelect,
						0,
						company,
						senderBankDetails,
						amount,
						signatory,
						invoice.getCurrency(),
						invoice.getInvoiceId(),
						invoice.getInvoiceId());
		
		BankDetails receiverBankDetails = bankDetailsRepo.all().filter("self.partner = ?1 AND self.isDefault = true", partner).fetchOne();
		
		bankOrder.addBankOrderLineListItem(bankOrderLineService.createBankOrderLine(partner, receiverBankDetails, amount, null, null));
		
		invoicePayment.setBankOrder(bankOrder);
		log.debug("BANK ORDER INVOICE PAYMENT {}", bankOrder );
		
		invoicePaymentRepository.save(invoicePayment);
		return bankOrder;
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
		BankOrder paymentBankOrder = invoicePayment.getBankOrder();
		Reconcile reconcile = invoicePayment.getReconcile();
		
		if(paymentBankOrder != null){
			if(paymentBankOrder.getStatusSelect() == BankOrderRepository.STATUS_CARRIED_OUT || paymentBankOrder.getStatusSelect() == BankOrderRepository.STATUS_REJECTED){
				throw new AxelorException(I18n.get(IExceptionMessage.INVOICE_PAYMENT_CANCEL), IException.FUNCTIONNAL);
			}
			else{
				invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
				bankOrderService.cancelBankOrder(paymentBankOrder);
			}
		}else
		{
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

}
