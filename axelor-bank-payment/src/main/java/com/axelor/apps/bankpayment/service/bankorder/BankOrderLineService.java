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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.inject.Beans;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
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
	 * Method to create a specific BankOrderLine for SEPA and international transfer and direct debit
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
	
	public void checkPreconditions(BankOrderLine bankOrderLine)  throws AxelorException{

		if (bankOrderLine.getBankOrder().getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY)  {
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

	public void checkBankDetails(BankOrderLine bankOrderLine) throws AxelorException {
	    BankDetails bankDetails = bankOrderLine.getBankOrder().getSenderBankDetails();
	    if (bankDetails == null) {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING), IException.INCONSISTENCY);
		}
		EbicsPartner partner = Beans.get(EbicsPartnerRepository.class).all()
				.filter("? MEMBER OF self.bankDetailsSet", bankDetails)
				.fetchOne();
	    if (partner.getFilterReceiverBD() &&
			(partner.getOrderTypeSelect() == bankOrderLine.getBankOrder().getOrderTypeSelect())
			) {
			if (!partner.getReceiverBankDetailsSet().contains(bankOrderLine.getReceiverBankDetails())) {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_FORBIDDEN), IException.INCONSISTENCY);
			}
		}
	}

	public String createDomainForBankDetails(BankOrderLine bankOrderLine, BankOrder bankOrder) {
		String domain = "";
		String bankDetailsIds = "";

		if ((bankOrderLine == null) || (bankOrder == null)) {
			return domain;
		}

		//the case where the bank order is for a company
		if (bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
			if (bankOrderLine.getReceiverCompany() != null) {

			    bankDetailsIds = this.getIdStringListFromList(bankOrderLine.getReceiverCompany().getBankDetailsSet());

				if(bankOrderLine.getReceiverCompany().getDefaultBankDetails() != null) {
					bankDetailsIds += bankDetailsIds.equals("") ? "" : ",";
					bankDetailsIds += bankOrderLine.getReceiverCompany()
							.getDefaultBankDetails().getId().toString();
				}

			}
		}

		//case where the bank order is for a partner
		else if (bankOrderLine.getPartner() != null) {
		    bankDetailsIds = this.getIdStringListFromList(bankOrderLine.getPartner().getBankDetailsList());
		}

		if (bankDetailsIds.equals("")) {
			return domain = "";
		}

		domain = "self.id IN(" + bankDetailsIds + ")";

		//filter on the result from bankPartner if the option is active.
		EbicsPartner ebicsPartner = Beans.get(EbicsPartnerRepository.class).all()
				.filter("? MEMBER OF self.bankDetailsSet",
						bankOrder.getSenderBankDetails())
				.fetchOne();

		if (ebicsPartnerIsFiltering(ebicsPartner, bankOrder.getOrderTypeSelect())) {
		    domain += " AND self.id IN (" +
					this.getIdStringListFromList(ebicsPartner.getReceiverBankDetailsSet()) +
					")";
		}
		return domain;
	}

	public BankDetails getDefaultBankDetails(BankOrderLine bankOrderLine, BankOrder bankOrder) {
		BankDetails candidateBankDetails = null;
		if (bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
			//fill using the default in company
			if (bankOrderLine.getReceiverCompany() == null) {return null;}
			candidateBankDetails = bankOrderLine.getReceiverCompany().getDefaultBankDetails();
		}
		else {
			//fill using the default in partner
			if (bankOrderLine.getPartner() == null) {return null;}
			for (BankDetails bankDetails : bankOrderLine.getPartner().getBankDetailsList()) {
				if (bankDetails.getIsDefault()) {
					candidateBankDetails = bankDetails;
					break;
				}
			}
		}

		//filter on the result from bankPartner if the option is active.
		EbicsPartner ebicsPartner = Beans.get(EbicsPartnerRepository.class).all()
				.filter("? MEMBER OF self.bankDetailsSet",
						bankOrder.getSenderBankDetails())
				.fetchOne();

		if (ebicsPartnerIsFiltering(ebicsPartner, bankOrder.getOrderTypeSelect())) {

			if (ebicsPartner.getReceiverBankDetailsSet().contains(candidateBankDetails)) {
				return candidateBankDetails;
			}
			else {
				return null;
			}
		}
		return candidateBankDetails;
	}

	private String getIdStringListFromList(Collection<BankDetails> bankDetailsList) {
		String idList = "";
		for (BankDetails bankDetails : bankDetailsList) {
			idList += bankDetails.getId() + ",";
		}
		//remove the last comma
		if(!idList.equals("") && idList.substring(idList.length() - 1).equals(",")) {
			idList = idList.substring(0, idList.length() - 1);
		}
		return idList;
	}

	private boolean ebicsPartnerIsFiltering(EbicsPartner ebicsPartner, int orderType) {
		return  (ebicsPartner != null) &&
				(ebicsPartner.getFilterReceiverBD()) &&
				(ebicsPartner.getReceiverBankDetailsSet() != null) &&
				(!ebicsPartner.getReceiverBankDetailsSet().isEmpty()) &&
				(ebicsPartner.getOrderTypeSelect() == orderType);
	}
	
}
