/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetLineServiceImpl implements TimesheetLineService {

  protected TimesheetService timesheetService;
  protected EmployeeRepository employeeRepository;
  protected TimesheetRepository timesheetRepo;
  protected AppHumanResourceService appHumanResourceService;
  protected UserHrService userHrService;

  @Inject
  public TimesheetLineServiceImpl(
      TimesheetService timesheetService,
      EmployeeRepository employeeRepository,
      TimesheetRepository timesheetRepo,
      AppHumanResourceService appHumanResourceService,
      UserHrService userHrService) {
    this.timesheetService = timesheetService;
    this.employeeRepository = employeeRepository;
    this.timesheetRepo = timesheetRepo;
    this.appHumanResourceService = appHumanResourceService;
    this.userHrService = userHrService;
  }

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public BigDecimal computeHoursDuration(Timesheet timesheet, BigDecimal duration, boolean toHours)
      throws AxelorException {
    if (duration == null) {
      return null;
    }
    AppBaseService appBaseService = Beans.get(AppBaseService.class);
    BigDecimal dailyWorkHrs;
    String timePref;

    log.debug(
        "Get user duration for duration: {}, timesheet: {}",
        duration,
        timesheet == null ? "null" : timesheet.getFullName());

    if (timesheet != null) {
      Employee employee = timesheet.getEmployee();

      timePref = timesheet.getTimeLoggingPreferenceSelect();

      if (employee != null) {
        employee = employeeRepository.find(employee.getId());

        log.debug("Employee: {}", employee);

        dailyWorkHrs = employee.getDailyWorkHours();
        if (timePref == null) {
          timePref = employee.getTimeLoggingPreferenceSelect();
        }
      } else {
        dailyWorkHrs = appBaseService.getAppBase().getDailyWorkHours();
      }
    } else {
      timePref = appBaseService.getAppBase().getTimeLoggingPreferenceSelect();
      dailyWorkHrs = appBaseService.getAppBase().getDailyWorkHours();
    }

    return computeHoursDuration(timePref, duration, dailyWorkHrs, toHours);
  }

  @Override
  public BigDecimal computeHoursDuration(
      String timePref, BigDecimal duration, BigDecimal dailyWorkHrs, boolean toHours)
      throws AxelorException {
    log.debug("Timesheet time pref: {}, Daily Working hours: {}", timePref, dailyWorkHrs);
    if (timePref == null) {
      return duration;
    }
    if (toHours) {
      duration = computeDurationToHours(timePref, duration, dailyWorkHrs);
    } else {
      duration = computeDurationFromHours(timePref, duration, dailyWorkHrs);
    }
    log.debug("Calculated duration: {}", duration);
    return duration;
  }

  protected BigDecimal computeDurationToHours(
      String timePref, BigDecimal duration, BigDecimal dailyWorkHrs) throws AxelorException {
    switch (timePref) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        if (dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAILY_WORK_HOURS));
        }
        return duration.multiply(dailyWorkHrs);
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return duration.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
      default:
        return duration;
    }
  }

  protected BigDecimal computeDurationFromHours(
      String timePref, BigDecimal duration, BigDecimal dailyWorkHrs) throws AxelorException {
    switch (timePref) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        if (dailyWorkHrs.compareTo(BigDecimal.ZERO) == 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAILY_WORK_HOURS));
        }
        return duration.divide(dailyWorkHrs, 2, RoundingMode.HALF_UP);
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return duration.multiply(new BigDecimal(60));
      default:
        return duration;
    }
  }

  @Override
  public TimesheetLine createTimesheetLine(
      Project project,
      Product product,
      Employee employee,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments) {

    TimesheetLine timesheetLine = new TimesheetLine();

    timesheetLine.setDate(date);
    timesheetLine.setComments(comments);
    timesheetLine.setProduct(product);
    timesheetLine.setProject(project);
    timesheetLine.setEmployee(employee);
    timesheetLine.setHoursDuration(hours);
    try {
      timesheetLine.setDuration(computeHoursDuration(timesheet, hours, false));
    } catch (AxelorException e) {
      log.error(e.getLocalizedMessage());
      TraceBackService.trace(e);
    }
    timesheet.addTimesheetLineListItem(timesheetLine);

    return timesheetLine;
  }

  @Override
  public TimesheetLine createTimesheetLine(
      Employee employee, LocalDate date, Timesheet timesheet, BigDecimal hours, String comments) {
    return createTimesheetLine(null, null, employee, date, timesheet, hours, comments);
  }

  @Override
  public TimesheetLine updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      Product product,
      Employee employee,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments) {

    timesheetLine.setDate(date);
    timesheetLine.setComments(comments);
    timesheetLine.setProduct(product);
    timesheetLine.setProject(project);
    timesheetLine.setEmployee(employee);
    timesheetLine.setHoursDuration(hours);
    try {
      timesheetLine.setDuration(computeHoursDuration(timesheet, hours, false));
    } catch (AxelorException e) {
      log.error(e.getLocalizedMessage());
      TraceBackService.trace(e);
    }
    timesheet.addTimesheetLineListItem(timesheetLine);

    return timesheetLine;
  }

  @Override
  public Duration computeTotalDuration(List<TimesheetLine> timesheetLineList) {
    if (timesheetLineList == null || timesheetLineList.isEmpty()) {
      return Duration.ZERO;
    }
    long totalSecDuration = 0L;
    for (TimesheetLine timesheetLine : timesheetLineList) {
      // if null, that means the timesheet line is just created so the parent is not canceled.
      if (timesheetLine.getTimesheet() == null
          || timesheetLine.getTimesheet().getStatusSelect()
              != TimesheetRepository.STATUS_CANCELED) {
        totalSecDuration +=
            timesheetLine.getHoursDuration().multiply(new BigDecimal("3600")).longValue();
      }
    }
    return Duration.ofSeconds(totalSecDuration);
  }

  @Override
  public Map<Project, BigDecimal> getProjectTimeSpentMap(List<TimesheetLine> timesheetLineList) {
    Map<Project, BigDecimal> projectTimeSpentMap = new HashMap<>();

    if (timesheetLineList != null) {

      for (TimesheetLine timesheetLine : timesheetLineList) {
        Project project = timesheetLine.getProject();
        BigDecimal hoursDuration = timesheetLine.getHoursDuration();

        if (project != null) {
          if (projectTimeSpentMap.containsKey(project)) {
            hoursDuration = hoursDuration.add(projectTimeSpentMap.get(project));
          }
          projectTimeSpentMap.put(project, hoursDuration);
        }
      }
    }

    return projectTimeSpentMap;
  }
}
