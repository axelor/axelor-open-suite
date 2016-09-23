package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
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
	public BankOrder createBankOrder(String reference, Integer orderType, Integer partnerType, LocalDate bankOrderDate, Integer statusSelect,
			Integer rejectStatus, Company senderCompany, BankDetails senderBankDetails, BigDecimal amount, Currency currency,
									String senderReference,  String senderLabel, Move senderMove) throws AxelorException{
		
		BankOrder bankOrder = new BankOrder();
		
		bankOrder.setReference(reference);
		bankOrder.setOrderType(orderType);
		bankOrder.setPartnerType(partnerType);
		bankOrder.setBankOrderDate(bankOrderDate);
		// To check : bankOrderRepository static values
		bankOrder.setStatusSelect(1);
		bankOrder.setRejectStatus(0);
		bankOrder.setSenderCompany(senderCompany);
		bankOrder.setSenderBankDetails(senderBankDetails);
		bankOrder.setAmount(amount);
		bankOrder.setCurrency(currency);
		bankOrder.setSenderLabel(senderLabel);
		bankOrder.setSenderMove(senderMove);
		bankOrder.setBankOrderLineList(new ArrayList<BankOrderLine>());
		bankOrderRepo.save(bankOrder);
		bankOrder.setSenderReference("*"+bankOrder.getId());
		
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
