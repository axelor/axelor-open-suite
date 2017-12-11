/**
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
package com.axelor.apps.hr.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.YearServiceImpl;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowanceRate;
import com.axelor.apps.hr.db.KilometricAllowanceRule;
import com.axelor.apps.hr.db.KilometricLog;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.KilometricAllowanceRateRepository;
import com.axelor.apps.hr.db.repo.KilometricLogRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class KilometricService {
	
	@Inject
	GeneralService generalService;
	
	@Inject
	KilometricLogRepository kilometricLogRepo;
	
	
	
	public KilometricLog getKilometricLog(Employee employee, LocalDate refDate) {
		
		for (KilometricLog log : employee.getKilometricLogList()) {
			
			if (log.getYear().getFromDate().isBefore(refDate) && log.getYear().getToDate().isAfter(refDate)){
				return log;
			}
		}
		return null;
	}
	
	public KilometricLog getCurrentKilometricLog(Employee employee){
		return getKilometricLog(employee, generalService.getTodayDate() );
	}
	
	public KilometricLog createKilometricLog(Employee employee, BigDecimal distance, Year year){
		
		KilometricLog log = new KilometricLog();
		log.setEmployee(employee);
		log.setDistanceTravelled(distance);
		log.setYear(year);
		return log;
	}
	
	public KilometricLog getOrCreateKilometricLog(Employee employee, LocalDate date) throws AxelorException{
		
		KilometricLog log = getKilometricLog(employee, date);
		
		if (log != null) { return log; }
		if (employee.getMainEmploymentContract() == null) { throw new AxelorException( String.format( I18n.get(IExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT), employee.getName() ), IException.CONFIGURATION_ERROR ); }
		
		Year year = Beans.get(YearServiceImpl.class).getYear(date, employee.getMainEmploymentContract().getPayCompany());
		
		if (year == null){
			throw new AxelorException( String.format( I18n.get(IExceptionMessage.KILOMETRIC_LOG_NO_YEAR), employee.getMainEmploymentContract().getPayCompany(), date)  , IException.CONFIGURATION_ERROR);
		}
		
		return createKilometricLog(employee, new BigDecimal("0.00"), year);
	}
	
	
	public BigDecimal computeKilometricExpense(ExpenseLine expenseLine, Employee employee) throws AxelorException{

		BigDecimal distance =  getDistanceTravelled(expenseLine);

		BigDecimal previousDistance;
		KilometricLog log = Beans.get(KilometricService.class).getKilometricLog(employee, expenseLine.getExpenseDate());
		if (log == null){
			previousDistance= BigDecimal.ZERO;
		}else {
			previousDistance= log.getDistanceTravelled();
		}
		
		KilometricAllowanceRate allowance = Beans.get(KilometricAllowanceRateRepository.class).all().filter("self.kilometricAllowParam = ?1", expenseLine.getKilometricAllowParam() ).fetchOne();
		
		List<KilometricAllowanceRule> ruleList = new ArrayList<>();
		
		for (KilometricAllowanceRule rule : allowance.getKilometricAllowanceRuleList() ) {
			
			if (rule.getMinimumCondition().compareTo( previousDistance.add(distance)) <= 0 && rule.getMaximumCondition().compareTo(previousDistance) >= 0 ){
				ruleList.add(rule);				
			}
		}
		
		if (ruleList.size() == 0) { throw new AxelorException( String.format(I18n.get( IExceptionMessage.KILOMETRIC_ALLOWANCE_NO_RULE ), allowance.getKilometricAllowParam().getName()) , IException.CONFIGURATION_ERROR); }
		
		BigDecimal price = BigDecimal.ZERO;
		
		if (ruleList.size() == 1){
			price = distance.multiply( ruleList.get(0).getRate()   );
		}else if (ruleList.size() > 0) {
			  Collections.sort(ruleList, new Comparator<KilometricAllowanceRule>() {
			      @Override
			      public int compare(final KilometricAllowanceRule object1, final KilometricAllowanceRule object2) {
			          return object1.getMinimumCondition().compareTo(object2.getMinimumCondition());
			      }
			  });
			  for (KilometricAllowanceRule rule : ruleList){
				  BigDecimal min = rule.getMinimumCondition().max( previousDistance  );
				  BigDecimal max = rule.getMaximumCondition().min(previousDistance.add(distance ) )  ;
				  price = price.add(  max.subtract(min).multiply(rule.getRate())  );
			  }
			}
		
		return price.setScale( generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
	}
	
	@Transactional
	public void updateKilometricLog(ExpenseLine expenseLine, Employee employee) throws AxelorException{
		
		KilometricLog log = getOrCreateKilometricLog(employee, expenseLine.getExpenseDate());
		log.setDistanceTravelled(log.getDistanceTravelled().add(getDistanceTravelled(expenseLine)));
		log.addExpenseLineListItem(expenseLine);
		kilometricLogRepo.save(log);
	}

	/**
	 * Get distance traveled according to kilometric type.
	 * 
	 * @param expenseLine
	 * @return
	 */
	private BigDecimal getDistanceTravelled(ExpenseLine expenseLine) {
		if (expenseLine.getKilometricTypeSelect().equals(ExpenseLineRepository.KILOMETRIC_TYPE_ROUND_TRIP)) {
			return expenseLine.getDistance().multiply(new BigDecimal(2));
		}
		return expenseLine.getDistance();
	}

}
