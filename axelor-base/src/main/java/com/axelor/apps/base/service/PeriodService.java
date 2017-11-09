/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.lang.invoke.MethodHandles;

public class PeriodService {
	
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private PeriodRepository periodRepo;

	@Inject
	private AdjustHistoryService adjustHistoryService;

	/**
	 * Recupère la bonne période pour la date passée en paramètre
	 * @param date
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public Period rightPeriod(LocalDate date, Company company) throws AxelorException {
	
		Period period = this.getPeriod(date, company);
		if (period == null || period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PERIOD_1), company.getName());
		}
		LOG.debug("Period : {}",period);	
		return period;
		
	}
	
	public Period getPeriod(LocalDate date, Company company)  {
		
		return periodRepo.all().filter("year.company = ?1 and fromDate <= ?2 and toDate >= ?3",company,date,date).fetchOne();

	}
	
	public Period getNextPeriod(Period period) throws AxelorException  {
		
		Period nextPeriod = periodRepo.all().filter("self.fromDate > ?1 AND self.year.company = ?2 AND self.statusSelect = ?3", period.getToDate(), period.getYear().getCompany(), PeriodRepository.STATUS_OPENED).fetchOne();
		
		if (nextPeriod == null || nextPeriod.getStatusSelect() == PeriodRepository.STATUS_CLOSED)  {
			throw new AxelorException(period, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PERIOD_1), period.getYear().getCompany().getName());
		}
		LOG.debug("Next Period : {}",nextPeriod);	
		return nextPeriod;
	}
	
	public void testOpenPeriod(Period period) throws AxelorException {
		if (period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
			throw new AxelorException(period, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PERIOD_2));
		}
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void close(Period period) {
		period = periodRepo.find(period.getId());

		if (period.getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
			adjustHistoryService.setEndDate(period);
		}

		period.setStatusSelect(PeriodRepository.STATUS_CLOSED);
		period.setClosureDateTime(LocalDateTime.now());
		periodRepo.save(period);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void adjust(Period period) {
		period = periodRepo.find(period.getId());

		adjustHistoryService.setStartDate(period);

		period.setStatusSelect(PeriodRepository.STATUS_ADJUSTING);
		periodRepo.save(period);
	}
}
