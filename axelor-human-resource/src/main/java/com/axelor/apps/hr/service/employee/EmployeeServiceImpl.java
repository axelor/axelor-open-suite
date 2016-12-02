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
package com.axelor.apps.hr.service.employee;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class EmployeeServiceImpl extends UserServiceImpl implements EmployeeService {

	@Inject
	private GeneralService generalService;  
	
	private static final Logger LOG = LoggerFactory.getLogger(EmployeeService.class);

	/**
	 * Convert hours duration to user duration using time logging preference of user
	 * @param duration
	 * @return
	 */
	@Override
	public BigDecimal getUserDuration(BigDecimal duration, User user, boolean toHours)  {

		LOG.debug("Get user duration for duration: {}, to hours : {}",  duration, toHours);

		if(duration == null) { return null; }

		if(user == null)  {  user = this.getUser();  }
		
		Employee employee = user.getEmployee();
		
		LOG.debug("Employee: {}",employee);

		BigDecimal dailyWorkHrs = null;
		String timePref = null;
		if(employee != null)  {
			timePref = employee.getTimeLoggingPreferenceSelect();
			dailyWorkHrs = employee.getDailyWorkHours();
		}
		else {
			timePref = generalService.getGeneral().getTimeLoggingPreferenceSelect();
		}
		if(dailyWorkHrs == null || dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0)  {
			dailyWorkHrs = generalService.getGeneral().getDailyWorkHours();
		}

		LOG.debug("Employee's time pref: {}, Daily Working hours: {}", timePref, dailyWorkHrs);

		if(toHours)  {
			if(timePref.equals("days"))  {
				duration = duration.multiply(dailyWorkHrs);
			}
			else if (timePref.equals("minutes"))  {
				duration = duration.divide(new BigDecimal(60),4, RoundingMode.HALF_UP);
			}
		}
		else  {
			if(timePref.equals("days"))  {
				duration = duration.divide(dailyWorkHrs,4, RoundingMode.HALF_UP);
			}
			else if (timePref.equals("minutes"))  {
				duration = duration.multiply(new BigDecimal(60));
			}
		}

		LOG.debug("Calculated duration: {}",  duration);
		return duration;
	}
	
	public int getLengthOfService(Employee employee, LocalDate refDate) throws AxelorException{
		
		try{
			Years years = Years.yearsBetween(employee.getSeniorityDate(), refDate == null ? Beans.get(GeneralService.class).getTodayDate() : refDate );
			return years.getYears();
		}catch (IllegalArgumentException e){
			throw new AxelorException(String.format( I18n.get( IExceptionMessage.EMPLOYEE_NO_SENIORITY_DATE ), employee.getName() ), IException.NO_VALUE);
		}
		
	}
	
	public int getAge(Employee employee, LocalDate refDate) throws AxelorException{
		
		try{
			Years years = Years.yearsBetween(employee.getBirthDate(), refDate == null ? Beans.get(GeneralService.class).getTodayDate() : refDate );
			return years.getYears();
		}catch (IllegalArgumentException e){
			throw new AxelorException(String.format( I18n.get( IExceptionMessage.EMPLOYEE_NO_BIRTH_DATE ), employee.getName() ), IException.NO_VALUE);
		}
	}

}
