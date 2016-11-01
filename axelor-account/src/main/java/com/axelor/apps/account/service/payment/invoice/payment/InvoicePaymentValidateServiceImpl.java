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
package com.axelor.apps.account.service.payment.invoice.payment;

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
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.bankOrder.BankOrderCreateService;
import com.axelor.apps.account.service.bankOrder.BankOrderLineService;
import com.axelor.apps.account.service.bankOrder.BankOrderService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePaymentValidateServiceImpl  implements  InvoicePaymentValidateService  {
	
	protected PaymentModeService paymentModeService;
	protected MoveService moveService;
	protected MoveLineService moveLineService;
	protected AccountConfigService accountConfigService;
	protected InvoicePaymentRepository invoicePaymentRepository;
	protected MoveCancelService moveCancelService;
	protected ReconcileService reconcileService;
	protected BankOrderCreateService bankOrderCreateService;
	protected BankOrderLineService bankOrderLineService;
	protected BankOrderService bankOrderService;
	protected BankDetailsRepository bankDetailsRepo;
	protected BankOrderRepository bankOrderRepo;
	protected InvoicePaymentToolService invoicePaymentToolService;

	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	@Inject
	public InvoicePaymentValidateServiceImpl(PaymentModeService paymentModeService, MoveService moveService, MoveLineService moveLineService, 
			AccountConfigService accountConfigService, InvoicePaymentRepository invoicePaymentRepository, MoveCancelService moveCancelService, 
			ReconcileService reconcileService, BankOrderCreateService bankOrderCreateService, BankOrderLineService bankOrderLineService, 
			BankOrderService bankOrderService, BankDetailsRepository bankDetailsRepo, InvoicePaymentToolService invoicePaymentToolService)  {
		
		this.paymentModeService = paymentModeService;
		this.moveService = moveService;
		this.moveLineService = moveLineService;
		this.accountConfigService = accountConfigService;
		this.invoicePaymentRepository = invoicePaymentRepository;
		this.moveCancelService = moveCancelService;
		this.reconcileService = reconcileService;
		this.bankOrderCreateService = bankOrderCreateService;
		this.bankOrderLineService = bankOrderLineService;
		this.bankOrderService = bankOrderService;
		this.bankDetailsRepo = bankDetailsRepo;
		this.invoicePaymentToolService = invoicePaymentToolService;
		
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
		
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		int typeSelect = paymentMode.getTypeSelect();
		int inOutSelect = paymentMode.getInOutSelect();
		
		if( (typeSelect == PaymentModeRepository.TYPE_DD || typeSelect == PaymentModeRepository.TYPE_TRANSFER) && inOutSelect == PaymentModeRepository.OUT){
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
		}else{
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		}
		
		//TODO assign an automatic reference
		
		Company company = invoicePayment.getInvoice().getCompany();
				
		if(accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment())  {
			this.createMoveForInvoicePayment(invoicePayment);
		}
		if(invoicePayment.getPaymentMode().getGenerateBankOrder() == true)  {
			this.createBankOrderForInvoicePayment(invoicePayment);
		}
		
		invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
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
		BigDecimal paymentAmount = invoicePayment.getAmount();
		
		Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company);
		
		boolean isDebitInvoice = moveService.getMoveToolService().isDebitCustomer(invoice, true);
		
		MoveLine invoiceMoveLine = moveService.getMoveToolService().getInvoiceCustomerMoveLineByLoop(invoice);
		
		Move move = moveService.getMoveCreateService().createMove(journal, company, invoicePayment.getCurrency(), partner, paymentDate, paymentMode, MoveRepository.AUTOMATIC);
		
		move.addMoveLineListItem(moveLineService.createMoveLine(move, partner, paymentModeService.getPaymentModeAccount(paymentMode, company), 
				paymentAmount, isDebitInvoice, paymentDate, null, 1, ""));
		
		MoveLine customerMoveLine = moveLineService.createMoveLine(move, partner, invoiceMoveLine.getAccount(), 
				paymentAmount, !isDebitInvoice, paymentDate, null, 2, "");
		
		move.addMoveLineListItem(customerMoveLine);
		
		moveService.getMoveValidateService().validate(move);
		
		Reconcile reconcile = reconcileService.reconcile(invoiceMoveLine, customerMoveLine, true, false);
		
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
	
	
}
