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
package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskBusinessProjectComputeServiceImpl
    implements ProjectTaskBusinessProjectComputeService {
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectTimeUnitService projectTimeUnitService;
  protected TimesheetLineRepository timesheetLineRepository;
  protected AppBaseService appBaseService;
  protected ProjectTaskRepository projectTaskRepository;
  public static final int BIG_DECIMAL_SCALE = 2;

  @Inject
  public ProjectTaskBusinessProjectComputeServiceImpl(
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectTimeUnitService projectTimeUnitService,
      TimesheetLineRepository timesheetLineRepository,
      AppBaseService appBaseService,
      ProjectTaskRepository projectTaskRepository) {
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectTimeUnitService = projectTimeUnitService;
    this.timesheetLineRepository = timesheetLineRepository;
    this.appBaseService = appBaseService;
    this.projectTaskRepository = projectTaskRepository;
  }

  @Override
  public void computePlannedTime(ProjectTask projectTask) throws AxelorException {

    BigDecimal plannedTime = BigDecimal.ZERO;
    Unit unitTime = projectTask.getTimeUnit();
    Project project = projectTask.getProject();
    List<ProjectPlanningTime> projectPlanningTimeList = projectTask.getProjectPlanningTimeList();
    if (!CollectionUtils.isEmpty(projectPlanningTimeList)) {
      for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeList) {
        plannedTime =
            plannedTime.add(
                unitConversionForProjectService.convert(
                    projectPlanningTime.getTimeUnit(),
                    unitTime,
                    projectPlanningTime.getPlannedTime(),
                    projectTask.getPlannedTime().scale(),
                    project));
      }
    }
    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    if (!CollectionUtils.isEmpty(projectTaskList)) {
      for (ProjectTask task : projectTaskList) {
        computePlannedTime(task);
        plannedTime = plannedTime.add(task.getPlannedTime());
      }
    }

    projectTask.setPlannedTime(plannedTime);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void computeProjectTaskSpentTime(ProjectTask projectTask) throws AxelorException {
    BigDecimal spentTime = BigDecimal.ZERO;
    Unit timeUnit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);
    List<TimesheetLine> timeSheetLines =
        timesheetLineRepository
            .all()
            .filter("self.projectTask = :projectTask")
            .bind("projectTask", projectTask)
            .fetch();
    for (TimesheetLine timeSheetLine : timeSheetLines) {
      spentTime =
          spentTime.add(convertTimesheetLineDurationToProjectTaskUnit(timeSheetLine, timeUnit));
    }

    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    if (projectTaskList != null) {
      for (ProjectTask task : projectTaskList) {
        computeProjectTaskSpentTime(task);
        spentTime = spentTime.add(task.getSpentTime());
      }
    }

    projectTask.setSpentTime(spentTime);
    projectTaskRepository.save(projectTask);
  }

  @Override
  public BigDecimal convertTimesheetLineDurationToProjectTaskUnit(
      TimesheetLine timesheetLine, Unit timeUnit) throws AxelorException {
    String timeLoggingUnit = timesheetLine.getTimesheet().getTimeLoggingPreferenceSelect();
    BigDecimal duration = timesheetLine.getDuration();
    BigDecimal convertedDuration = BigDecimal.ZERO;

    Unit daysUnit = appBaseService.getUnitDays();
    Unit hoursUnit = appBaseService.getUnitHours();
    BigDecimal defaultHoursADay = appBaseService.getDailyWorkHours();
    if (defaultHoursADay.compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAILY_WORK_HOURS));
    }

    switch (timeLoggingUnit) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        if (timeUnit.equals(daysUnit)) {
          convertedDuration = duration;
        }
        if (timeUnit.equals(hoursUnit)) {
          convertedDuration = duration.multiply(defaultHoursADay);
        }
        break;
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        if (timeUnit.equals(hoursUnit)) {
          convertedDuration = duration;
        }
        if (timeUnit.equals(daysUnit)) {
          convertedDuration =
              duration.divide(defaultHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        break;
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        // convert to hours
        convertedDuration =
            duration.divide(new BigDecimal(60), BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        if (timeUnit.equals(daysUnit)) {
          convertedDuration =
              duration.divide(defaultHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        break;
      default:
        break;
    }

    return convertedDuration;
  }
}
