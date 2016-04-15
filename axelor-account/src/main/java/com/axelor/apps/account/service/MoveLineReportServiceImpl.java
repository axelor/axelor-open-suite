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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveLineReportRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveLineReportServiceImpl implements MoveLineReportService  {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveLineReportRepository moveLineReportRepo;

	protected DateTime dateTime;

	protected String query = "";

	protected AccountRepository accountRepo;
	
	protected List<Object> params = new ArrayList<Object>();
	protected int paramNumber = 1;


	@Inject
	public MoveLineReportServiceImpl(GeneralService generalService, MoveLineReportRepository moveLineReportRepo, AccountRepository accountRepo) {
		this.moveLineReportRepo = moveLineReportRepo;
		this.accountRepo = accountRepo;
		dateTime = generalService.getTodayDateTime();

	}


	public String getMoveLineList(MoveLineReport moveLineReport) throws AxelorException  {

		this.buildQuery(moveLineReport);

		int i = 1;
		
		String domainQuery = this.query;
		
		for(Object param : params.toArray())  {
			
			String paramStr = "";
			if(param instanceof Model)  {
				paramStr = ((Model)param).getId().toString();
			}
			else if(param instanceof Set)  {
				Set<Object> paramSet = (Set<Object>) param;
				for(Object object : paramSet)  {
					if(!paramStr.isEmpty())  {  paramStr += ",";  }
					paramStr = ((Model)object).getId().toString();
				}
			}
			else if(param instanceof LocalDate)  {
				paramStr = "'"+param.toString()+"'";
			}
			else  {
				paramStr = param.toString();
			}

			domainQuery = domainQuery.replace("?"+i, paramStr);
			i++;
		}
		
		log.debug("domainQuery : {}", domainQuery);

		return domainQuery;

	}
	
	
	public String buildQuery(MoveLineReport moveLineReport) throws AxelorException  {
		
		if(moveLineReport.getCompany() != null)  {
			this.addParams("self.move.company = ?%d", moveLineReport.getCompany());
		}

		if(moveLineReport.getCashRegister() != null)	{
			this.addParams("self.move.agency = ?%d", moveLineReport.getCashRegister());
		}

		if(moveLineReport.getDateFrom() != null)  {
			this.addParams("self.date >= ?%d", moveLineReport.getDateFrom());
		}

		if(moveLineReport.getDateTo() != null)  {
			this.addParams("self.date <= ?%d", moveLineReport.getDateTo());
		}

		if(moveLineReport.getDate() != null)  {
			this.addParams("self.date <= ?%d", moveLineReport.getDate());
		}

		if(moveLineReport.getJournal() != null)	{
			this.addParams("self.move.journal = ?%d", moveLineReport.getJournal());
		}

		if(moveLineReport.getPeriod() != null)	{
			this.addParams("self.move.period = ?%d", moveLineReport.getPeriod());
		}

		if(moveLineReport.getAccountSet() != null && !moveLineReport.getAccountSet().isEmpty())	{
			this.addParams("self.account in (?%d)", moveLineReport.getAccountSet());
		}

		if(moveLineReport.getPartnerSet() != null && !moveLineReport.getPartnerSet().isEmpty())	{
			this.addParams("self.partner in (?%d)", moveLineReport.getPartnerSet());
		}

		if(moveLineReport.getYear() != null)  {
			this.addParams("self.move.period.year = ?%d", moveLineReport.getYear());
		}

		if(moveLineReport.getPaymentMode() != null)	{
			this.addParams("self.move.paymentMode = ?%d", moveLineReport.getPaymentMode());
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("self.move.journal.type = ?%d", this.getJournalType(moveLineReport));
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("(self.move.accountingOk = false OR (self.move.accountingOk = true and self.move.moveLineReport = ?%d))", moveLineReport);
		}

		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			this.addParams("self.move.journal.notExportOk = false ");
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

		if(moveLineReport.getTypeSelect() == 4)  {
			this.addParams("self.amountRemaining > 0 AND self.debit > 0");
		}

		this.addParams("self.move.ignoreInAccountingOk = 'false'");

		log.debug("Query : {}", this.query);
		
		return this.query;

	}



	public String addParams(String paramQuery, Object param)  {

		log.debug("requete et param : {} : {}", paramQuery, paramNumber);
		
		this.addParams(String.format(paramQuery, paramNumber++));
		this.params.add(param);
		
		return this.query;

	}

	public String addParams(String paramQuery)  {

		if(!this.query.equals("")) {  this.query += " AND ";  }

		this.query += paramQuery;
		return this.query;

	}


	public void setSequence(MoveLineReport moveLineReport, String sequence)  {
		moveLineReport.setRef(sequence);
	}

	public String getSequence(MoveLineReport moveLineReport) throws AxelorException  {
		if(moveLineReport.getTypeSelect() <= 0)  { 	return null;  }

		SequenceService sequenceService = Beans.get(SequenceService.class);
		if(moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() >= 10 )  {

			String seq = sequenceService.getSequenceNumber(IAdministration.MOVE_LINE_REPORT, moveLineReport.getCompany());
			if(seq == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_LINE_REPORT_1),
						GeneralServiceImpl.EXCEPTION, moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}

			return seq;
		}
		else  {
			String seq = sequenceService.getSequenceNumber(IAdministration.MOVE_LINE_EXPORT, moveLineReport.getCompany());
			if(seq == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_LINE_REPORT_2),
						GeneralServiceImpl.EXCEPTION, moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}

			return seq;
		}
	}


	public JournalType getJournalType(MoveLineReport moveLineReport) throws AxelorException  {
		Company company = moveLineReport.getCompany();

		AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		switch (moveLineReport.getTypeSelect()) {
			case MoveLineReportRepository.EXPORT_SALES:

				return accountConfigService.getSaleJournalType(accountConfig);

			case MoveLineReportRepository.EXPORT_REFUNDS:

				return accountConfigService.getCreditNoteJournalType(accountConfig);

			case MoveLineReportRepository.EXPORT_TREASURY:

				return accountConfigService.getCashJournalType(accountConfig);

			case MoveLineReportRepository.EXPORT_PURCHASES:

				return accountConfigService.getPurchaseJournalType(accountConfig);

			default:
				break;
		}

		return null;
	}

	public Account getAccount(MoveLineReport moveLineReport)  {
		if(moveLineReport.getTypeSelect() ==  13 && moveLineReport.getCompany() != null)  {
			return accountRepo.all().filter("self.company = ?1 AND self.code LIKE '58%'", moveLineReport.getCompany()).fetchOne();
		}
		return null;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setStatus(MoveLineReport moveLineReport)  {
		moveLineReport.setStatusSelect(MoveLineReportRepository.STATUS_VALIDATED);
		moveLineReportRepo.save(moveLineReport);
	}

	/**
	 * @param moveLineReport
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setPublicationDateTime(MoveLineReport moveLineReport)  {
		moveLineReport.setPublicationDateTime(this.dateTime);
		moveLineReportRepo.save(moveLineReport);
	}


	/**
	 * @param queryFilter
	 * @return
	 */
	public BigDecimal getDebitBalance()  {

		Query q = JPA.em().createQuery("select SUM(self.debit) FROM MoveLine as self WHERE " + query, BigDecimal.class);
		
		int i = 1;
		
		for(Object param : params.toArray())  {
			q.setParameter(i++, param);
		}
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		log.debug("Total debit : {}", result);
		

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
	public BigDecimal getCreditBalance()  {

		Query q = JPA.em().createQuery("select SUM(self.credit) FROM MoveLine as self WHERE " + query, BigDecimal.class);
		
		int i = 1;
		
		for(Object param : params.toArray())  {
			q.setParameter(i++, param);
		}
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		log.debug("Total debit : {}", result);

		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}

	}


	public BigDecimal getDebitBalanceType4() {

		Query q = JPA.em().createQuery("select SUM(self.amountRemaining) FROM MoveLine as self WHERE " + query, BigDecimal.class);
		
		int i = 1;
		
		for(Object param : params.toArray())  {
			q.setParameter(i++, param);
		}
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		log.debug("Total debit : {}", result);

		if(result != null) {
			return result;
		}
		else {
			return BigDecimal.ZERO;
		}

	}


	public BigDecimal getCreditBalance(MoveLineReport moveLineReport, String queryFilter) {

		if(moveLineReport.getTypeSelect() == 4) {
			return this.getCreditBalanceType4();
		}

		else {
			return this.getCreditBalance();
		}

	}

	public BigDecimal getCreditBalanceType4() {

		return this.getDebitBalance().subtract(this.getDebitBalanceType4());

	}

}
