package com.axelor.apps.human.resource.service.user;

import java.math.BigDecimal;

import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;

public class UserServiceHrImpl extends UserServiceImpl implements UserServiceHr {
	
	/**
	 * Convert  user duration to hours using time logging preference of user
	 * @param duration
	 * @return
	 */
	public BigDecimal getDurationHours(BigDecimal duration){
		
		if(duration == null) { return null; }
		
		User user = AuthUtils.getUser();
		String timePref = user.getTimeLoggingPreferenceSelect();
		
		if(timePref.equals("days")){
			duration = duration.multiply(user.getDailyWorkHours());
		}
		else if (timePref.equals("minutes")) {
			duration = duration.divide(new BigDecimal(60));
		}
		
		return duration;
	}
	
	/**
	 * Convert hours duration to user duration using time logging preference of user
	 * @param duration
	 * @return
	 */
	public BigDecimal getUserDuration(BigDecimal duration){
		
		if(duration == null) { return null; }
		
		User user = AuthUtils.getUser();
		String timePref = user.getTimeLoggingPreferenceSelect();
		
		if(timePref.equals("days") && user.getDailyWorkHours() != null && user.getDailyWorkHours().compareTo(BigDecimal.ZERO) != 0){
			duration = duration.divide(user.getDailyWorkHours());
		}
		else if (timePref.equals("minutes")) {
			duration = duration.multiply(new BigDecimal(60));
		}
		
		return duration;
	}
		
}	
