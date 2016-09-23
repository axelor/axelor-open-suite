package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BankOrderServiceImpl implements BankOrderService{
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BankOrderRepository bankOrderRepo;
	
	@Inject
	public BankOrderServiceImpl(BankOrderRepository bankOrderRepo){
		
		this.bankOrderRepo = bankOrderRepo;
	}
	
	@Override
	public void validate(BankOrder bankOrder) throws AxelorException{
		LocalDate brankOrderDate = bankOrder.getBankOrderDate();
		Integer orderType = bankOrder.getOrderType();
		Integer partnerType = bankOrder.getPartnerType();
		BigDecimal amount = bankOrder.getAmount();
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		
		if (brankOrderDate != null){
			if(brankOrderDate.isBefore(LocalDate.now())){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE), IException.INCONSISTENCY);
			}
		}else{
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE_MISSING), IException.INCONSISTENCY);
		}
		
		if(orderType == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_TYPE_MISSING), IException.INCONSISTENCY);
		}
		else{
			//TODO check why static values not generated : replace 1 by bank_to_bank type
			if(orderType !=  1  && partnerType == null){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING), IException.INCONSISTENCY);
			}
		}
		if(bankOrder.getSenderCompany() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_COOMPANY_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSenderBankDetails() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getCurrency() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_CURRENCY_MISSING), IException.INCONSISTENCY);
		}
		if(amount == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_AMOUNT_MISSING), IException.INCONSISTENCY);
		}else{
			if(amount.compareTo(BigDecimal.ZERO) <= 0){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
			}
		}
		if((!bankOrderLines.isEmpty() || bankOrderLines != null) && orderType != null){
//			validateBankOrderLines(bankOrderLines,orderType);
		}
	}
	
//	public void validateBankOrderLines(List<BankOrderLine> bankOrderLines, int orderType)throws AxelorException{
//		for (BankOrderLine bankOrderLine : bankOrderLines) {
//			if (orderType == 1){
//				if (bankOrderLine.getReceiverCompany() == null){
//					throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_COOMPANY_MISSING), IException.INCONSISTENCY);
//			}
//		}
//	}

}
