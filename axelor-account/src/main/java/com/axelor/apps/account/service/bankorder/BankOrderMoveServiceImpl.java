/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderMoveServiceImpl implements BankOrderMoveService  {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BankOrderRepository bankOrderRepo;
	protected MoveService moveService;
	protected MoveLineService moveLineService;
	protected PaymentModeService paymentModeService;
	protected AccountingSituationService accountingSituationService;
	
	protected PaymentMode paymentMode;
	protected Company senderCompany;
	protected int orderTypeSelect;
	protected int partnerTypeSelect;
	protected Journal journal;
	protected LocalDate bankOrderDate;
	protected Currency bankOrderCurrency;
	protected Account senderBankAccount;
	protected boolean isMultiDate;
	protected boolean isMultiCurrency;
	protected boolean isDebit;
	
	@Inject
	public BankOrderMoveServiceImpl(BankOrderRepository bankOrderRepo, MoveService moveService, 
			MoveLineService moveLineService, PaymentModeService paymentModeService, AccountingSituationService accountingSituationService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.moveService = moveService;
		this.moveLineService = moveLineService;
		this.paymentModeService = paymentModeService;
		this.accountingSituationService = accountingSituationService;
		
	}
	
	
	
	public void generateMoves(BankOrder bankOrder) throws AxelorException  {
		
		paymentMode = bankOrder.getPaymentMode();
		
		if(!paymentMode.getGenerateMoveAutoFromBankOrder())  {  return;  }
		
		orderTypeSelect = bankOrder.getOrderTypeSelect();
		senderCompany = bankOrder.getSenderCompany();
		journal = paymentModeService.getPaymentModeJournal(paymentMode, senderCompany);
		senderBankAccount = paymentModeService.getPaymentModeAccount(paymentMode, senderCompany);
		
		isMultiDate = bankOrder.getIsMultiDate();
		isMultiCurrency = bankOrder.getIsMultiCurrency();
		
		if(orderTypeSelect == BankOrderRepository.ORDER_TYPE_INTERNATIONAL_CREDIT_TRANSFER 
				|| orderTypeSelect == BankOrderRepository.ORDER_TYPE_SEPA_CREDIT_TRANSFER
				|| orderTypeSelect == BankOrderRepository.ORDER_TYPE_BANK_TO_BANK_TRANSFER)  {
			isDebit = true;
		}
		else  {
			isDebit = false;
		}
		 
		
		for(BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList())  {
			
			generateMoves(bankOrderLine);
			
		}
		
	}
	
	protected void generateMoves(BankOrderLine bankOrderLine) throws AxelorException  {
		
		bankOrderLine.setSenderMove(
				generateSenderMove(bankOrderLine));
		
		if(orderTypeSelect == BankOrderRepository.ORDER_TYPE_BANK_TO_BANK_TRANSFER)  {
			bankOrderLine.setReceiverMove(
					generateReceiverMove(bankOrderLine));
		}
		
	}
	
	
	protected Move generateSenderMove(BankOrderLine bankOrderLine) throws AxelorException  {
		
		Partner partner = bankOrderLine.getPartner();
		
		Move senderMove = moveService.getMoveCreateService()
				.createMove(journal, senderCompany, 
						this.getCurrency(bankOrderLine), partner, 
						this.getDate(bankOrderLine), paymentMode, MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
		
		MoveLine bankMoveLine = moveLineService.createMoveLine(
				senderMove, partner, senderBankAccount,
				bankOrderLine.getBankOrderAmount(), !isDebit,
				senderMove.getDate(), 1, bankOrderLine.getReceiverReference());
		senderMove.addMoveLineListItem(bankMoveLine);
		
		
		//TODO manage the case with bank to bank account
		MoveLine partnerMoveLine = moveLineService.createMoveLine(
				senderMove, partner, getPartnerAccount(partner),
				bankOrderLine.getBankOrderAmount(), isDebit,
				senderMove.getDate(), 2, bankOrderLine.getReceiverReference());
		senderMove.addMoveLineListItem(partnerMoveLine);

		return senderMove;
		
	}
	
	protected Move generateReceiverMove(BankOrderLine bankOrderLine) throws AxelorException  {
		
		Partner partner = bankOrderLine.getPartner();
		
		Move receiverMove = moveService.getMoveCreateService()
				.createMove(journal, senderCompany, 
						this.getCurrency(bankOrderLine), partner, 
						this.getDate(bankOrderLine), paymentMode, MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
		
		MoveLine bankMoveLine = moveLineService.createMoveLine(
				receiverMove, partner, senderBankAccount,
				bankOrderLine.getBankOrderAmount(), isDebit,
				receiverMove.getDate(), 1, bankOrderLine.getReceiverReference());
		receiverMove.addMoveLineListItem(bankMoveLine);
		
		//TODO manage the case with bank to bank account
		MoveLine partnerMoveLine = moveLineService.createMoveLine(
				receiverMove, partner, getPartnerAccount(partner),
				bankOrderLine.getBankOrderAmount(), !isDebit,
				receiverMove.getDate(), 2, bankOrderLine.getReceiverReference());
		receiverMove.addMoveLineListItem(partnerMoveLine);

		return receiverMove;
		
	}
	
	
	protected Account getPartnerAccount(Partner partner)  {
		
		AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, senderCompany);

		switch (partnerTypeSelect) {
		case BankOrderRepository.PARTNER_TYPE_CUSTOMER :
			return accountingSituation.getCustomerAccount();
			
		case BankOrderRepository.PARTNER_TYPE_EMPLOYEE :
			return accountingSituation.getEmployeeAccount();
			
		case BankOrderRepository.PARTNER_TYPE_SUPPLIER :
			return accountingSituation.getSupplierAccount();

		default:
			//throw new AxelorException(cause, category); TODO anomaly
			return null;
		}
		
	}
	
	protected LocalDate getDate(BankOrderLine bankOrderLine)  {
		
		if(isMultiDate)  {
			return bankOrderDate;
		}
		else  {
			return bankOrderLine.getBankOrderDate();
		}
		
	}
	
	protected Currency getCurrency(BankOrderLine bankOrderLine)   {
		
		if(isMultiCurrency)  {
			return bankOrderCurrency;
		}
		else  {
			return bankOrderLine.getBankOrderCurrency();
		}
		
	}
	
}



























