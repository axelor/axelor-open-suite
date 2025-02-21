package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlanningTimeComputeServiceImpl implements PlanningTimeComputeService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected AppBaseService appBaseService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected AppProjectService appProjectService;

  protected ProjectPlanningTimeRepository planningTimeRepo;

  @Inject
  public PlanningTimeComputeServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService,
      AppProjectService appProjectService,
      ProjectPlanningTimeRepository planningTimeRepo) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.appProjectService = appProjectService;
    this.planningTimeRepo = planningTimeRepo;
  }

  @Override
  public BigDecimal computePlannedTime(
      LocalDate fromDate, LocalDate toDate, Employee employee, Project project)
      throws AxelorException {
    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    if (fromDate == null
        || toDate == null
        || project == null
        || !project.getIsShowPlanning()
        || !Optional.ofNullable(appProjectService.getAppProject())
            .map(AppProject::getEnablePlanification)
            .orElse(false)) {
      return totalPlannedTime;
    }

    List<ProjectPlanningTime> projectPlanningTimeList = new ArrayList<>();
    if (employee == null) {
      projectPlanningTimeList= planningTimeRepo.findByProjectAndPeriod(project, fromDate, toDate).fetch();
    } else {
      projectPlanningTimeList =
          planningTimeRepo
              .findByEmployeeProjectAndPeriod(employee, project, fromDate, toDate)
              .fetch();
    }

    if (ObjectUtils.notEmpty(projectPlanningTimeList)) {
      Unit dayUnit = appBaseService.getAppBase().getUnitDays();

      for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeList) {
        BigDecimal plannedTime =
            getPlannedTimeInTargetUnit(
                projectPlanningTime.getTimeUnit(),
                dayUnit,
                projectPlanningTime.getPlannedTime(),
                projectPlanningTime.getProject());
        if (employee == null) {
          employee = projectPlanningTime.getEmployee();
        }
        BigDecimal prorata = computeProrata(projectPlanningTime, fromDate, toDate, employee);

        totalPlannedTime = totalPlannedTime.add(plannedTime.multiply(prorata));
      }
    }

    return totalPlannedTime;
  }

  protected BigDecimal getWorkingDays(LocalDate fromDate, LocalDate toDate, Employee employee) {
    BigDecimal workingDays = BigDecimal.ZERO;
    if (fromDate != null && toDate != null && employee != null) {
      for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
        double workingDay =
            weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);
        workingDays = workingDays.add(BigDecimal.valueOf(workingDay));
      }
    }
    return workingDays.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getPlannedTimeInTargetUnit(
      Unit startUnit, Unit endUnit, BigDecimal value, Project project) throws AxelorException {

    return unitConversionForProjectService.convert(
        startUnit, endUnit, value, value.scale(), project);
  }

  protected BigDecimal computeProrata(
      ProjectPlanningTime projectPlanningTime,
      LocalDate fromDate,
      LocalDate toDate,
      Employee employee) {
    if (fromDate == null || toDate == null) {
      return BigDecimal.ONE;
    }
    LocalDate startDate =
        Optional.of(projectPlanningTime)
            .map(ProjectPlanningTime::getStartDateTime)
            .map(LocalDateTime::toLocalDate)
            .orElse(fromDate);
    LocalDate endDate =
        Optional.of(projectPlanningTime)
            .map(ProjectPlanningTime::getEndDateTime)
            .map(LocalDateTime::toLocalDate)
            .orElse(toDate);
    LocalDate maxFromDate = fromDate.isAfter(startDate) ? fromDate : startDate;
    LocalDate minToDate = toDate.isBefore(endDate) ? toDate : endDate;
    BigDecimal jointDays = getWorkingDays(maxFromDate, minToDate, employee);
    BigDecimal totalDays = getWorkingDays(startDate, endDate, employee);
    BigDecimal prorata = BigDecimal.ONE;
    if (totalDays.signum() > 0) {
      prorata = jointDays.divide(totalDays, 2, RoundingMode.HALF_UP);
    }

    return prorata;
  }
}
