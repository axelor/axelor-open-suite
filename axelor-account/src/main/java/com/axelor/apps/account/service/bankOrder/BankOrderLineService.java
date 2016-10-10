package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderLineService {
	
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	
	@Inject
	public BankOrderLineService(){
		
	}
	
	/**
	 * Créer une ligne d'ordre bancaire pour un virement/prélèvement SEPA et international
	 *
	 * @param 
	 * @return
	 * @throws AxelorException
	 */
	public BankOrderLine createBankOrderLine(Partner partner, BankDetails bankDetails, BigDecimal amount, String receiverReference,  String receiverLabel) throws AxelorException{
		
		BankOrderLine bankOrderLine = new BankOrderLine();
		
		bankOrderLine.setPartner(partner);
		bankOrderLine.setReceiverBankDetails(bankDetails);
		bankOrderLine.setAmount(amount);
		bankOrderLine.setReceiverReference(receiverReference);
		bankOrderLine.setReceiverLabel(receiverLabel);
		
		return bankOrderLine;
	}
	
	/**
	 * Créer une ligne d'ordre bancaire pour un virement banque à banque
	 *
	 * @param 
	 * @return
	 * @throws AxelorException
	 */
	public BankOrderLine createBankOrderLine(Company receiverCompany, Partner partner, BankDetails bankDetails, BigDecimal amount, String receiverReference,  String receiverLabel) throws AxelorException{
		
		BankOrderLine bankOrderLine = new BankOrderLine();
		
		
		bankOrderLine.setReceiverCompany(receiverCompany);
		bankOrderLine.setPartner(partner);
		bankOrderLine.setReceiverBankDetails(bankDetails);
		bankOrderLine.setAmount(amount);
		bankOrderLine.setReceiverReference(receiverReference);
		bankOrderLine.setReceiverLabel(receiverLabel);
		
		return bankOrderLine;
	}
	
	
}
