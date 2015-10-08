package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePaymentServiceImpl  implements  InvoicePaymentService {
	
	@Inject
	protected PaymentModeService paymentModeService;
	
	@Inject
	protected MoveService moveService;
	
	@Inject
	protected MoveLineService moveLineService;
	
	@Inject
	protected CurrencyService currencyService;
	
	@Inject
	protected AccountConfigService accountConfigService;
	
	@Inject
	protected InvoicePaymentRepository invoicePaymentRepository;
	
	@Inject
	protected MoveCancelService moveCancelService;
	
	@Inject
	protected InvoiceService invoiceService;
	
	
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
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createMoveForInvoicePayment(InvoicePayment invoicePayment) throws AxelorException  {
		
		Invoice invoice = invoicePayment.getInvoice();
		Company company = invoice.getCompany();
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		Partner partner = invoice.getPartner();
		LocalDate paymentDate = invoicePayment.getPaymentDate();
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company);
		
		boolean isDebitInvoice = moveService.getMoveToolService().isDebitCustomer(invoice);
		
		Move move = moveService.getMoveCreateService().createMove(journal, company, null, partner, paymentDate, paymentMode);
		
		BigDecimal amountConverted = currencyService.getAmountCurrencyConverted(invoicePayment.getCurrency(), invoice.getCurrency(), invoicePayment.getAmount(), paymentDate);
		
		move.addMoveLineListItem(moveLineService.createMoveLine(move, partner, paymentModeService.getCompanyAccount(paymentMode, company), 
				amountConverted, isDebitInvoice, paymentDate, null, 1, ""));
		
		move.addMoveLineListItem(moveLineService.createMoveLine(move, partner, accountConfigService.getAdvancePaymentAccount(accountConfig), 
				amountConverted, !isDebitInvoice, paymentDate, null, 2, ""));
		
		moveService.getMoveValidateService().validate(move);
		
		invoicePayment.setMove(move);
		invoicePaymentRepository.save(invoicePayment);
		
		return move;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(InvoicePayment invoicePayment) throws AxelorException  {
		
		moveCancelService.cancel(invoicePayment.getMove());
		invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
		invoicePaymentRepository.save(invoicePayment);
		invoiceService.updateAmountPaid(invoicePayment.getInvoice());
			
	}
	
}
