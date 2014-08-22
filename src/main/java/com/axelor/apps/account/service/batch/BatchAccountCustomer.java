/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchAccountCustomer extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchAccountCustomer.class);

	@Inject
	public BatchAccountCustomer(AccountCustomerService accountCustomerService) {
		
		super(accountCustomerService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
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
		
		List<AccountingSituation> accountingSituationList = (List<AccountingSituation>) AccountingSituation.all().filter("self.company = ?1", company).fetch();
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
			accountingSituationList = (List<AccountingSituation>) AccountingSituation.all().filter("self.company = ?1 and self.custAccountMustBeUpdateOk = 'true'", company).fetch();
		}
		else  {
			accountingSituationList = (List<AccountingSituation>) AccountingSituation.all().filter("self.custAccountMustBeUpdateOk = 'true'").fetch();
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
