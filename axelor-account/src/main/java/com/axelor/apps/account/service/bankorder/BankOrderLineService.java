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
package com.axelor.apps.account.service.bankorder;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrderFileFormat;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
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
	public BankOrderLine createBankOrderLine(BankOrderFileFormat bankOrderFileFormat, Partner partner, BigDecimal amount, Currency currency, 
			LocalDate bankOrderDate, String receiverReference,  String receiverLabel) throws AxelorException{
		
		BankDetails receiverBankDetails = bankDetailsRepo.findDefaultByPartner(partner, true);
		
		return this.createBankOrderLine(bankOrderFileFormat, null, partner, receiverBankDetails, amount, currency, bankOrderDate, receiverReference, receiverLabel);
	
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
	public BankOrderLine createBankOrderLine(BankOrderFileFormat bankOrderFileFormat, Company receiverCompany, BigDecimal amount, Currency currency, 
			LocalDate bankOrderDate, String receiverReference,  String receiverLabel) throws AxelorException{
		
		return this.createBankOrderLine(bankOrderFileFormat, receiverCompany, receiverCompany.getPartner(), receiverCompany.getDefaultBankDetails(), 
				amount, currency, bankOrderDate, receiverReference, receiverLabel);
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
	public BankOrderLine createBankOrderLine(BankOrderFileFormat bankOrderFileFormat, Company receiverCompany, Partner partner, BankDetails bankDetails, 
			BigDecimal amount, Currency currency, LocalDate bankOrderDate, String receiverReference,  String receiverLabel) throws AxelorException{
		
		BankOrderLine bankOrderLine = new BankOrderLine();
		
		bankOrderLine.setReceiverCompany(receiverCompany);
		bankOrderLine.setPartner(partner);
		bankOrderLine.setReceiverBankDetails(bankDetails);
		bankOrderLine.setBankOrderAmount(amount);
		
		if(bankOrderFileFormat.getIsMultiCurrency())  {
			bankOrderLine.setBankOrderCurrency(currency);
		}
		
		if(bankOrderFileFormat.getIsMultiDate())  {
			bankOrderLine.setBankOrderDate(bankOrderDate);
		}
		
		bankOrderLine.setReceiverReference(receiverReference);
		bankOrderLine.setReceiverLabel(receiverLabel);
		
		bankOrderLine.setBankOrderEconomicReason(bankOrderFileFormat.getBankOrderEconomicReason());
		bankOrderLine.setReceiverCountry(bankOrderFileFormat.getReceiverCountry());
		bankOrderLine.setPaymentModeSelect(bankOrderFileFormat.getPaymentModeSelect());
		bankOrderLine.setFeesImputationModeSelect(bankOrderFileFormat.getFeesImputationModeSelect());
		
		return bankOrderLine;
	}
	
	public void checkPreconditions(BankOrderLine bankOrderLine, int orderType)  throws AxelorException{

		if (orderType == BankOrderRepository.ORDER_TYPE_BANK_TO_BANK_TRANSFER)  {
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
		if(bankOrderLine.getBankOrderAmount().compareTo(BigDecimal.ZERO) <= 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
		}
		
	}
	
	
}
