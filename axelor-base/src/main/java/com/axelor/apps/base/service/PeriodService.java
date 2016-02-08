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
package com.axelor.apps.base.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class PeriodService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PeriodService.class);
	
	@Inject
	private PeriodRepository periodRepo;
	
	/**
	 * Recupère la bonne période pour la date passée en paramètre
	 * @param date
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public Period rightPeriod(LocalDate date, Company company) throws AxelorException {
	
		Period period = this.getPeriod(date, company);
		if (period == null || period.getStatusSelect() == PeriodRepository.STATUS_CLOSED)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PERIOD_1), company.getName()), IException.CONFIGURATION_ERROR);
		}
		LOG.debug("Period : {}",period);	
		return period;
		
	}
	
	public Period getPeriod(LocalDate date, Company company)  {
		
		return periodRepo.all().filter("company = ?1 and fromDate <= ?2 and toDate >= ?3",company,date,date).fetchOne();

	}
	
	public Period getNextPeriod(Period period) throws AxelorException  {
		
		Period nextPeriod = periodRepo.all().filter("self.fromDate > ?1 AND self.company = ?2 AND self.statusSelect = ?3", period.getToDate(), period.getCompany(), PeriodRepository.STATUS_OPENED).fetchOne();
		
		if (nextPeriod == null || nextPeriod.getStatusSelect() == PeriodRepository.STATUS_CLOSED)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PERIOD_1), period.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		LOG.debug("Next Period : {}",nextPeriod);	
		return period;
	}
	
	public void testOpenPeriod(Period period) throws AxelorException {
		if(period.getStatusSelect()==PeriodRepository.STATUS_CLOSED){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PERIOD_2)), IException.CONFIGURATION_ERROR);
		}
	}
	
}
