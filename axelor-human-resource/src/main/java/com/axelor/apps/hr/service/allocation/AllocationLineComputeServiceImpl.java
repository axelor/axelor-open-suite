package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.AllocationLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveDaysService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.UnitConversionForProjectService;
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
import java.util.stream.Collectors;

public class AllocationLineComputeServiceImpl implements AllocationLineComputeService {

  protected LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected AppBaseService appBaseService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected AppProjectService appProjectService;
  protected LeaveRequestRepository leaveRequestRepo;
  protected AllocationLineRepository allocationLineRepo;
  protected ProjectPlanningTimeRepository planningTimeRepo;
  protected TimesheetLineRepository timesheetLineRepository;
  protected AppHumanResourceService appHumanResourceService;

  @Inject
  public AllocationLineComputeServiceImpl(
      LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService,
      WeeklyPlanningService weeklyPlanningService,
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService,
      AppProjectService appProjectService,
      LeaveRequestRepository leaveRequestRepo,
      AllocationLineRepository allocationLineRepo,
      ProjectPlanningTimeRepository planningTimeRepo,
      TimesheetLineRepository timesheetLineRepository,
      AppHumanResourceService appHumanResourceService) {
    this.leaveRequestComputeLeaveDaysService = leaveRequestComputeLeaveDaysService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.appProjectService = appProjectService;
    this.leaveRequestRepo = leaveRequestRepo;
    this.allocationLineRepo = allocationLineRepo;
    this.planningTimeRepo = planningTimeRepo;
    this.timesheetLineRepository = timesheetLineRepository;
    this.appHumanResourceService = appHumanResourceService;
  }

  @Override
  public BigDecimal getLeaves(LocalDate fromDate, LocalDate toDate, Employee employee)
      throws AxelorException {
    BigDecimal leaveDayCount = BigDecimal.ZERO;
    if (fromDate == null || toDate == null || employee == null) {
      return leaveDayCount;
    }

    List<LeaveRequest> leaveRequestList =
        leaveRequestRepo
            .all()
            .filter(
                "self.statusSelect = :statusValidated AND self.employee = :employee AND ((self.fromDateT BETWEEN :fromDate AND :toDate OR self.toDateT BETWEEN :fromDate AND :toDate) OR (:toDate BETWEEN self.fromDateT AND self.toDateT OR :fromDate BETWEEN self.fromDateT AND self.toDateT))")
            .bind("statusValidated", LeaveRequestRepository.STATUS_VALIDATED)
            .bind("employee", employee)
            .bind("fromDate", fromDate)
            .bind("toDate", toDate)
            .fetch();
    if (ObjectUtils.notEmpty(leaveRequestList)) {
      for (LeaveRequest leaveRequest : leaveRequestList) {
        leaveDayCount =
            leaveDayCount.add(
                leaveRequestComputeLeaveDaysService.computeLeaveDaysByLeaveRequest(
                    fromDate, toDate, leaveRequest, employee));
      }
    }
    return leaveDayCount.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getAlreadyAllocated(
      AllocationLine allocationLine, Period period, Employee employee) {
    BigDecimal alreadyAllocated = BigDecimal.ZERO;
    if (period == null) {
      return alreadyAllocated;
    }

    List<AllocationLine> allocationLineList =
        allocationLineRepo.findByPeriodAndEmployee(period, employee).fetch();
    if (ObjectUtils.notEmpty(allocationLineList)) {
      alreadyAllocated =
          allocationLineList.stream()
              .filter(line -> !line.equals(allocationLine))
              .map(AllocationLine::getAllocated)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return alreadyAllocated.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getAvailableAllocation(
      LocalDate fromDate,
      LocalDate toDate,
      Employee employee,
      BigDecimal leaves,
      BigDecimal alreadyAllocated) {
    BigDecimal availableAllocation = BigDecimal.ZERO;
    if (fromDate != null && toDate != null && employee != null) {
      BigDecimal workingDays = getWorkingDays(fromDate, toDate, employee);
      availableAllocation = workingDays.subtract(leaves).subtract(alreadyAllocated);
    }
    return availableAllocation.setScale(2, RoundingMode.HALF_UP);
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
      projectPlanningTimeList =
          planningTimeRepo.findByProjectAndPeriod(project, fromDate, toDate).fetch();
    } else {
      projectPlanningTimeList =
          planningTimeRepo
              .findByEmployeeProjectAndPeriod(employee, project, fromDate, toDate)
              .fetch();
    }
    List<ProjectPlanningTime> projectPlanningTimeList = new ArrayList<>();
    if (employee == null) {
      planningTimeRepo.findByProjectAndPeriod(project, fromDate, toDate).fetch();
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
            unitConversionForProjectService.convert(
                projectPlanningTime.getTimeUnit(),
                dayUnit,
                projectPlanningTime.getPlannedTime(),
                projectPlanningTime.getPlannedTime().scale(),
                projectPlanningTime.getProject());
        if (employee == null) {
          employee = projectPlanningTime.getEmployee();
        }
        BigDecimal prorata = computeProrata(projectPlanningTime, fromDate, toDate, employee);

        totalPlannedTime =
            totalPlannedTime
                .add(plannedTime.multiply(prorata))
                .setScale(plannedTime.scale(), RoundingMode.HALF_UP);
      }
    }

    return totalPlannedTime;
  }

  @Override
  public BigDecimal computeSpentTime(
      LocalDate fromDate, LocalDate toDate, Employee employee, Project project)
      throws AxelorException {
    BigDecimal totalSpentTime = BigDecimal.ZERO;
    if (fromDate == null
        || toDate == null
        || project == null
        || !project.getManageTimeSpent()
        || appHumanResourceService.getAppTimesheet() == null
        || !Optional.ofNullable(appProjectService.getAppProject())
            .map(AppProject::getEnablePlanification)
            .orElse(false)) {
      return totalSpentTime;
    }

    List<TimesheetLine> timesheetLineList = new ArrayList<>();
    if (employee == null) {
      timesheetLineList =
          timesheetLineRepository.findByProjectAndPeriod(project, fromDate, toDate).fetch();
    } else {
      timesheetLineList =
          timesheetLineRepository
              .findByEmployeeProjectAndPeriod(employee, project, fromDate, toDate)
              .fetch();
    }

    if (ObjectUtils.notEmpty(timesheetLineList)) {
      Unit dayUnit = appBaseService.getAppBase().getUnitDays();

      for (TimesheetLine timesheetLine : timesheetLineList) {
        BigDecimal spentTime =
            unitConversionForProjectService.convert(
                appBaseService.getAppBase().getUnitHours(),
                dayUnit,
                timesheetLine.getHoursDuration(),
                timesheetLine.getHoursDuration().scale(),
                timesheetLine.getProject());
        if (employee == null) {
          employee = timesheetLine.getEmployee();
        }
        totalSpentTime =
            totalSpentTime.add(spentTime).setScale(spentTime.scale(), RoundingMode.HALF_UP);
      }
    }

    return totalSpentTime;
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

  @Override
  public BigDecimal getAllocatedTime(Project project, Sprint sprint) {
    List<AllocationLine> allocationLineList =
        allocationLineRepo.findByProject(project).fetch();
    BigDecimal allocatedTime = BigDecimal.ZERO;
    if (ObjectUtils.isEmpty(allocationLineList)) {
      return allocatedTime;
    }

    for (AllocationLine allocationLine : allocationLineList) {
      Employee employee = allocationLine.getEmployee();
      Period period = allocationLine.getPeriod();

      if (period == null) {
        continue;
      }

      LocalDate allocationLineFromDate = period.getFromDate();
      LocalDate allocationLineToDate = period.getToDate();
      LocalDate sprintFromDate = sprint.getFromDate();
      LocalDate sprintToDate = sprint.getToDate();

      if (allocationLineFromDate == null
          || allocationLineToDate == null
          || sprintFromDate == null
          || sprintToDate == null
          || employee == null) {
        continue;
      }

      BigDecimal sprintPeriod = BigDecimal.ZERO;
      if (sprintFromDate.isAfter(allocationLineFromDate)
          && sprintToDate.isBefore(allocationLineToDate)) {
        sprintPeriod = getWorkingDays(sprintFromDate, sprintToDate, employee);
      }

      if (!sprintFromDate.isAfter(allocationLineFromDate)
          && sprintToDate.isBefore(allocationLineToDate)) {
        sprintPeriod = getWorkingDays(allocationLineFromDate, sprintToDate, employee);
      }

      if (sprintFromDate.isAfter(allocationLineFromDate)
          && !sprintToDate.isBefore(allocationLineToDate)) {
        sprintPeriod = getWorkingDays(sprintFromDate, allocationLineToDate, employee);
      }

      BigDecimal allocationPeriod =
          getWorkingDays(allocationLineFromDate, allocationLineToDate, employee);
      BigDecimal allocation = allocationLine.getAllocated();
      allocatedTime = allocatedTime.add(getProrata(allocationPeriod, sprintPeriod, allocation));
    }
    return allocatedTime;
  }

  @Override
  public BigDecimal getBudgetedTime(Sprint sprint, Project project) throws AxelorException {
    if (sprint == null || ObjectUtils.isEmpty(sprint.getProjectTaskList())) {
      return BigDecimal.ZERO;
    }

    Unit unitHours = appBaseService.getUnitHours();
    Unit unitDays = appBaseService.getUnitDays();

    return sprint.getProjectTaskList().stream()
        .map(
            projectTask -> {
              BigDecimal budgetedTime = projectTask.getBudgetedTime();
              if (unitHours.equals(projectTask.getTimeUnit()) && budgetedTime.signum() != 0) {
                try {
                  return unitConversionForProjectService.convert(
                      unitHours, unitDays, budgetedTime, budgetedTime.scale(), project);
                } catch (AxelorException e) {
                  throw new RuntimeException(e.getMessage(), e);
                }
              }
              return budgetedTime;
            })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  protected BigDecimal getProrata(
      BigDecimal allocationPeriod, BigDecimal sprintPeriod, BigDecimal allocation) {
    if (allocationPeriod.signum() != 0) {
      return allocation
          .multiply(sprintPeriod)
          .divide(allocationPeriod, allocation.scale(), RoundingMode.HALF_UP);
    }
    return BigDecimal.ONE;
  }
}
