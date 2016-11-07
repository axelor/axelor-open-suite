package com.axelor.apps.account.service.bankorder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
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
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
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
		
		bankOrder.setAmount(bankOrderService.computeTotalAmount(bankOrder));
		
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
		Currency refCurrency = refBankOrder.getCurrency();
		
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
			
			if(!bankOrder.getCurrency().equals(refCurrency))  {
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
	
	
	
	
	
	
	
	
	
	
	
}



























