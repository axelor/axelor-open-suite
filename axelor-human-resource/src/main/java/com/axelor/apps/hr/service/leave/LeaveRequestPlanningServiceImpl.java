package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;

public class LeaveRequestPlanningServiceImpl implements LeaveRequestPlanningService {

  @Override
  public WeeklyPlanning getWeeklyPlanning(LeaveRequest leave, Employee employee)
      throws AxelorException {
    Company comp = leave.getCompany();
    return getWeeklyPlanning(employee, comp);
  }

  @Override
  public WeeklyPlanning getWeeklyPlanning(Employee employee, Company comp) throws AxelorException {
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    if (weeklyPlanning == null) {
      if (comp != null) {
        HRConfig conf = comp.getHrConfig();
        if (conf != null) {
          weeklyPlanning = conf.getWeeklyPlanning();
        }
      }
    }
    if (weeklyPlanning == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }
    return weeklyPlanning;
  }

  @Override
  public EventsPlanning getPublicHolidayEventsPlanning(LeaveRequest leave, Employee employee) {
    Company company = leave.getCompany();
    return getPublicHolidayEventsPlanning(employee, company);
  }

  @Override
  public EventsPlanning getPublicHolidayEventsPlanning(Employee employee, Company company) {
    EventsPlanning publicHolidayPlanning = employee.getPublicHolidayEventsPlanning();
    if (publicHolidayPlanning == null && company != null && company.getHrConfig() != null) {
      publicHolidayPlanning = company.getHrConfig().getPublicHolidayEventsPlanning();
    }
    return publicHolidayPlanning;
  }
}
