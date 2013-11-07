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
package com.axelor.apps.account.service.batch;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchAccountCustomer extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchAccountCustomer.class);

	@Inject
	public BatchAccountCustomer(AccountCustomerService accountCustomerService) {
		
		super(accountCustomerService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
		
		super.start();
		
		checkPoint();

	}

	
	@Override
	public void process()  {
		
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		Company company = accountingBatch.getCompany();
		
		boolean updateCustAccountOk = accountingBatch.getUpdateCustAccountOk();
		boolean updateDueCustAccountOk = accountingBatch.getUpdateDueCustAccountOk();
		boolean updateDueReminderCustAccountOk = accountingBatch.getUpdateDueReminderCustAccountOk();
		
		List<AccountingSituation> accountingSituationList = AccountingSituation.all().filter("self.company = ?1", company).fetch();
		int i = 0;
		JPA.clear();
		for(AccountingSituation accountingSituation : accountingSituationList)  {
			try {
				
				accountingSituation = accountCustomerService.updateAccountingSituationCustomerAccount(
						AccountingSituation.find(accountingSituation.getId()),
						updateCustAccountOk,
						updateDueCustAccountOk,
						updateDueReminderCustAccountOk);
				
				if(accountingSituation != null)  {
					this.updateAccountingSituation(accountingSituation);
					i++;
				}
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Situation compable %s", 
						AccountingSituation.find(accountingSituation.getId()).getName()), e), IException.ACCOUNT_CUSTOMER, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour la situation compable {}", AccountingSituation.find(accountingSituation.getId()).getName());
				
			} finally {
				
				if (i % 1 == 0) { JPA.clear(); }
	
			}	
		}
	}
	
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = "";
		comment = "Compte rendu de la détermination des soldes des comptes clients :\n";
		comment += String.format("\t* %s Situation(s) compable(s) traitée(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}
	
	
	
	public String updateAccountingSituationMarked(Company company)  {
		
		int anomaly = 0;
		
		List<AccountingSituation> accountingSituationList = null;
		
		if(company != null)  {
			accountingSituationList = AccountingSituation.all().filter("self.company = ?1 and self.custAccountMustBeUpdateOk = 'true'", company).fetch();
		}
		else  {
			accountingSituationList = AccountingSituation.all().filter("self.custAccountMustBeUpdateOk = 'true'").fetch();
		}
		
		int i = 0;
		JPA.clear();
		for(AccountingSituation accountingSituation : accountingSituationList)  {
			try {
				
				accountingSituation = accountCustomerService.updateAccountingSituationCustomerAccount(
						AccountingSituation.find(accountingSituation.getId()),
						true, true, false);
				
				if(accountingSituation != null)  {
					i++;
				}
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Situation comptable %s", AccountingSituation.find(accountingSituation.getId()).getName()), e), IException.ACCOUNT_CUSTOMER, batch.getId());
				
				anomaly++;
				
				LOG.error("Bug(Anomalie) généré(e) pour le compte client {}",  AccountingSituation.find(accountingSituation.getId()));
				
			} finally {
				
				if (i % 5 == 0) { JPA.clear(); }
	
			}	
		}
		
		if(anomaly!=0)  {		
			return "Les soldes de "+anomaly+" situations comptables n'ont pas été mis à jour, merci de lancer le batch de mise à jour des comptes clients ";
		}
		else  {
			return "Les soldes de l'ensemble des situations compables ("+i+") ont été mis à jour.";
		}
	}
	
}
