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
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
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
	
	private String query;

	@Inject
	public MoveLineReportService() {

		dateTime = GeneralService.getTodayDateTime();

	}
	

	public String getMoveLineList(MoveLineReport moveLineReport) throws AxelorException  {
		
		this.initQuery();

		this.buildQuery(moveLineReport);
		
		LOG.debug("Requete : {}", this.query);

		return this.query;

	}
	
	
	public void initQuery()  {
		
		this.query = "";
		
	}
	
	
	public String buildQuery(MoveLineReport moveLineReport) throws AxelorException  {
		
		if(moveLineReport.getCompany() != null)  {  
			this.addParams("self.move.company = %s", moveLineReport.getCompany().getId().toString());
		}

		if(moveLineReport.getCashRegister() != null)	{
			this.addParams("self.move.agency = %s", moveLineReport.getCashRegister().getId().toString());
		}
		
		if(moveLineReport.getDateFrom() != null)  {
			this.addParams("self.date >='%s'", moveLineReport.getDateFrom().toString());
		}

		if(moveLineReport.getDateTo() != null)  {
			this.addParams("self.date <= '%s'", moveLineReport.getDateTo().toString());
		}

		if(moveLineReport.getDate() != null)  {
			this.addParams("self.date <= '%s'", moveLineReport.getDate().toString());
		}

		if(moveLineReport.getJournal() != null)	{
			this.addParams("self.move.journal = %s", moveLineReport.getJournal().getId().toString());
		}

		if(moveLineReport.getPeriod() != null)	{
			this.addParams("self.move.period = %s", moveLineReport.getPeriod().getId().toString());
		}

		if(moveLineReport.getAccount() != null)	{
			this.addParams("self.account = %s", moveLineReport.getAccount().getId().toString());
		}

		if(moveLineReport.getFromPartner() != null)	{
			this.addParams("self.partner.name >= '%s'", moveLineReport.getFromPartner().getName().replace("'", " "));
		}

		if(moveLineReport.getToPartner() != null)	{
			this.addParams("self.partner.name <= '%s'", moveLineReport.getToPartner().getName().replace("'", " "));
		}

		if(moveLineReport.getPartner() != null)	{
			this.addParams("self.partner = %s", moveLineReport.getPartner().getId().toString());
		}

		if(moveLineReport.getYear() != null)  {
			this.addParams("self.move.period.year = %s", moveLineReport.getYear().getId().toString());
		}

		if(moveLineReport.getPaymentMode() != null)	{
			this.addParams("self.move.paymentMode = %s", moveLineReport.getPaymentMode().getId().toString());
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("self.move.journal.type = %s", this.getJournalType(moveLineReport).getId().toString());
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("(self.move.accountingOk = false OR (self.move.accountingOk = true and self.move.moveLineReport = %s))", moveLineReport.getId().toString());
		}
		
		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("self.move.journal.notExportOk = false ");
		}
		
		if(moveLineReport.getTypeSelect() == 5)	{
			this.addParams("self.move.paymentMode.code = 'CHQ'");
		}

		if(moveLineReport.getTypeSelect() == 10)	{
			this.addParams("self.move.paymentMode.code = 'ESP'");
		}

		if(moveLineReport.getTypeSelect() == 5)	{
			this.addParams("self.amountPaid > 0 AND self.credit > 0");
		}

		if(moveLineReport.getTypeSelect() == 10)	{
			this.addParams("self.credit > 0");
		}

		if(moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() == 10)	{
			this.addParams("self.account.reconcileOk = 'true'");
		}

		if(moveLineReport.getTypeSelect() == 1)  {
			this.addParams("self.credit > 0");
		}

		if(moveLineReport.getTypeSelect() == 12)  {
			this.addParams("self.account.code LIKE '7%'");
		}

		if(moveLineReport.getTypeSelect() == 4)  {
			this.addParams("self.amountRemaining > 0 AND self.debit > 0");
		}

		this.addParams("self.move.ignoreInAccountingOk = 'false'");
		
		return this.query;
		
	}
	
	
	
	public String addParams(String paramQuery, String param)  {
		
		this.addParams(String.format(paramQuery, param));
		return this.query;
		
	}
	
	public String addParams(String paramQuery)  {
		
		if(!this.query.equals("")) {  this.query += " AND ";  }
		
		this.query += paramQuery;
		return this.query;
		
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setSequence(MoveLineReport moveLineReport, String sequence)  {
		moveLineReport.setRef(sequence);
		moveLineReport.save();
	}

	public String getSequence(MoveLineReport moveLineReport) throws AxelorException  {
		if(moveLineReport.getTypeSelect() <= 0)  { 	return null;  }
			
		SequenceService sequenceService = injector.getInstance(SequenceService.class);
		if(moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() >= 10 )  {

			String seq = sequenceService.getSequenceNumber(IAdministration.MOVE_LINE_REPORT, moveLineReport.getCompany());
			if(seq == null)  {  
				throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Reporting comptable pour la société %s",
						GeneralServiceAccount.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}
			
			return seq;
		}
		else  {
			String seq = sequenceService.getSequenceNumber(IAdministration.MOVE_LINE_EXPORT, moveLineReport.getCompany());
			if(seq == null)  {  
				throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Export comptable pour la société %s",
						GeneralServiceAccount.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}
			
			return seq;
		}
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
			return Account.filter("self.company = ?1 AND self.code LIKE '58%'", moveLineReport.getCompany()).fetchOne();
		}
		return null;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setStatus(MoveLineReport moveLineReport)  {
		moveLineReport.setStatus(Status.findByCode("val"));
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
	
	
	public BigDecimal getDebitBalanceType4(String queryFilter) {
		
		Query q = JPA.em().createQuery("select SUM(self.amountRemaining) FROM MoveLine as self WHERE " + queryFilter, BigDecimal.class);
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Total debit : {}", result);
		
		if(result != null) {
			return result;
		}
		else {
			return BigDecimal.ZERO;
		}
		
	}
		
		
	public BigDecimal getCreditBalance(MoveLineReport moveLineReport, String queryFilter) {
		
		if(moveLineReport.getTypeSelect() == 4) {
			return this.getCreditBalanceType4(queryFilter);
		}
		
		else {
			return this.getCreditBalance(queryFilter);
		}
		
	}
	
	public BigDecimal getCreditBalanceType4(String queryFilter) {
		
		return this.getDebitBalance(queryFilter).subtract(this.getDebitBalanceType4(queryFilter));
		
	}
	
}
