package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BankOrderLineService {
	
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BankDetailsRepository bankDetailsRepo;

	
	@Inject
	public BankOrderLineService(BankDetailsRepository bankDetailsRepo)  {
		
		this.bankDetailsRepo = bankDetailsRepo;
		
	}
	
	/**
	 * Method to create a specific BankOrderLine for SEPA and internationnal transfer and direct debit
	 *
	 * @param partner
	 * @param amount
	 * @param receiverReference
	 * @param receiverLabel
	 * @return
	 * @throws AxelorException
	 */
	public BankOrderLine createBankOrderLine(Partner partner, BigDecimal amount, String receiverReference,  String receiverLabel) throws AxelorException{
		
		BankDetails receiverBankDetails = bankDetailsRepo.findDefaultByPartner(partner, true);
		
		return this.createBankOrderLine(null, partner, receiverBankDetails, amount, receiverReference, receiverLabel);
	
	}
	
	/**
	 * Method to create a specific BankOrderLine for treasury transfer
	 *
	 * @param receiverCompany
	 * @param amount
	 * @param receiverReference
	 * @param receiverLabel
	 * @return
	 * @throws AxelorException
	 */
	public BankOrderLine createBankOrderLine(Company receiverCompany, BigDecimal amount, String receiverReference,  String receiverLabel) throws AxelorException{
		
		return this.createBankOrderLine(receiverCompany, receiverCompany.getPartner(), receiverCompany.getDefaultBankDetails(), amount, receiverReference, receiverLabel);
	}
	
	
	/**
	 * Generic method to create a BankOrderLine
	 * @param receiverCompany
	 * @param partner
	 * @param bankDetails
	 * @param amount
	 * @param receiverReference
	 * @param receiverLabel
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
	
	public void checkPreconditions(BankOrderLine bankOrderLine, int orderType)  throws AxelorException{

		if (orderType == BankOrderRepository.BANK_TO_BANK_TRANSFER)  {
			if (bankOrderLine.getReceiverCompany() == null )  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_COMPANY_MISSING), IException.INCONSISTENCY);
			}
		}
		if(bankOrderLine.getPartner() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_PARTNER_MISSING), IException.INCONSISTENCY);
		}
		if (bankOrderLine.getReceiverBankDetails() == null )  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_COMPANY_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrderLine.getAmount().compareTo(BigDecimal.ZERO) <= 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
		}
		
	}
	
	
}
