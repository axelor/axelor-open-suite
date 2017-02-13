package com.axelor.apps.base.service;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;

public interface YearService {
	
	
	public Year getYear(LocalDate date, Company company);

}
