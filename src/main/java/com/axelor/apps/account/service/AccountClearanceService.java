/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountClearance;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountClearanceService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountClearanceService.class);
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private ReconcileService rs;
	
	@Inject
	private VatService vs;
	
	@Inject
	private VatAccountService vas;
	
	@Inject
	private AccountManagementService ams;
	
	private DateTime todayTime;
	private UserInfo user;

	@Inject
	public AccountClearanceService(UserInfoService uis) {

		this.todayTime = GeneralService.getTodayDateTime();
		this.user = uis.getUserInfo();
	}
	
	
	public List<MoveLine> getExcessPayment(AccountClearance accountClearance) throws AxelorException  {		
		
		Company company = accountClearance.getCompany();
		
		this.testCompanyField(company);
		
		String contractLineStatusRequest = this.getContractLineStatusRequest(accountClearance);
		
		List<MoveLine> moveLineList = MoveLine.all().filter("self.company = ?1 AND self.account.reconcileOk = 'true' AND self.fromSchedulePaymentOk = 'false' " +
				"AND self.move.state = ?2 AND self.amountRemaining > 0 AND self.amountRemaining <= ?3 AND self.credit > 0 AND self.account in ?4 AND self.date <= ?5"
				+contractLineStatusRequest,
				company, IAccount.VALIDATED_MOVE , accountClearance.getAmountThreshold(), 
				company.getClearanceAccountSet(), accountClearance.getDateThreshold()).fetch();
		
		LOG.debug("Liste des trop perçus récupérés : {}", moveLineList);
		
		return moveLineList;
	}
	
	
	public String getContractLineStatusRequest(AccountClearance accountClearance)  {
//		switch (accountClearance.getContractLineTypeSelect()) {
		
//			// Contrat résilié						
//			case IAccount.CANCELED_CONTRACTLINE:
//				return " AND self.contractLine.status.code = 'res'";
//							
//			// Contrat non résilié
//			case IAccount.NOT_CANCELED_CONTRACTLINE:			
//				return " AND self.contractLine.status.code != 'res'";
							
			// Tous les contrats
//			case IAccount.ALL_CONTRACTLINE:			
				return "";
				
//			default:
//				break;
//			}
//		
//		return null;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setExcessPayment(AccountClearance accountClearance) throws AxelorException  {
		accountClearance.setMoveLineSet(new HashSet<MoveLine>());
		List<MoveLine> moveLineList = this.getExcessPayment(accountClearance);
		if(moveLineList != null && moveLineList.size() != 0)  {
			accountClearance.getMoveLineSet().addAll(moveLineList);
		}
		accountClearance.save();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateAccountClearance(AccountClearance accountClearance) throws AxelorException  {
		Company company = accountClearance.getCompany();
		Vat vat = company.getStandardRateVat();
		
		BigDecimal vatRate = vs.getVatRate(vat, todayTime.toLocalDate());
		Account vatAccount = vas.getAccount(vat, company);
		Account profitAccount = company.getProfitAccount();
		Journal journal = company.getAccountClearanceJournal();
		
		Set<MoveLine> moveLineList = accountClearance.getMoveLineSet();
		
		for(MoveLine moveLine : moveLineList)  {
			Move move = this.createAccountClearanceMove(moveLine, vatRate, vatAccount, profitAccount, company, journal, accountClearance);
			ms.validateMove(move);
		}
		
		accountClearance.setStatus(Status.all().filter("self.code ='val'").fetchOne());
		accountClearance.setDateTime(this.todayTime);
		accountClearance.setName(sgs.getSequence(IAdministration.ACCOUNT_CLEARANCE, company, false));
		accountClearance.save();
	}
	
	
	public Move createAccountClearanceMove(MoveLine moveLine, BigDecimal vatRate, Account vatAccount, Account profitAccount, Company company, Journal journal, AccountClearance accountClearance) throws AxelorException  {
		Partner partner = moveLine.getPartner();
		
		// Move
		Move move = ms.createMove(journal, company, null, partner, null, false);

		// Debit MoveLine 411
		BigDecimal amount = moveLine.getAmountRemaining();
		MoveLine debitMoveLine = mls.createMoveLine(move, partner, moveLine.getAccount(), amount, true, false, todayTime.toLocalDate(),
				1, false, false, false, null);
		move.getMoveLineList().add(debitMoveLine);
		
		
		// Credit MoveLine 77. (profit account)
		BigDecimal divid = vatRate.add(BigDecimal.ONE);
		BigDecimal profitAmount = amount.divide(divid, 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
		MoveLine creditMoveLine1 = mls.createMoveLine(move, partner, profitAccount, profitAmount, false, false, todayTime.toLocalDate(),
				2, false, false, false, null);
		move.getMoveLineList().add(creditMoveLine1);

		// Credit MoveLine 445 (VAT account)
		BigDecimal vatAmount = amount.subtract(profitAmount);
		MoveLine creditMoveLine2 = mls.createMoveLine(move, partner, vatAccount, vatAmount, false, false, todayTime.toLocalDate(),
				3, false, false, false, null);
		move.getMoveLineList().add(creditMoveLine2);
		
		
		Reconcile reconcile = rs.createReconcile(debitMoveLine, moveLine, amount);
		rs.confirmReconcile(reconcile);
		
		debitMoveLine.setAccountClearance(accountClearance);
		creditMoveLine1.setAccountClearance(accountClearance);
		creditMoveLine2.setAccountClearance(accountClearance);
		return move;
	}
	
	
	
	
	public AccountClearance createAccountClearance(Company company, String name,BigDecimal amountThreshold, LocalDate dateThreshold, int contractLineTypeSelect, List<MoveLine> moveLineSet)  {
		AccountClearance accountClearance = new AccountClearance();
		accountClearance.setAmountThreshold(amountThreshold);
		accountClearance.setCompany(company);
		accountClearance.setDateThreshold(dateThreshold);
		accountClearance.getMoveLineSet().addAll(moveLineSet);
		accountClearance.setName(name);
		accountClearance.setDateTime(this.todayTime);
		accountClearance.setUserInfo(this.user);
		accountClearance.setStatus(Status.all().filter("self.code = 'val'").fetchOne());
		return accountClearance;
		
	}
	
	
	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		if(company.getProfitAccount() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte de profit pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		if(company.getStandardRateVat() == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une TVA taux normal pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		if(company.getClearanceAccountSet() == null || company.getClearanceAccountSet().size() == 0)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer les comptes d'apurements pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		String seq = sgs.getSequence(IAdministration.ACCOUNT_CLEARANCE, company, true);
		if(seq == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Apurement des trop-perçus pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		if(company.getAccountClearanceJournal() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal d'apurements des trop-perçus pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
	}
	
}
