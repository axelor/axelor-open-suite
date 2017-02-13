package com.axelor.apps.base.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.google.inject.Inject;

public class YearServiceImpl implements YearService {
	
	protected YearRepository yearRepo;
	protected PartnerRepository partnerRepository;
	
	@Inject
	public YearServiceImpl(  PartnerRepository partnerRepository, YearRepository yearRepo)  {
		this.partnerRepository = partnerRepository;
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
			period.setCode(String.format("%02d", periodNumber)+"/"+year.getCode()+"_"+year.getCompany().getCode());
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
