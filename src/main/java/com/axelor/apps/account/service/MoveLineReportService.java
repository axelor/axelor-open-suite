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

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class MoveLineReportService {

	private static final Logger LOG = LoggerFactory.getLogger(MoveLineReportService.class);

	@Inject
	private Injector injector;

	private DateTime dateTime;

	@Inject
	public MoveLineReportService() {

		dateTime = GeneralService.getTodayDateTime();

	}

	public String getMoveLineList(MoveLineReport moveLineReport) throws AxelorException  {

		String query = "";
		String and = " AND ";

		if(moveLineReport.getCompany() != null)  {  query += String.format("self.move.company = %s", moveLineReport.getCompany().getId());  } 

		if(moveLineReport.getCashRegister() != null)	{
			if(!query.equals(""))  {  query += and;  }
			query += String.format("self.move.agency = %s", moveLineReport.getCashRegister().getId());  
		}
		
		if(moveLineReport.getDateFrom() != null)  {
			if(!query.equals(""))  { query += and;  }
			query += String.format("self.date >='%s'", moveLineReport.getDateFrom().toString());  
		}

		if(moveLineReport.getDateTo() != null)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}

		if(moveLineReport.getDate() != null)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}

		if(moveLineReport.getJournal() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.journal = %s", moveLineReport.getJournal().getId());  
		}

		if(moveLineReport.getPeriod() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.period = %s", moveLineReport.getPeriod().getId());  
		}

		if(moveLineReport.getAccount() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.account = %s", moveLineReport.getAccount().getId());  
		}

		if(moveLineReport.getFromPartner() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.partner.name >= '%s'", moveLineReport.getFromPartner().getName().replace("'", " "));  
		}

		if(moveLineReport.getToPartner() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.partner.name <= '%s'", moveLineReport.getToPartner().getName().replace("'", " "));  
		}

		if(moveLineReport.getPartner() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.partner = %s", moveLineReport.getPartner().getId());  
		}

		if(moveLineReport.getYear() != null)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.period.year = %s", moveLineReport.getYear().getId()); 
		}

		if(moveLineReport.getPaymentMode() != null)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.paymentMode = %s", moveLineReport.getPaymentMode().getId());  
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.journal.type = %s", this.getJournalType(moveLineReport).getId());  
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("(self.move.accountingOk = false OR (self.move.accountingOk = true and self.move.moveLineReport = %s)) ", moveLineReport.getId());  
		}
		
		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.journal.notExportOk = false ");  
		}
		
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 5)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.paymentMode.code = 'CHQ'");  
		}

		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 10)	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.move.paymentMode.code = 'ESP'");  
		}

		if(moveLineReport.getTypeSelect() != null &&( moveLineReport.getTypeSelect() == 5 ))	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.amountPaid > 0 AND self.credit > 0");  
		}

		if(moveLineReport.getTypeSelect() != null &&( moveLineReport.getTypeSelect() == 10 ))	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.credit > 0");  
		}

		if(moveLineReport.getTypeSelect() != null && ( moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() == 10 ))	{
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.account.reconcileOk = 'true'");  
		}

		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 1)  {
			if(!query.equals("")) {  query += and;  }
			query += String.format("self.credit > 0");
		}

		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 12)  {
			if(!query.equals("")) {  query += and;  }
			query += "self.account.code LIKE '7%'";
		}

		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 4)  {
			if(!query.equals("")) {  query += and;  }
			query += "self.amountRemaining > 0 AND self.debit > 0";
		}

		if(!query.equals("")) {  query += and;  }
		query += String.format("self.move.ignoreInAccountingOk = 'false'");  

		LOG.debug("Requete : {}", query);

		return query;

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setSequence(MoveLineReport moveLineReport, String sequence)  {
		moveLineReport.setRef(sequence);
		moveLineReport.save();
	}

	public String getSequence(MoveLineReport moveLineReport) throws AxelorException  {
		if(moveLineReport.getTypeSelect() > 0)  {

			SequenceService sgs = injector.getInstance(SequenceService.class);
			if(moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() >= 10 )  {

				String seq = sgs.getSequence(IAdministration.MOVE_LINE_REPORT, moveLineReport.getCompany(), false);
				if(seq != null)  {  
					return seq;
				}
				else  {  
					throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Reporting comptable pour la société %s",
							GeneralService.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
			else  {
				String seq = sgs.getSequence(IAdministration.MOVE_LINE_EXPORT, moveLineReport.getCompany(), false);
				if(seq != null)  {  
					return seq;
				}
				else  {  
					throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Export comptable pour la société %s",
							GeneralService.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else  return "";
	}


	public JournalType getJournalType(MoveLineReport moveLineReport) throws AxelorException  {
		Company company = moveLineReport.getCompany();
		
		AccountConfigService accountConfigService = injector.getInstance(AccountConfigService.class);
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		switch (moveLineReport.getTypeSelect()) {
			case IAccount.EXPORT_SALES:
				
				return accountConfigService.getSaleJournalType(accountConfig);
				
			case IAccount.EXPORT_REFUNDS:
				
				return accountConfigService.getCreditNoteJournalType(accountConfig);
				
			case IAccount.EXPORT_TREASURY:
				
				return accountConfigService.getCashJournalType(accountConfig);
				
			case IAccount.EXPORT_PURCHASES:
				
				return accountConfigService.getPurchaseJournalType(accountConfig);
	
			default:
				break;
		}
		
		return null;
	}
	
	public Account getAccount(MoveLineReport moveLineReport)  {
		if(moveLineReport.getTypeSelect() ==  13 && moveLineReport.getCompany() != null)  {
			return Account.all().filter("self.company = ?1 AND self.code LIKE '58%'", moveLineReport.getCompany()).fetchOne();
		}
		return null;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setStatus(MoveLineReport moveLineReport)  {
		moveLineReport.setStatus(Status.all().filter("self.code = 'val'").fetchOne());
		moveLineReport.save();
	}

	/**
	 * @param moveLineReport
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setPublicationDateTime(MoveLineReport moveLineReport)  {
		moveLineReport.setPublicationDateTime(this.dateTime);
		moveLineReport.save();
	}


	/**
	 * @param queryFilter
	 * @return
	 */
	public BigDecimal getDebitBalance(String queryFilter)  {

		Query q = JPA.em().createQuery("select SUM(self.debit) FROM MoveLine as self WHERE " + queryFilter, BigDecimal.class);

		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Total debit : {}", result);

		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}

	}

	
	/**
	 * @param queryFilter
	 * @return
	 */
	public BigDecimal getCreditBalance(String queryFilter)  {

		Query q = JPA.em().createQuery("select SUM(self.credit) FROM MoveLine as self WHERE " + queryFilter, BigDecimal.class);

		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Total debit : {}", result);

		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}

	}
}
