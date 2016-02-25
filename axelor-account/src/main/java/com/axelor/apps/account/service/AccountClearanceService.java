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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountClearanceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountClearanceService{

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveService moveService;
	protected MoveLineService moveLineService;
	protected MoveLineRepository moveLineRepo;
	protected SequenceService sequenceService;
	protected ReconcileService reconcileService;
	protected TaxService taxService;
	protected TaxAccountService taxAccountService;
	protected AccountClearanceRepository accountClearanceRepository;
	protected DateTime todayTime;
	protected User user;

	@Inject
	public AccountClearanceService(UserService userService, GeneralService generalService) {

		this.todayTime = generalService.getTodayDateTime();
		this.user = userService.getUser();
		
	}


	public List<? extends MoveLine> getExcessPayment(AccountClearance accountClearance) throws AxelorException  {

		Company company = accountClearance.getCompany();

		this.testCompanyField(company);

		List<? extends MoveLine> moveLineList = moveLineRepo.all().filter("self.company = ?1 AND self.account.reconcileOk = 'true' AND self.fromSchedulePaymentOk = 'false' " +
				"AND self.move.statusSelect = ?2 AND self.amountRemaining > 0 AND self.amountRemaining <= ?3 AND self.credit > 0 AND self.account in ?4 AND self.date <= ?5",
				company, MoveRepository.STATUS_VALIDATED , accountClearance.getAmountThreshold(),
				company.getAccountConfig().getClearanceAccountSet(), accountClearance.getDateThreshold()).fetch();

		log.debug("Liste des trop perçus récupérés : {}", moveLineList);

		return moveLineList;
	}


	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setExcessPayment(AccountClearance accountClearance) throws AxelorException  {
		accountClearance.setMoveLineSet(new HashSet<MoveLine>());
		List<MoveLine> moveLineList = (List<MoveLine>) this.getExcessPayment(accountClearance);
		if(moveLineList != null && moveLineList.size() != 0)  {
			accountClearance.getMoveLineSet().addAll(moveLineList);
		}
		accountClearanceRepository.save(accountClearance);
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateAccountClearance(AccountClearance accountClearance) throws AxelorException  {
		Company company = accountClearance.getCompany();
		AccountConfig accountConfig = company.getAccountConfig();

		Tax tax = accountConfig.getStandardRateTax();

		BigDecimal taxRate = taxService.getTaxRate(tax, todayTime.toLocalDate());
		Account taxAccount = taxAccountService.getAccount(tax, company);
		Account profitAccount = accountConfig.getProfitAccount();
		Journal journal = accountConfig.getAccountClearanceJournal();

		Set<MoveLine> moveLineList = accountClearance.getMoveLineSet();

		for(MoveLine moveLine : moveLineList)  {
			Move move = this.createAccountClearanceMove(moveLine, taxRate, taxAccount, profitAccount, company, journal, accountClearance);
			moveService.getMoveValidateService().validateMove(move);
		}

		accountClearance.setStatusSelect(AccountClearanceRepository.STATUS_VALIDATED);
		accountClearance.setDateTime(this.todayTime);
		accountClearance.setName(sequenceService.getSequenceNumber(IAdministration.ACCOUNT_CLEARANCE, company));
		accountClearanceRepository.save(accountClearance);
	}


	public Move createAccountClearanceMove(MoveLine moveLine, BigDecimal taxRate, Account taxAccount, Account profitAccount, Company company, Journal journal, AccountClearance accountClearance) throws AxelorException  {
		Partner partner = moveLine.getPartner();

		// Move
		Move move = moveService.getMoveCreateService().createMove(journal, company, null, partner, null);

		// Debit MoveLine 411
		BigDecimal amount = moveLine.getAmountRemaining();
		MoveLine debitMoveLine = moveLineService.createMoveLine(move, partner, moveLine.getAccount(), amount, true, todayTime.toLocalDate(), 1, null);
		move.getMoveLineList().add(debitMoveLine);

		// Credit MoveLine 77. (profit account)
		BigDecimal divid = taxRate.add(BigDecimal.ONE);
		BigDecimal profitAmount = amount.divide(divid, IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);
		MoveLine creditMoveLine1 = moveLineService.createMoveLine(move, partner, profitAccount, profitAmount, false, todayTime.toLocalDate(), 2, null);
		move.getMoveLineList().add(creditMoveLine1);

		// Credit MoveLine 445 (Tax account)
		BigDecimal taxAmount = amount.subtract(profitAmount);
		MoveLine creditMoveLine2 = moveLineService.createMoveLine(move, partner, taxAccount, taxAmount, false, todayTime.toLocalDate(), 3, null);
		move.getMoveLineList().add(creditMoveLine2);

		Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, moveLine, amount, false);
		reconcileService.confirmReconcile(reconcile);

		debitMoveLine.setAccountClearance(accountClearance);
		creditMoveLine1.setAccountClearance(accountClearance);
		creditMoveLine2.setAccountClearance(accountClearance);
		return move;
	}


	public AccountClearance createAccountClearance(Company company, String name,BigDecimal amountThreshold, LocalDate dateThreshold, List<MoveLine> moveLineSet)  {
		AccountClearance accountClearance = new AccountClearance();
		accountClearance.setAmountThreshold(amountThreshold);
		accountClearance.setCompany(company);
		accountClearance.setDateThreshold(dateThreshold);
		accountClearance.getMoveLineSet().addAll(moveLineSet);
		accountClearance.setName(name);
		accountClearance.setDateTime(this.todayTime);
		accountClearance.setUser(this.user);
		accountClearance.setStatusSelect(AccountClearanceRepository.STATUS_VALIDATED);
		return accountClearance;

	}


	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {

		AccountConfig accountConfig = company.getAccountConfig();

		if(accountConfig == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_1),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		if(accountConfig.getProfitAccount() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_2),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		if(accountConfig.getStandardRateTax() == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_3),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		if(accountConfig.getClearanceAccountSet() == null || accountConfig.getClearanceAccountSet().size() == 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_4),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		if(!sequenceService.hasSequence(IAdministration.ACCOUNT_CLEARANCE, company)) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_5),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

		if(accountConfig.getAccountClearanceJournal() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_6),
					GeneralServiceImpl.EXCEPTION,company.getName()), IException.CONFIGURATION_ERROR);
		}

	}

}
