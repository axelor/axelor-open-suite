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
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountCustomerService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected AccountingSituationService  accountingSituationService;
	protected AccountingSituationRepository accSituationRepo;
	protected LocalDate today;
	
	@Inject
	public AccountCustomerService(AccountingSituationService  accountingSituationService, AccountingSituationRepository accSituationRepo, GeneralService generalService) {

		this.accountingSituationService =accountingSituationService;
		this.accSituationRepo = accSituationRepo;
		this.today = generalService.getTodayDate();
	}

	public AccountingSituationService getAccountingSituationService()  {
		return this.accountingSituationService;
	}


	/**
	 * Fonction permettant de calculer le solde total d'un tiers
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @return
	 * 			Le solde total
	 */
	public BigDecimal getBalance (Partner partner, Company company)  {
		log.debug("Compute balance (Partner : {}, Company : {})",partner.getName(),company.getName());

		Query query = JPA.em().createNativeQuery("SELECT SUM(COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "+
												"FROM public.account_move_line AS ml  "+
												"LEFT OUTER JOIN ( "+
													"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
													"FROM public.account_move_line AS moveline "+
													"WHERE moveline.debit > 0  GROUP BY moveline.id, moveline.amount_remaining) AS m1 ON (m1.moveline_id = ml.id) "+
												"LEFT OUTER JOIN ( "+
													"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
													"FROM public.account_move_line AS moveline "+
													"WHERE moveline.credit > 0  GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "+
												"LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "+
												"LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "+
												"WHERE ml.partner = ?1 AND move.company = ?2 AND move.ignore_in_accounting_ok IN ('false', null) AND account.reconcile_ok = 'true' "+
												"AND move.status_select = ?3 AND ml.amount_remaining > 0 ")
												.setParameter(1, partner)
												.setParameter(2, company)
												.setParameter(3, MoveRepository.STATUS_VALIDATED);

		BigDecimal balance = (BigDecimal)query.getSingleResult();

		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}

		log.debug("Balance : {}", balance);

		return balance;
	}


	/**
	 * Fonction permettant de calculer le solde exigible d'un tiers
	 *
	 * Calcul du solde exigible du tiers :
	 * Montant Total des factures et des échéances rejetées échues (date du jour >= date de l’échéance)
	 *
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 *
	 * @return
	 * 			Le solde exigible
	 */
	public BigDecimal getBalanceDue (Partner partner, Company company)  {
		log.debug("Compute balance due (Partner : {}, Company : {})",partner.getName(),company.getName());

		Query query = JPA.em().createNativeQuery("SELECT SUM( COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "+
				"FROM public.account_move_line AS ml  "+
				"LEFT OUTER JOIN ( "+
					"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
					"FROM public.account_move_line AS moveline "+
					"WHERE moveline.debit > 0 " +
					"AND ((moveline.due_date IS NULL AND moveline.date_val <= ?1) OR (moveline.due_date IS NOT NULL AND moveline.due_date <= ?1)) " +
					"GROUP BY moveline.id, moveline.amount_remaining) AS m1 on (m1.moveline_id = ml.id) "+
				"LEFT OUTER JOIN ( "+
					"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
					"FROM public.account_move_line AS moveline "+
					"WHERE moveline.credit > 0 " +
					"GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "+
				"LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "+
				"LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "+
				"WHERE ml.partner = ?2 AND move.company = ?3 AND move.ignore_in_reminder_ok IN ('false', null) " +
				"AND move.ignore_in_accounting_ok IN ('false', null) AND account.reconcile_ok = 'true' "+
				"AND move.status_select = ?4 AND ml.amount_remaining > 0 ")
				.setParameter(1, today.toDate(), TemporalType.DATE)
				.setParameter(2, partner)
				.setParameter(3, company)
				.setParameter(4, MoveRepository.STATUS_VALIDATED);

		BigDecimal balance = (BigDecimal)query.getSingleResult();

		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}

		log.debug("Balance due : {}", balance);

		return balance;
	}



	/******************************************  2. Calcul du solde exigible (relançable) du tiers  ******************************************/
	/** solde des factures exigibles non bloquées en relance et dont « la date de facture » + « délai d’acheminement(X) » <« date du jour »
	 *  si la date de facture = date d'échéance de facture, sinon pas de prise en compte du délai d'acheminement ***/
	/** solde des échéances rejetées qui ne sont pas bloqués ******************************************************/

	public BigDecimal getBalanceDueReminder(Partner partner, Company company)  {
		log.debug("Compute balance due reminder (Partner : {}, Company : {})",partner.getName(),company.getName());

		int mailTransitTime = 0;

		AccountConfig accountConfig = company.getAccountConfig();

		if(accountConfig != null)  {
			mailTransitTime = accountConfig.getMailTransitTime();
		}

		Query query = JPA.em().createNativeQuery("SELECT SUM( COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "+
				"FROM public.account_move_line as ml  "+
				"LEFT OUTER JOIN ( "+
					"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
					"FROM public.account_move_line AS moveline "+
					"WHERE moveline.debit > 0 AND (( moveline.date_val = moveline.due_date AND (moveline.due_date + ?1 ) < ?2 ) " +
					"OR (moveline.due_date IS NOT NULL AND moveline.date_val != moveline.due_date AND moveline.due_date < ?2)" +
					"OR (moveline.due_date IS NULL AND moveline.date_val < ?2)) " +
					"GROUP BY moveline.id, moveline.amount_remaining) AS m1 ON (m1.moveline_id = ml.id) "+
				"LEFT OUTER JOIN ( "+
					"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
					"FROM public.account_move_line AS moveline "+
					"WHERE moveline.credit > 0 " +
					"GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "+
				"LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "+
				"LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "+
				"WHERE ml.partner = ?3 AND move.company = ?4 AND move.ignore_in_reminder_ok in ('false', null) " +
				"AND move.ignore_in_accounting_ok IN ('false', null) AND account.reconcile_ok = 'true' "+
				"AND move.status_select = ?5 AND ml.amount_remaining > 0 ")
				.setParameter(1, mailTransitTime)
				.setParameter(2, today.toDate(), TemporalType.DATE)
				.setParameter(3, partner)
				.setParameter(4, company)
				.setParameter(5, MoveRepository.STATUS_VALIDATED);

		BigDecimal balance = (BigDecimal)query.getSingleResult();

		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}

		log.debug("Balance due reminder : {}", balance);

		return balance;
	}


	/**
	 * Méthode permettant de récupérer l'ensemble des lignes d'écriture pour une société et un tiers
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @return
	 */
	public List<? extends MoveLine> getMoveLine(Partner partner, Company company)  {

		return Beans.get(MoveLineRepository.class).all().filter("self.partner = ?1 AND self.move.company = ?2", partner, company).fetch();

	}


	/**
	 * Procédure mettant à jour les soldes du compte client des tiers pour une société
	 * @param partnerList
	 * 				Une liste de tiers à mettre à jour
	 * @param company
	 * 				Une société
	 */
	public void updatePartnerAccountingSituation(List<Partner> partnerList, Company company, boolean updateCustAccount, boolean updateDueCustAccount, boolean updateDueReminderCustAccount)  {
		for(Partner partner : partnerList)  {
			AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, company);
			if(accountingSituation != null)  {
				this.updateAccountingSituationCustomerAccount(accountingSituation, updateCustAccount, updateDueCustAccount, updateDueReminderCustAccount);
			}
		}
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void flagPartners(List<Partner> partnerList, Company company)  {
		for(Partner partner : partnerList)  {
			AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, company);
			accountingSituation.setCustAccountMustBeUpdateOk(true);
			accSituationRepo.save(accountingSituation);
		}
	}


	/**
	 * Méthode permettant de mettre à jour les soldes du compte client d'un tiers.
	 * @param accountingSituation
	 * 				Un compte client
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateCustomerAccount(AccountingSituation accountingSituation)  {

		log.debug("Begin updateCustomerAccount service ...");

		Partner partner = accountingSituation.getPartner();
		Company company = accountingSituation.getCompany();

		accountingSituation.setBalanceCustAccount(this.getBalance(partner, company));
		accountingSituation.setBalanceDueCustAccount(this.getBalanceDue(partner, company));
		accountingSituation.setBalanceDueReminderCustAccount(this.getBalanceDueReminder(partner, company));

		accSituationRepo.save(accountingSituation);

		log.debug("End updateCustomerAccount service");
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public AccountingSituation updateAccountingSituationCustomerAccount(AccountingSituation accountingSituation, boolean updateCustAccount, boolean updateDueCustAccount, boolean updateDueReminderCustAccount)  {
		Partner partner = accountingSituation.getPartner();
		Company company = accountingSituation.getCompany();

		log.debug("Update customer account (Partner : {}, Company : {}, Update balance : {}, balance due : {}, balance due reminder : {})",
				partner.getName(), company.getName(), updateCustAccount, updateDueReminderCustAccount);

		if(updateCustAccount)  {
			accountingSituation.setBalanceCustAccount(this.getBalance(partner, company));
		}
		if(updateDueCustAccount)  {
			accountingSituation.setBalanceDueCustAccount(this.getBalanceDue(partner, company));
		}
		if(updateDueReminderCustAccount)  {
			accountingSituation.setBalanceDueReminderCustAccount(this.getBalanceDueReminder(partner, company));
		}
		accountingSituation.setCustAccountMustBeUpdateOk(false);
		accSituationRepo.save(accountingSituation);
		
		return accountingSituation;
	}



	public Account getPartnerAccount(Partner partner, Company company, boolean isSupplierInvoice) throws AxelorException  {

		if(isSupplierInvoice)  {  return this.getSupplierAccount(partner, company);  }

		else  {  return this.getCustomerAccount(partner, company);  }

	}


	protected Account getCustomerAccount(Partner partner, Company company) throws AxelorException  {

		Account customerAccount = null;

		AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, company);

		if(accountingSituation != null)   {

			customerAccount = accountingSituation.getCustomerAccount();
		}

		if(customerAccount == null)  {

			AccountConfigService accountConfigService = new AccountConfigService();

			customerAccount = accountConfigService.getCustomerAccount(accountConfigService.getAccountConfig(company));

		}

		if(customerAccount == null)  {

			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CUSTOMER_1),
					GeneralServiceImpl.EXCEPTION, company.getName()), IException.MISSING_FIELD);

		}

		return customerAccount;

	}


	protected Account getSupplierAccount(Partner partner, Company company) throws AxelorException  {

		Account supplierAccount = null;

		AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, company);

		if(accountingSituation != null)   {

			supplierAccount = accountingSituation.getSupplierAccount();
		}

		if(supplierAccount == null)  {

			AccountConfigService accountConfigService = new AccountConfigService();

			supplierAccount = accountConfigService.getSupplierAccount(accountConfigService.getAccountConfig(company));

		}

		if(supplierAccount == null)  {

			throw new AxelorException(String.format(I18n.get(IExceptionMessage.ACCOUNT_CUSTOMER_2),
					GeneralServiceImpl.EXCEPTION, company.getName()), IException.MISSING_FIELD);

		}

		return supplierAccount;

	}


}
