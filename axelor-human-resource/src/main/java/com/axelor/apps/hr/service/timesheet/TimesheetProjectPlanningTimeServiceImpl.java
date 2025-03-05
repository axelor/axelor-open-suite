/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TimesheetProjectPlanningTimeServiceImpl
    implements TimesheetProjectPlanningTimeService {

  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;
  protected TimesheetLineService timesheetLineService;
  protected AppBaseService appBaseService;
  protected UserHrService userHrService;

  @Inject
  public TimesheetProjectPlanningTimeServiceImpl(
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      TimesheetLineService timesheetLineService,
      AppBaseService appBaseService,
      UserHrService userHrService) {
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
    this.timesheetLineService = timesheetLineService;
    this.appBaseService = appBaseService;
    this.userHrService = userHrService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void generateLinesFromExpectedProjectPlanning(Timesheet timesheet) throws AxelorException {
    List<ProjectPlanningTime> planningList = getExpectedProjectPlanningTimeList(timesheet);
    for (ProjectPlanningTime projectPlanningTime : planningList) {
      TimesheetLine timesheetLine = createTimeSheetLineFromPPT(timesheet, projectPlanningTime);
      timesheet.addTimesheetLineListItem(timesheetLine);
    }
  }

  protected List<ProjectPlanningTime> getExpectedProjectPlanningTimeList(Timesheet timesheet) {
    List<ProjectPlanningTime> planningList;

    LocalDate toDate = timesheet.getToDate();
    if (toDate == null) {
      planningList =
          projectPlanningTimeRepository
              .all()
              .filter(
                  "self.employee.id = ?1 "
                      + "AND self.startDateTime >= ?2 "
                      + "AND self.id NOT IN "
                      + "(SELECT timesheetLine.projectPlanningTime.id FROM TimesheetLine as timesheetLine "
                      + "WHERE timesheetLine.projectPlanningTime != null "
                      + "AND timesheetLine.timesheet = ?3) ",
                  timesheet.getEmployee().getId(),
                  timesheet.getFromDate(),
                  timesheet)
              .fetch();
    } else {
      LocalDateTime toDateEndDay = toDate.plusDays(1).atStartOfDay();
      planningList =
          projectPlanningTimeRepository
              .all()
              .filter(
                  "self.employee.id = ?1 "
                      + "AND self.startDateTime >= ?2 AND self.endDateTime < ?3 "
                      + "AND self.id NOT IN "
                      + "(SELECT timesheetLine.projectPlanningTime.id FROM TimesheetLine as timesheetLine "
                      + "WHERE timesheetLine.projectPlanningTime != null "
                      + "AND timesheetLine.timesheet = ?4) ",
                  timesheet.getEmployee().getId(),
                  timesheet.getFromDate(),
                  toDateEndDay,
                  timesheet)
              .fetch();
    }
    return planningList;
  }

  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    TimesheetLine timesheetLine = new TimesheetLine();
    Project project = projectPlanningTime.getProject();
    BigDecimal plannedTime = getConvertedPlannedTime(timesheet, projectPlanningTime);
    timesheetLine.setDuration(plannedTime);
    timesheetLine.setHoursDuration(
        timesheetLineService.computeHoursDuration(timesheet, plannedTime, true));
    timesheetLine.setTimesheet(timesheet);
    timesheetLine.setEmployee(timesheet.getEmployee());
    Product product = projectPlanningTime.getProduct();
    if (product == null) {
      product =
          userHrService.getTimesheetProduct(
              timesheetLine.getEmployee(), projectPlanningTime.getProjectTask());
    }
    timesheetLine.setProduct(product);
    if (project.getManageTimeSpent()) {
      timesheetLine.setProjectTask(projectPlanningTime.getProjectTask());
      timesheetLine.setProject(projectPlanningTime.getProject());
      Site site = projectPlanningTime.getSite();
      if (site != null && site.getIsUsableOnTimesheet()) {
        timesheetLine.setSite(site);
      }
    }
    LocalDateTime startDateTime = projectPlanningTime.getStartDateTime();
    if (!Objects.isNull(startDateTime)) {
      timesheetLine.setDate(startDateTime.toLocalDate());
    }
    timesheetLine.setProjectPlanningTime(projectPlanningTime);
    return timesheetLine;
  }

  protected BigDecimal getConvertedPlannedTime(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    BigDecimal dailyWorkHours = timesheet.getEmployee().getDailyWorkHours();
    BigDecimal time = projectPlanningTime.getPlannedTime();
    Unit timeUnit = projectPlanningTime.getTimeUnit();
    String timeLoggingPreference = timesheet.getTimeLoggingPreferenceSelect();

    if (timeLoggingPreference == null && timesheet.getEmployee() != null) {
      timeLoggingPreference = timesheet.getEmployee().getTimeLoggingPreferenceSelect();
    }

    if (dailyWorkHours == null || dailyWorkHours.compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAILY_WORK_HOURS));
    }

    if (timeLoggingPreference == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIME_LOGGING_PREFERENCE));
    }

    switch (timeLoggingPreference) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return computeDaysDuration(dailyWorkHours, time, timeUnit);
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        return computeHoursDuration(dailyWorkHours, time, timeUnit);
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return computeMinutesDuration(dailyWorkHours, time, timeUnit);
      default:
        return BigDecimal.ZERO;
    }
  }

  protected BigDecimal computeDaysDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();

    if (Objects.equals(timeUnit, appBase.getUnitDays())) {
      return time;
    } else if (Objects.equals(timeUnit, appBase.getUnitHours())) {
      return time.divide(dailyWorkHours, 2, RoundingMode.HALF_UP);
    } else if (Objects.equals(timeUnit, appBase.getUnitMinutes())) {
      return time.divide(dailyWorkHours.multiply(BigDecimal.valueOf(60)), 2, RoundingMode.HALF_UP);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }

  protected BigDecimal computeHoursDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();

    if (Objects.equals(timeUnit, appBase.getUnitDays())) {
      return time.multiply(dailyWorkHours);
    } else if (Objects.equals(timeUnit, appBase.getUnitHours())) {
      return time;
    } else if (Objects.equals(timeUnit, appBase.getUnitMinutes())) {
      return time.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }

  protected BigDecimal computeMinutesDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();

    if (Objects.equals(timeUnit, appBase.getUnitDays())) {
      return time.multiply(dailyWorkHours.multiply(BigDecimal.valueOf(60)));
    } else if (Objects.equals(timeUnit, appBase.getUnitHours())) {
      return time.multiply(BigDecimal.valueOf(60));
    } else if (Objects.equals(timeUnit, appBase.getUnitMinutes())) {
      return time;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }
}
