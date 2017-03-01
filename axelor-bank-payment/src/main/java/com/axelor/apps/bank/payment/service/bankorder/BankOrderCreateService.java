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
package com.axelor.apps.bank.payment.service.bankorder;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.bank.payment.db.BankOrder;
import com.axelor.apps.bank.payment.db.BankOrderFileFormat;
import com.axelor.apps.bank.payment.db.BankOrderLine;
import com.axelor.apps.bank.payment.db.repo.BankOrderRepository;
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
			Company senderCompany, BankDetails senderBankDetails, Currency currency,
			String senderReference, String senderLabel) throws AxelorException  {
		
		BankOrderFileFormat bankOrderFileFormat = paymentMode.getBankOrderFileFormat();
		
		BankOrder bankOrder = new BankOrder();
		
		bankOrder.setOrderTypeSelect(paymentMode.getOrderTypeSelect());
		bankOrder.setPaymentMode(paymentMode);
		bankOrder.setPartnerTypeSelect(partnerType);
		
		if(!bankOrderFileFormat.getIsMultiDate())  {
			bankOrder.setBankOrderDate(bankOrderDate);
		}
		
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_DRAFT);
		bankOrder.setRejectStatusSelect(BankOrderRepository.REJECT_STATUS_NOT_REJECTED);
		bankOrder.setSenderCompany(senderCompany);
		bankOrder.setSenderBankDetails(senderBankDetails);
		bankOrder.setSignatoryUser(accountConfigService.getAccountConfig(senderCompany).getDefaultSignatoryUser());
		
		if(!bankOrderFileFormat.getIsMultiCurrency())  {
			bankOrder.setBankOrderCurrency(currency);
		}
		bankOrder.setCompanyCurrency(senderCompany.getCurrency());
		
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
		Currency currency = invoicePayment.getCurrency();
		LocalDate paymentDate = invoicePayment.getPaymentDate();
		
		BankOrder bankOrder = this.createBankOrder( 
								paymentMode,
								this.getBankOrderPartnerType(invoice),
								paymentDate,
								company,
								this.getSenderBankDetails(invoice),
								currency,
								invoice.getInvoiceId(),
								invoice.getInvoiceId());
		
		bankOrder.addBankOrderLineListItem(bankOrderLineService.createBankOrderLine(paymentMode.getBankOrderFileFormat(), partner, amount, currency, paymentDate, invoice.getInvoiceId(), null));
		
		return bankOrder;
		
	}
	
	
	public int getBankOrderPartnerType(Invoice invoice)  {

		if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)  {
			return BankOrderRepository.PARTNER_TYPE_CUSTOMER;
		}else{
			return BankOrderRepository.PARTNER_TYPE_SUPPLIER;
		}
		
	}
	
	
	public BankDetails getSenderBankDetails(Invoice invoice)  {
		
		if(invoice.getBankDetails() != null)  {
			return invoice.getCompanyBankDetails() ;
		}  
		
		return invoice.getCompany().getDefaultBankDetails();
	}
	
}
