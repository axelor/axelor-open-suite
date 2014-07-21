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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReportedBalance;
import com.axelor.apps.account.db.ReportedBalanceLine;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.db.Year;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class YearService {
	
	private static final Logger LOG = LoggerFactory.getLogger(YearService.class);
	
	@Inject
	private ReminderService rs;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	/**
	 * Procédure permettant de cloturer un exercice comptable
	 * @param year
	 * 			Un exercice comptable
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void closeYear(Year year) throws AxelorException  {
		year = Year.find(year.getId());
		Status status = Status.findByCode("clo");
		for (Period period : year.getPeriodList())  {
			period.setStatus(status);
		}
		Company company = year.getCompany();
		if(company == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez renseigner une société pour l'année fiscale %s",
					GeneralServiceAccount.getExceptionAccountingMsg(),year.getName()), IException.CONFIGURATION_ERROR);
		}

		Query q = JPA.em().createQuery("select DISTINCT(ml.partner) FROM MoveLine as ml WHERE ml.date >= ?1 AND ml.date <= ?2 AND ml.company = ?3");
		q.setParameter(1, year.getFromDate());
		q.setParameter(2, year.getToDate());
		q.setParameter(3, year.getCompany());
				
		@SuppressWarnings("unchecked")
		List<Partner> partnerList = q.getResultList();
		
		List<Partner> partnerListAll = Partner.all().fetch();

		
		LOG.debug("Nombre total de tiers : {}", partnerListAll.size());
		LOG.debug("Nombre de tiers récupéré : {}", partnerList.size());

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		Account customerAccount = accountConfigService.getCustomerAccount(accountConfig);
		Account doubtfulCustomerAccount = accountConfigService.getDoubtfulCustomerAccount(accountConfig);
		
		for(Partner partner : partnerList)  {
			partner = Partner.find(partner.getId());
			LOG.debug("Tiers en cours de traitement : {}", partner.getName());
			boolean find = false;
			for(ReportedBalance reportedBalance : partner.getReportedBalanceList())  {
				if(reportedBalance.getCompany().equals(company))  {
					// On ajoute une ligne au A nouveau trouvé
					LOG.debug("On ajoute une ligne au A nouveau trouvé");
					
					ReportedBalanceLine reportedBalanceLine = this.createReportedBalanceLine(
							reportedBalance, 
							this.computeReportedBalance(year.getFromDate(), year.getToDate(), partner, customerAccount, doubtfulCustomerAccount),
							year);
					LOG.debug("ReportedBalanceLine : {}",reportedBalanceLine);
					reportedBalance.getReportedBalanceLineList().add(reportedBalanceLine);
//					year.getReportedBalanceLineList().add(reportedBalanceLine);
					reportedBalance.save();
					find = true;
				}
				
			}
			if(!find)  {
				// On crée un A nouveau et on lui ajoute une ligne
				LOG.debug("On crée un A nouveau et on lui ajoute une ligne");
				ReportedBalance reportedBalance = this.createReportedBalance(company, partner);
				ReportedBalanceLine reportedBalanceLine = this.createReportedBalanceLine(
						reportedBalance,
						this.computeReportedBalance(year.getFromDate(), year.getToDate(), partner, customerAccount, doubtfulCustomerAccount),
						year);
//				year.getReportedBalanceLineList().add(reportedBalanceLine);
				LOG.debug("ReportedBalanceLine : {}",reportedBalanceLine);
				reportedBalance.getReportedBalanceLineList().add(reportedBalanceLine);
				reportedBalance.save();
			}
			
			partner.save();
		}
		year.setStatus(status);
		year.save();
	}
	
	
	/**
	 * Fonction permettant de créer un A nouveau
	 * @param company
	 * 				Une société
	 * @return
	 * 				Un A nouveau
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ReportedBalance createReportedBalance(Company company, Partner partner)  {
		ReportedBalance reportedBalance = new ReportedBalance();
		reportedBalance.setCompany(company);
		reportedBalance.setPartner(partner);
		reportedBalance.setReportedBalanceLineList(new ArrayList<ReportedBalanceLine>());
		reportedBalance.save();
		return reportedBalance;
	}
	
	
	/**
	 * Fonction permettant de créer un Solde rapporté
	 * @param reportedBalance
	 * 				Un A nouveau
	 */
	@Transactional
	public ReportedBalanceLine createReportedBalanceLine(ReportedBalance reportedBalance, BigDecimal amount, Year year)  {
		ReportedBalanceLine reportedBalanceLine = new ReportedBalanceLine();
		reportedBalanceLine.setReportedBalance(reportedBalance);
		reportedBalanceLine.setAmount(amount);
		reportedBalanceLine.setYear(year);
		reportedBalanceLine.save();
		return reportedBalanceLine;
	}
	
	
	/**
	 * Fonction permettant de calculer le solde rapporté
	 * @param fromDate
	 * 				La date de début d'exercice comptable
	 * @param toDate
	 * 				La date de fin d'exercice comptable
	 * @param partner
	 * 				Un client payeur
	 * @param account
	 * 				Le compte client
	 * @return
	 * 				Le solde rapporté
	 */
	public BigDecimal computeReportedBalance(LocalDate fromDate, LocalDate toDate, Partner partner, Account account, Account account2)  {
		Query q = JPA.em().createQuery("select SUM(ml.credit-ml.debit) FROM MoveLine as ml " +
				"WHERE ml.partner = ?1 AND ml.ignoreInAccountingOk = false AND ml.date >= ?2 AND ml.date <= ?3 AND (ml.account = ?4 OR ml.account = ?5) ", BigDecimal.class);
		q.setParameter(1, partner);
		q.setParameter(2, fromDate);
		q.setParameter(3, toDate);
		q.setParameter(4, account);
		q.setParameter(5, account2);

		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Solde rapporté (result) : {}", result);

		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}
	}
	
	
	
	@Deprecated
	public BigDecimal computeReportedBalance2(LocalDate fromDate, LocalDate toDate, Partner partner, Account account)  {
		
		List<MoveLine> moveLineList = MoveLine.all()
				.filter("self.partner = ?1 AND self.ignoreInAccountingOk = 'false' AND self.date >= ?2 AND self.date <= ?3 AND self.account = ?4", 
						partner, fromDate, toDate, account).fetch();
		
		BigDecimal reportedBalanceAmount = BigDecimal.ZERO;
		for(MoveLine moveLine : moveLineList)  {
			if(moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  { 
				reportedBalanceAmount = reportedBalanceAmount.subtract(moveLine.getAmountRemaining());
			}
			else if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
				reportedBalanceAmount = reportedBalanceAmount.add(moveLine.getAmountRemaining());
			}
		}
		if (LOG.isDebugEnabled())  {  LOG.debug("Solde rapporté : {}", reportedBalanceAmount);  }
		return reportedBalanceAmount;
		
		
		
	}
	
	
	public List<Period> generatePeriods(Year year){
		
		List<Period> periods = new ArrayList<Period>();
		Integer duration = year.getPeriodDurationSelect();
		LocalDate fromDate = year.getFromDate();
		LocalDate toDate = year.getToDate();
		LocalDate periodToDate = fromDate;
		Integer periodNumber = 1;
		
		while(periodToDate.isBefore(toDate)){
			if(periodNumber != 1)
				fromDate = fromDate.plusMonths(duration);
			periodToDate = fromDate.plusMonths(duration).minusDays(1);
			if(periodToDate.isAfter(toDate))
				periodToDate = toDate; 
			if(fromDate.isAfter(toDate))
				continue;
			Period period = new Period();
			period.setFromDate(fromDate);
			period.setToDate(periodToDate);
			period.setYear(year);
			period.setName(String.format("%02d", periodNumber)+"/"+year.getCode());
			period.setCode(String.format("%02d", periodNumber)+"/"+year.getCode()+"_"+year.getCompany().getCode());
			period.setCompany(year.getCompany());
			period.setStatus(year.getStatus());
			periods.add(period);
			periodNumber ++;
		}
		return periods;
	}
	
	
	
}
