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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EventsPlanning;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class EmployeeServiceImpl extends UserServiceImpl implements EmployeeService {

	@Inject
	private AppBaseService appBaseService;
	
	@Inject
	protected WeeklyPlanningService weeklyPlanningService;
	
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/**
	 * Convert hours duration to user duration using time logging preference of user
	 * @param duration
	 * @return
	 */
	@Override
	public BigDecimal getUserDuration(BigDecimal duration, User user, boolean toHours) throws AxelorException {

		LOG.debug("Get user duration for duration: {}, to hours : {}",  duration, toHours);

		if(duration == null) { return null; }

		if(user == null)  {  user = this.getUser();  }
		
		Employee employee = user.getEmployee();
		
		LOG.debug("Employee: {}",employee);

		BigDecimal dailyWorkHrs = new BigDecimal(1);
		String timePref = null;
		if(employee != null)  {
			timePref = employee.getTimeLoggingPreferenceSelect();
			dailyWorkHrs = employee.getDailyWorkHours();
		}
		if (timePref ==  null) {
			timePref = appBaseService.getAppBase().getTimeLoggingPreferenceSelect();
		}
		
		if(dailyWorkHrs == null || dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0)  {
			dailyWorkHrs = appBaseService.getAppBase().getDailyWorkHours();
			if (dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0) {
			    throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_EMPLOYEE_DAILY_WORK_HOURS),
						employee == null ? user.getName() : employee.getName()),
						IException.CONFIGURATION_ERROR);
			}
		}

		if (dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0) {
			dailyWorkHrs = new BigDecimal(1);
		}
		LOG.debug("Employee's time pref: {}, Daily Working hours: {}", timePref, dailyWorkHrs);
		
		if (timePref ==  null) {
			return duration;
		}
		
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
			Period period = Period.between(employee.getSeniorityDate(), refDate == null ? Beans.get(AppBaseService.class).getTodayDate() : refDate );
			return period.getYears();
		} catch (IllegalArgumentException e) {
			throw new AxelorException(e.getCause(), employee, IException.NO_VALUE, I18n.get(IExceptionMessage.EMPLOYEE_NO_SENIORITY_DATE ), employee.getName());
		}
		
	}
	
	public int getAge(Employee employee, LocalDate refDate) throws AxelorException{
		
		try{
			Period period = Period.between(employee.getBirthDate(), refDate == null ? Beans.get(AppBaseService.class).getTodayDate() : refDate );
			return period.getYears();
		} catch (IllegalArgumentException e) {
			throw new AxelorException(e.getCause(), employee, IException.NO_VALUE, I18n.get( IExceptionMessage.EMPLOYEE_NO_BIRTH_DATE ), employee.getName());
		}
	}

	@Override
	public BigDecimal getDaysWorksInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate) throws AxelorException {
		Company company = employee.getMainEmploymentContract().getPayCompany();
		BigDecimal duration = BigDecimal.ZERO;
		
		WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
		if(weeklyPlanning == null){
			HRConfig conf = company.getHrConfig();
			if(conf != null){
				weeklyPlanning = conf.getWeeklyPlanning();
			}
		}
		
		if (weeklyPlanning == null) {
			throw new AxelorException(employee, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.EMPLOYEE_PLANNING), employee.getName());
		}
		
		EventsPlanning publicHolidayPlanning = employee.getPublicHolidayEventsPlanning();
		if(publicHolidayPlanning == null){
			HRConfig conf = company.getHrConfig();
			if(conf != null){
				publicHolidayPlanning = conf.getPublicHolidayEventsPlanning();
			}
		}
		
		if (publicHolidayPlanning == null) {
			throw new AxelorException(employee, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.EMPLOYEE_PUBLIC_HOLIDAY), employee.getName());
		}

		LocalDate itDate = fromDate;

		while(!itDate.isAfter(toDate)){
			duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
			itDate = itDate.plusDays(1);
		}

		if (publicHolidayPlanning != null) {
			duration = duration.subtract(Beans.get(PublicHolidayService.class).computePublicHolidayDays(fromDate, toDate, weeklyPlanning, publicHolidayPlanning));
		}
		
		return duration;
	}

	@Override
	public BigDecimal getDaysWorkedInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate) throws AxelorException {
		BigDecimal daysWorks = getDaysWorksInPeriod(employee, fromDate, toDate);
		
		BigDecimal daysLeave = BigDecimal.ZERO;
		List<LeaveRequest> leaveRequestList = Beans.get(LeaveRequestRepository.class).all()
				.filter("self.user = ?1 AND self.duration >= 1 AND self.statusSelect = ?2 AND (self.fromDate BETWEEN ?3 AND ?4 OR self.toDate BETWEEN ?3 AND ?4)", 
						employee.getUser(), LeaveRequestRepository.STATUS_VALIDATED, fromDate, toDate).fetch();
		
		for (LeaveRequest leaveRequest : leaveRequestList) {
			daysLeave = daysLeave.add(
					Beans.get(LeaveService.class)
							.computeDuration(leaveRequest, fromDate, toDate)
			);
		}
		
		return daysWorks.subtract(daysLeave);
	}


}
