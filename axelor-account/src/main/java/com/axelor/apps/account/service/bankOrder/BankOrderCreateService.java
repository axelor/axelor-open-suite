package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderCreateService {

	private final Logger log = LoggerFactory.getLogger( getClass() );
	protected BankOrderRepository bankOrderRepo;
	protected AccountConfigService accountConfigService;
	protected BankOrderLineService bankOrderLineService;

	
	@Inject
	public BankOrderCreateService(BankOrderRepository bankOrderRepo, AccountConfigService accountConfigService, BankOrderLineService bankOrderLineService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.accountConfigService = accountConfigService;
		this.bankOrderLineService = bankOrderLineService;
		
	}
	
	/**
	 * Créer un ordre bancaire avec tous les paramètres
	 *
	 * @param 
	 * @return
	 * @throws AxelorException
	 */
	public BankOrder createBankOrder(PaymentMode paymentMode, Integer partnerType, LocalDate bankOrderDate, 
			Company senderCompany, BankDetails senderBankDetails, BigDecimal amount, Currency currency,
			String senderReference, String senderLabel) throws AxelorException  {
		
		BankOrder bankOrder = new BankOrder();
		
		bankOrder.setOrderTypeSelect(paymentMode.getOrderTypeSelect());
		bankOrder.setPaymentMode(paymentMode);
		bankOrder.setPartnerTypeSelect(partnerType);
		bankOrder.setBankOrderDate(bankOrderDate);
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_DRAFT);
		bankOrder.setRejectStatusSelect(BankOrderRepository.REJECT_STATUS_NOT_REJECTED);
		bankOrder.setSenderCompany(senderCompany);
		bankOrder.setSenderBankDetails(senderBankDetails);
		bankOrder.setAmount(amount);
		bankOrder.setSignatoryUser(accountConfigService.getAccountConfig(senderCompany).getDefaultSignatoryUser());
		bankOrder.setCurrency(currency);
		bankOrder.setSenderLabel(senderLabel);
		bankOrder.setBankOrderLineList(new ArrayList<BankOrderLine>());
		bankOrderRepo.save(bankOrder);
		
		return bankOrder;
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
	public BankOrder createBankOrder(InvoicePayment invoicePayment) throws AxelorException {
		Invoice invoice = invoicePayment.getInvoice();
		Company company = invoice.getCompany();
		PaymentMode paymentMode = invoicePayment.getPaymentMode();
		Partner partner = invoice.getPartner();
		BigDecimal amount = invoicePayment.getAmount();
		
		BankOrder bankOrder = this.createBankOrder( 
								paymentMode,
								this.getBankOrderPartnerType(invoice),
								invoicePayment.getPaymentDate(),
								company,
								this.getSenderBankDetails(invoice),
								amount,
								invoice.getCurrency(),
								invoice.getInvoiceId(),
								invoice.getInvoiceId());
		
		bankOrder.addBankOrderLineListItem(bankOrderLineService.createBankOrderLine(partner, amount, null, null));
		
		return bankOrder;
		
	}
	
	
	public int getBankOrderPartnerType(Invoice invoice)  {

		if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)  {
			return BankOrderRepository.CUSTOMER;
		}else{
			return BankOrderRepository.SUPPLIER;
		}
		
	}
	
	
	public BankDetails getSenderBankDetails(Invoice invoice)  {
		
		if(invoice.getBankDetails() != null)  {
			return invoice.getCompanyBankDetails() ;
		}  
		
		return invoice.getCompany().getDefaultBankDetails();
	}
	
}
