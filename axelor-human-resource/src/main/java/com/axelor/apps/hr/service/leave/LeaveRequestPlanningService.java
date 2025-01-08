package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;

public interface LeaveRequestPlanningService {

  WeeklyPlanning getWeeklyPlanning(LeaveRequest leave, Employee employee) throws AxelorException;

  WeeklyPlanning getWeeklyPlanning(Employee employee, Company comp) throws AxelorException;

  EventsPlanning getPublicHolidayEventsPlanning(LeaveRequest leave, Employee employee);

  EventsPlanning getPublicHolidayEventsPlanning(Employee employee, Company company);
}
