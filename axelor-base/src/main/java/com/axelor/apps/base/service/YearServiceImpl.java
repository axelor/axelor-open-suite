/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.google.inject.Inject;

public class YearServiceImpl implements YearService {
	
	protected YearRepository yearRepo;
	
	@Inject
	public YearServiceImpl(YearRepository yearRepo)  {
		this.yearRepo = yearRepo;
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
			period.setCode((String.format("%02d", periodNumber)+"/"+year.getCode()+"_"+year.getCompany().getCode()).toUpperCase());
			period.setStatusSelect(year.getStatusSelect());
			periods.add(period);
			periodNumber ++;
		}
		return periods;
	}

	@Override
	public Year getYear(LocalDate date, Company company) {
		
		return yearRepo.all().filter("self.company = ?1 AND self.fromDate < ?2 AND self.toDate >= ?2", company, date).fetchOne();
	}

}
