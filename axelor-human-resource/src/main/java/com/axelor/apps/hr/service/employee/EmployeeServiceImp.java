package com.axelor.apps.hr.service.employee;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.AuthUtils;

public class EmployeeServiceImp extends UserServiceImpl implements EmployeeService {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmployeeService.class);
	
	/**
	 * Convert  user duration to hours using time logging preference of user
	 * @param duration
	 * @return
	 */
	public BigDecimal getDurationHours(BigDecimal duration){
		
		LOG.debug("GET duration hours for duration: {}",duration);
		
		if(duration == null) { return null; }
		
		Employee employee = AuthUtils.getUser().getEmployee();
		
		LOG.debug("Employee: {}",employee);
		
		if(employee != null){
			String timePref = employee.getTimeLoggingPreferenceSelect();
			LOG.debug("Employee's time pref: {}, Daily Working hours: {}",timePref,employee.getDailyWorkHours());
			
			if(timePref.equals("days")){
				duration = duration.multiply(employee.getDailyWorkHours());
			}
			else if (timePref.equals("minutes")) {
				duration = duration.divide(new BigDecimal(60));
			}
		}
		
		LOG.debug("Calculated duration: {}",duration);
		return duration;
	}
	
	/**
	 * Convert hours duration to user duration using time logging preference of user
	 * @param duration
	 * @return
	 */
	public BigDecimal getUserDuration(BigDecimal duration){
		
		LOG.debug("Get user duration for duration: {}",duration);
		
		if(duration == null) { return null; }
		
		Employee employee = AuthUtils.getUser().getEmployee();
		LOG.debug("Employee: {}",employee);
		
		if(employee != null){
			String timePref = employee.getTimeLoggingPreferenceSelect();
			
			BigDecimal dailyWorkHrs = employee.getDailyWorkHours();
			LOG.debug("Employee's time pref: {}, Daily Working hours: {}",timePref,dailyWorkHrs);
			
			if(timePref.equals("days") && dailyWorkHrs != null && dailyWorkHrs.compareTo(BigDecimal.ZERO) != 0){
				duration = duration.divide(dailyWorkHrs);
			}
			else if (timePref.equals("minutes")) {
				duration = duration.multiply(new BigDecimal(60));
			}
		}
		
		LOG.debug("Calculated duration: {}",duration);
		return duration;
	}
		
}	
