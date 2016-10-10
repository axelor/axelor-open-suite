package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderCreateService {

	private final Logger log = LoggerFactory.getLogger( getClass() );
	protected BankOrderRepository bankOrderRepo;
	
	
	@Inject
	public BankOrderCreateService(BankOrderRepository bankOrderRepo){
		
		this.bankOrderRepo = bankOrderRepo;
	}
	
	/**
	 * Créer un ordre bancaire avec tous les paramètres
	 *
	 * @param 
	 * @return
	 * @throws AxelorException
	 */
	public BankOrder createBankOrder(Integer orderType, PaymentMode paymentMode, Integer partnerType, LocalDate bankOrderDate, Integer statusSelect,
			Integer rejectStatus, Company senderCompany, BankDetails senderBankDetails, BigDecimal amount, User signatory ,Currency currency,
									String senderReference,  String senderLabel) throws AxelorException{
		
		BankOrder bankOrder = new BankOrder();
		
		bankOrder.setOrderType(orderType);
		bankOrder.setPaymentMode(paymentMode);
		bankOrder.setPartnerTypeSelect(partnerType);
		bankOrder.setBankOrderDate(bankOrderDate);
		bankOrder.setStatusSelect(statusSelect);
		bankOrder.setRejectStatusSelect(rejectStatus);
		bankOrder.setSenderCompany(senderCompany);
		bankOrder.setSenderBankDetails(senderBankDetails);
		bankOrder.setAmount(amount);
		bankOrder.setSignatoryUser(signatory);
		bankOrder.setCurrency(currency);
		bankOrder.setSenderLabel(senderLabel);
		bankOrder.setBankOrderLineList(new ArrayList<BankOrderLine>());
		bankOrderRepo.save(bankOrder);
		
		return bankOrder;
	}
	
	
//	/**
//	 * Créer un ordre bancaire à partir d'un ordre de paiement
//	 *
//	 * @param 
//	 * @return
//	 * @throws AxelorException
//	 */
//	public BankOrder createBankOrder(String reference, Integer orderType, Integer partnerType, LocalDate bankOrderDate, Integer statusSelect,
//			Integer rejectStatus, Company senderCompany, BankDetails senderBankDetails, BigDecimal amount, Currency currency,
//									String senderReference,  String senderLabel, Move senderMove) throws AxelorException{
//		
//		return this();
//	}
	
	
}
