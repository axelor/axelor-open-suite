package com.axelor.apps.organisation.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Period;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class TimeSheetPeriodService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TimeSheetPeriodService.class); 
	
	/**
	 * Recupère la bonne période pour la date passée en paramètre
	 * @param date
	 * @param company
	 * @return
	 * @throws AxelorException 
	 */
	public Period rightPeriod(LocalDate date, Company company) throws AxelorException {
	
		Period period = Period.all().filter("company = ?1 and fromDate <= ?2 and toDate >= ?3",company,date,date).fetchOne();
		if (period == null || period.getStatus().getCode().equals("clo"))  {
			throw new AxelorException(String.format("Aucune période trouvée ou celle-ci clôturée pour la société %s", company.getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			LOG.debug("Period : {}",period);	
			return period;
		}
		
	}
	
}
