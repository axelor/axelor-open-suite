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
package com.axelor.apps.bankpayment.service.bankorder;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BankOrderMergeServiceImpl implements BankOrderMergeService  {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	protected BankOrderRepository bankOrderRepo;
	protected InvoicePaymentRepository invoicePaymentRepo;
	protected BankOrderLineService bankOrderLineService;
	protected BankOrderCreateService bankOrderCreateService;
	protected BankOrderService bankOrderService;

	
	@Inject
	public BankOrderMergeServiceImpl(BankOrderRepository bankOrderRepo, InvoicePaymentRepository invoicePaymentRepo, 
			BankOrderLineService bankOrderLineService, BankOrderCreateService bankOrderCreateService, BankOrderService bankOrderService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.invoicePaymentRepo = invoicePaymentRepo;
		this.bankOrderLineService = bankOrderLineService;
		this.bankOrderCreateService = bankOrderCreateService;
		this.bankOrderService = bankOrderService;
		
	}
	
	@Transactional
	public BankOrder mergeBankOrderList(List<BankOrder> bankOrderList) throws AxelorException   {
		
		
		if(bankOrderList == null || bankOrderList.size() <= 1)  {  
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_AT_LEAST_TWO_BANK_ORDERS), IException.INCONSISTENCY);
		}

		this.checkSameElements(bankOrderList);
		
		BankOrder bankOrder = bankOrderList.get(0);
		
		bankOrderList.remove(bankOrder);
		
		for(BankOrderLine bankOrderLine : this.getAllBankOrderLineList(bankOrderList))  {
			
			bankOrder.addBankOrderLineListItem(bankOrderLine);
			
		}
			
		for(BankOrder bankOrderToRemove : bankOrderList)  {
			
			List<InvoicePayment> invoicePaymentList = invoicePaymentRepo.findByBankOrder(bankOrderToRemove).fetch();
			
			for(InvoicePayment invoicePayment : invoicePaymentList)  {
				
				invoicePayment.setBankOrder(bankOrder);
				
			}
			
			bankOrderRepo.remove(bankOrderToRemove);
			
		}
		
		bankOrderService.updateTotalAmounts(bankOrder);
		
		return bankOrderRepo.save(bankOrder);
		
	}

	
	protected void checkSameElements(List<BankOrder> bankOrderList) throws AxelorException  {
		
		BankOrder refBankOrder = bankOrderList.get(0);
		
		int refStatusSelect = refBankOrder.getStatusSelect();
		int orderTypeSelect = refBankOrder.getOrderTypeSelect();
		PaymentMode refPaymentMode = refBankOrder.getPaymentMode();
		int refPartnerTypeSelect = refBankOrder.getPartnerTypeSelect();
		Company refSenderCompany = refBankOrder.getSenderCompany();
		BankDetails refSenderBankDetails = refBankOrder.getSenderBankDetails();
		Currency refCurrency = refBankOrder.getBankOrderCurrency();
		boolean isMultiCurrency = refBankOrder.getIsMultiCurrency();
		
		for(BankOrder bankOrder : bankOrderList)  {
			
			int statusSelect = bankOrder.getStatusSelect();
			if(statusSelect != BankOrderRepository.STATUS_DRAFT && statusSelect != BankOrderRepository.STATUS_AWAITING_SIGNATURE)  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_STATUS), IException.INCONSISTENCY);
			}
			
			if(statusSelect != refStatusSelect)  { 
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_STATUS), IException.INCONSISTENCY);
			}
			
			if(!bankOrder.getOrderTypeSelect().equals(orderTypeSelect))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_ORDER_TYPE_SELECT), IException.INCONSISTENCY);
			}
			
			if(!bankOrder.getPaymentMode().equals(refPaymentMode))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_PAYMENT_MODE), IException.INCONSISTENCY);
			}
			
			if(!bankOrder.getPartnerTypeSelect().equals(refPartnerTypeSelect))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_PARTNER_TYPE_SELECT), IException.INCONSISTENCY);
			}
						
			if(!bankOrder.getSenderCompany().equals(refSenderCompany))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_SENDER_COMPANY), IException.INCONSISTENCY);
			}
			
			if(!bankOrder.getSenderBankDetails().equals(refSenderBankDetails))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_SENDER_BANK_DETAILS), IException.INCONSISTENCY);
			}
			
			if(bankOrder.getIsMultiCurrency() != isMultiCurrency || !(bankOrder.getIsMultiCurrency() && !bankOrder.getBankOrderCurrency().equals(refCurrency)))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_SAME_CURRENCY), IException.INCONSISTENCY);
			}
			
		}
		
		
	}
	
	
	protected List<BankOrderLine> getAllBankOrderLineList(List<BankOrder> bankOrderList)  {
		
		List<BankOrderLine> bankOrderLineList = Lists.newArrayList();
		
		for(BankOrder bankOrder : bankOrderList)  {
			
			bankOrderLineList.addAll(bankOrder.getBankOrderLineList());
			
		}
		
		return bankOrderLineList;
		
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public BankOrder mergeFromInvoicePayments(List<InvoicePayment> invoicePaymentList) throws AxelorException {

		if (invoicePaymentList == null || invoicePaymentList.isEmpty()) {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_NO_BANK_ORDERS),
					IException.INCONSISTENCY);
		}

		List<InvoicePayment> invoicePaymentWithBankOrderList = new ArrayList<>();
		List<BankOrder> bankOrderList = new ArrayList<>();

		for (InvoicePayment invoicePayment : invoicePaymentList) {
			BankOrder bankOrder = invoicePayment.getBankOrder();
			if (bankOrder != null) {
				invoicePaymentWithBankOrderList.add(invoicePayment);
				bankOrderList.add(bankOrder);
			}
		}

		if (bankOrderList.size() > 1) {
			BankOrder mergedBankOrder = mergeBankOrderList(bankOrderList);

			for (InvoicePayment invoicePayment : invoicePaymentWithBankOrderList) {
				invoicePayment.setBankOrder(mergedBankOrder);
			}

			return mergedBankOrder;
		}

		if (!bankOrderList.isEmpty()) {
			return bankOrderList.get(0);
		}

		throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_MERGE_NO_BANK_ORDERS),
				IException.INCONSISTENCY);
	}

}



























