/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetProjectPlanningTimeServiceImpl
    implements TimesheetProjectPlanningTimeService {

  protected static final int BIGDECIMAL_SCALE = 2;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;
  protected TimesheetLineService timesheetLineService;
  protected AppBaseService appBaseService;
  protected UserHrService userHrService;
  protected UnitConversionRepository unitConversionRepository;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected LeaveRequestService leaveRequestService;
  protected PublicHolidayHrService publicHolidayHrService;

  @Inject
  public TimesheetProjectPlanningTimeServiceImpl(
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      TimesheetLineService timesheetLineService,
      AppBaseService appBaseService,
      UserHrService userHrService,
      UnitConversionRepository unitConversionRepository,
      UnitConversionForProjectService unitConversionForProjectService,
      WeeklyPlanningService weeklyPlanningService,
      LeaveRequestService leaveRequestService,
      PublicHolidayHrService publicHolidayHrService) {
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
    this.timesheetLineService = timesheetLineService;
    this.appBaseService = appBaseService;
    this.userHrService = userHrService;
    this.unitConversionRepository = unitConversionRepository;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.leaveRequestService = leaveRequestService;
    this.publicHolidayHrService = publicHolidayHrService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void generateLinesFromExpectedProjectPlanning(Timesheet timesheet) throws AxelorException {
    List<ProjectPlanningTime> planningList = getExpectedProjectPlanningTimeList(timesheet);
    for (ProjectPlanningTime projectPlanningTime : planningList) {
      createTimeSheetLinesFromPPT(timesheet, projectPlanningTime);
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
                      + "WHERE timesheetLine.projectPlanningTime IS NOT null "
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
                      + "WHERE timesheetLine.projectPlanningTime IS NOT null "
                      + "AND timesheetLine.timesheet = ?4) ",
                  timesheet.getEmployee().getId(),
                  timesheet.getFromDate(),
                  toDateEndDay,
                  timesheet)
              .fetch();
    }
    return planningList;
  }

  protected void createTimeSheetLinesFromPPT(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    LocalDate startDate =
        Optional.ofNullable(projectPlanningTime.getStartDateTime())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    LocalDate endDate =
        Optional.ofNullable(projectPlanningTime.getEndDateTime())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    if (startDate == null || endDate == null) {
      return;
    }
    BigDecimal plannedTime = getConvertedPlannedTime(timesheet, projectPlanningTime);
    Project project = projectPlanningTime.getProject();
    Employee employee = timesheet.getEmployee();
    ProjectTask projectTask = projectPlanningTime.getProjectTask();
    Site site = projectPlanningTime.getSite();
    Product product = projectPlanningTime.getProduct();
    if (product == null) {
      product = userHrService.getTimesheetProduct(employee, projectTask);
    }
    List<LocalDate> dateList = new ArrayList<>();

    while (!startDate.isAfter(endDate)) {
      if (weeklyPlanningService.isWorkingDay(employee.getWeeklyPlanning(), startDate)
          && !leaveRequestService.isLeaveDay(employee, startDate)
          && !publicHolidayHrService.checkPublicHolidayDay(startDate, employee)) {
        dateList.add(startDate);
      }
      startDate = startDate.plusDays(1);
    }
    if (CollectionUtils.isEmpty(dateList) || dateList.size() <= 0) {
      return;
    }
    int totalLines = dateList.size();
    BigDecimal duration =
        plannedTime.divide(
            BigDecimal.valueOf(totalLines),
            AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
            RoundingMode.HALF_UP);
    for (int i = 0; i < totalLines; i++) {
      TimesheetLine timesheetLine =
          createTimeSheetLineFromPPT(
              timesheet,
              projectPlanningTime,
              dateList.get(i),
              duration,
              project,
              product,
              projectTask,
              site);
      timesheet.addTimesheetLineListItem(timesheetLine);
    }
  }

  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet,
      ProjectPlanningTime projectPlanningTime,
      LocalDate date,
      BigDecimal plannedTime,
      Project project,
      Product product,
      ProjectTask projectTask,
      Site site)
      throws AxelorException {
    TimesheetLine timesheetLine = new TimesheetLine();
    timesheetLine.setDuration(plannedTime);
    timesheetLine.setHoursDuration(
        timesheetLineService.computeHoursDuration(timesheet, plannedTime, true));
    timesheetLine.setTimesheet(timesheet);
    timesheetLine.setEmployee(timesheet.getEmployee());
    timesheetLine.setProduct(product);
    if (project.getManageTimeSpent()) {
      timesheetLine.setProjectTask(projectTask);
      timesheetLine.setProject(project);
      if (site != null && site.getIsUsableOnTimesheet()) {
        timesheetLine.setSite(site);
      }
    }
    timesheetLine.setDate(date);
    timesheetLine.setProjectPlanningTime(projectPlanningTime);
    return timesheetLine;
  }

  protected BigDecimal getConvertedPlannedTime(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    BigDecimal dailyWorkHours = timesheet.getEmployee().getDailyWorkHours();
    BigDecimal time = projectPlanningTime.getPlannedTime();
    Unit timeUnit = projectPlanningTime.getTimeUnit();
    String timeLoggingPreference = timesheet.getTimeLoggingPreferenceSelect();
    Project project = projectPlanningTime.getProject();

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
        return computeDaysDuration(dailyWorkHours, time, timeUnit, project);
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        return computeHoursDuration(dailyWorkHours, time, timeUnit, project);
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return computeMinutesDuration(dailyWorkHours, time, timeUnit, project);
      default:
        return BigDecimal.ZERO;
    }
  }

  protected BigDecimal computeDaysDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit, Project project)
      throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    Unit unitDays = appBase.getUnitDays();
    List<UnitConversion> unitConversions = fetchUnitConversionForProjectList(timeUnit, unitDays);
    if (Objects.equals(timeUnit, unitDays)) {
      return time;
    } else if (Objects.equals(timeUnit, appBase.getUnitHours())) {
      return time.divide(dailyWorkHours, BIGDECIMAL_SCALE, RoundingMode.HALF_UP);
    } else if (Objects.equals(timeUnit, appBase.getUnitMinutes())) {
      return time.divide(
          dailyWorkHours.multiply(BigDecimal.valueOf(60)), BIGDECIMAL_SCALE, RoundingMode.HALF_UP);
    } else if (ObjectUtils.notEmpty(unitConversions)) {
      return unitConversionForProjectService.convert(
          timeUnit, unitDays, time, BIGDECIMAL_SCALE, project);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }

  protected BigDecimal computeHoursDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit, Project project)
      throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    Unit unitHours = appBase.getUnitHours();
    List<UnitConversion> unitConversions = fetchUnitConversionForProjectList(timeUnit, unitHours);
    if (Objects.equals(timeUnit, appBase.getUnitDays())) {
      return time.multiply(dailyWorkHours);
    } else if (Objects.equals(timeUnit, unitHours)) {
      return time;
    } else if (Objects.equals(timeUnit, appBase.getUnitMinutes())) {
      return time.divide(BigDecimal.valueOf(60), BIGDECIMAL_SCALE, RoundingMode.HALF_UP);
    } else if (ObjectUtils.notEmpty(unitConversions)) {
      return unitConversionForProjectService.convert(
          timeUnit, unitHours, time, BIGDECIMAL_SCALE, project);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }

  protected BigDecimal computeMinutesDuration(
      BigDecimal dailyWorkHours, BigDecimal time, Unit timeUnit, Project project)
      throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    Unit unitMinutes = appBase.getUnitMinutes();
    List<UnitConversion> unitConversions = fetchUnitConversionForProjectList(timeUnit, unitMinutes);
    if (Objects.equals(timeUnit, appBase.getUnitDays())) {
      return time.multiply(dailyWorkHours.multiply(BigDecimal.valueOf(60)));
    } else if (Objects.equals(timeUnit, appBase.getUnitHours())) {
      return time.multiply(BigDecimal.valueOf(60));
    } else if (Objects.equals(timeUnit, unitMinutes)) {
      return time;
    } else if (ObjectUtils.notEmpty(unitConversions)) {
      return unitConversionForProjectService.convert(
          timeUnit, unitMinutes, time, BIGDECIMAL_SCALE, project);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_WRONG_TIME_UNIT));
  }

  protected List<UnitConversion> fetchUnitConversionForProjectList(Unit startUnit, Unit endUnit) {
    return unitConversionRepository
        .all()
        .filter(
            "(self.entitySelect = :entitySelectProject or (self.entitySelect = :entitySelectAll and self.typeSelect = :typeSelect))"
                + "AND ((self.startUnit = :startUnit AND self.endUnit = :endUnit) OR (self.startUnit = :endUnit AND self.endUnit = :startUnit))")
        .bind("entitySelectProject", UnitConversionRepository.ENTITY_PROJECT)
        .bind("entitySelectAll", UnitConversionRepository.ENTITY_ALL)
        .bind("typeSelect", UnitConversionRepository.TYPE_COEFF)
        .bind("startUnit", startUnit)
        .bind("endUnit", endUnit)
        .fetch()
        .stream()
        .sorted(Comparator.comparing(UnitConversion::getEntitySelect).reversed())
        .collect(Collectors.toList());
  }
}
