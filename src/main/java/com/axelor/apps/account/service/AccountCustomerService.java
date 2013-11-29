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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountCustomerService {
	private static final Logger LOG = LoggerFactory.getLogger(AccountCustomerService.class); 

	
	private LocalDate today;
	
	@Inject
	public AccountCustomerService() {

		this.today = GeneralService.getTodayDate();
		
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
		LOG.debug("Compute balance (Partner : {}, Company : {})",partner.getName(),company.getName());

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
												"AND move.state = 'validated' AND ml.amount_remaining > 0 ")
												.setParameter(1, partner)
												.setParameter(2, company);
		
		BigDecimal balance = (BigDecimal)query.getSingleResult();
		
		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}
		
		LOG.debug("Balance : {}", balance);	
		
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
		LOG.debug("Compute balance due (Partner : {}, Company : {})",partner.getName(),company.getName());
		
		Query query = JPA.em().createNativeQuery("SELECT SUM( COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "+
				"FROM public.account_move_line AS ml  "+
				"LEFT OUTER JOIN ( "+
					"SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "+
					"FROM public.account_move_line AS moveline "+
					"WHERE moveline.debit > 0 " +
					"AND ((moveline.due_date IS NULL AND moveline.date <= ?1) OR (moveline.due_date IS NOT NULL AND moveline.due_date <= ?1)) " +
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
				"AND move.state = 'validated' AND ml.amount_remaining > 0 ")
				.setParameter(1, today.toDate(), TemporalType.DATE)
				.setParameter(2, partner)
				.setParameter(3, company);

		BigDecimal balance = (BigDecimal)query.getSingleResult();
		
		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}
		
		LOG.debug("Balance due : {}", balance);	
		
		return balance;
	}
	
	
	
	/******************************************  2. Calcul du solde exigible (relançable) du tiers  ******************************************/
	/** solde des factures exigibles non bloquées en relance et dont « la date de facture » + « délai d’acheminement(X) » <« date du jour » 
	 *  si la date de facture = date d'échéance de facture, sinon pas de prise en compte du délai d'acheminement ***/
	/** solde des échéances rejetées qui ne sont pas bloqués ******************************************************/
	
	public BigDecimal getBalanceDueReminder(Partner partner, Company company)  {
		LOG.debug("Compute balance due reminder (Partner : {}, Company : {})",partner.getName(),company.getName());
		
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
					"WHERE moveline.debit > 0 AND (( moveline.date = moveline.due_date AND (moveline.due_date + ?1 ) < ?2 ) " +
					"OR (moveline.due_date IS NOT NULL AND moveline.date != moveline.due_date AND moveline.due_date < ?2)" +
					"OR (moveline.due_date IS NULL AND moveline.date < ?2)) " +
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
				"AND move.state = 'validated' AND ml.amount_remaining > 0 ")
				.setParameter(1, mailTransitTime)
				.setParameter(2, today.toDate(), TemporalType.DATE)
				.setParameter(3, partner)
				.setParameter(4, company);
		
		BigDecimal balance = (BigDecimal)query.getSingleResult();
		
		if(balance == null)  {
			balance = BigDecimal.ZERO;
		}
		
		LOG.debug("Balance due reminder : {}", balance);	
		
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
	public List<MoveLine> getMoveLine(Partner partner, Company company)  {
		
		return MoveLine.all().filter("self.partner = ?1 AND self.move.company = ?2", partner, company).fetch();

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
			AccountingSituation accountingSituation = this.getAccountingSituation(partner, company);
			if(accountingSituation != null)  {
				this.updateAccountingSituationCustomerAccount(accountingSituation, updateCustAccount, updateDueCustAccount, updateDueReminderCustAccount);
			}
		}
	}
	
	
	public AccountingSituation getAccountingSituation(Partner partner, Company company)  {
		for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {
			if(accountingSituation.getCompany().equals(company))  {
				return accountingSituation;
			}
		}
		return null;
	}
	
	
	/**
	 * Méthode permettant de récupérer la liste des tiers distincts impactés par l'écriture
	 * @param move
	 * 			Une écriture
	 * @return
	 */
	public List<Partner> getPartnerOfMove(Move move)  {
		List<Partner> partnerList = new ArrayList<Partner>();
		for(MoveLine moveLine : move.getMoveLineList())  {
			if(moveLine.getAccount() != null && moveLine.getAccount().getReconcileOk() && moveLine.getPartner() != null
					&& !partnerList.contains(moveLine.getPartner()))  {
				partnerList.add(moveLine.getPartner());
			}
		}
		return partnerList;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void flagPartners(List<Partner> partnerList, Company company)  {
		for(Partner partner : partnerList)  {
			AccountingSituation accountingSituation = this.getAccountingSituation(partner, company);
			accountingSituation.setCustAccountMustBeUpdateOk(true);
			accountingSituation.save();
		}
	}
	
	
	/**
	 * Méthode permettant de mettre à jour les soldes du compte client d'un tiers.
	 * @param accountingSituation
	 * 				Un compte client
	 */
	@Transactional
	public void updateCustomerAccount(AccountingSituation accountingSituation)  {
		
		LOG.debug("Begin updateCustomerAccount service ...");
		
		Partner partner = accountingSituation.getPartner();
		Company company = accountingSituation.getCompany();
		
		accountingSituation.setBalanceCustAccount(this.getBalance(partner, company));
		accountingSituation.setBalanceDueCustAccount(this.getBalanceDue(partner, company));
		accountingSituation.setBalanceDueReminderCustAccount(this.getBalanceDueReminder(partner, company));
		
		accountingSituation.save();
		
		LOG.debug("End updateCustomerAccount service");
	}
	
	
	@Transactional
	public AccountingSituation updateAccountingSituationCustomerAccount(AccountingSituation accountingSituation, boolean updateCustAccount, boolean updateDueCustAccount, boolean updateDueReminderCustAccount)  {
		Partner partner = accountingSituation.getPartner();
		Company company = accountingSituation.getCompany();
		
		LOG.debug("Update customer account (Partner : {}, Company : {}, Update balance : {}, balance due : {}, balance due reminder : {})",
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
		accountingSituation.save();
		return accountingSituation;
	}
	
	
}
