package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.AllocationLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveDaysService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AllocationLineComputeServiceImpl implements AllocationLineComputeService {

  protected LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected AppBaseService appBaseService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected LeaveRequestRepository leaveRequestRepo;
  protected AllocationLineRepository allocationLineRepo;
  protected ProjectPlanningTimeRepository planningTimeRepo;

  @Inject
  public AllocationLineComputeServiceImpl(
      LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService,
      WeeklyPlanningService weeklyPlanningService,
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService,
      LeaveRequestRepository leaveRequestRepo,
      AllocationLineRepository allocationLineRepo,
      ProjectPlanningTimeRepository planningTimeRepo) {
    this.leaveRequestComputeLeaveDaysService = leaveRequestComputeLeaveDaysService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.leaveRequestRepo = leaveRequestRepo;
    this.allocationLineRepo = allocationLineRepo;
    this.planningTimeRepo = planningTimeRepo;
  }

  @Override
  public BigDecimal getLeaves(Period period, Employee employee) throws AxelorException {
    BigDecimal leaveDayCount = BigDecimal.ZERO;
    if (period != null && employee != null) {
      LocalDate toDate = period.getToDate();
      LocalDate fromDate = period.getFromDate();
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
    }
    return leaveDayCount.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getAlreadyAllocated(
      AllocationLine allocationLine, Period period, Employee employee) {
    BigDecimal alreadyAllocated = BigDecimal.ZERO;
    if (period != null) {
      List<AllocationLine> allocationLineList =
          allocationLineRepo.findByPeriodAndEmployee(period, employee).fetch();
      if (ObjectUtils.notEmpty(allocationLineList)) {
        alreadyAllocated =
            allocationLineList.stream()
                .filter(line -> !line.equals(allocationLine))
                .map(AllocationLine::getAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }
    return alreadyAllocated.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getAvailableAllocation(
      Period period, Employee employee, BigDecimal leaves, BigDecimal alreadyAllocated) {
    BigDecimal availableAllocation = BigDecimal.ZERO;
    if (period != null && employee != null) {
      BigDecimal workingDays = getWorkingDays(period.getFromDate(), period.getToDate(), employee);
      availableAllocation = workingDays.subtract(leaves).subtract(alreadyAllocated);
    }
    return availableAllocation.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal computePlannedTime(Period period, Employee employee, Project project)
      throws AxelorException {
    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    if (period == null || employee == null || project == null) {
      return totalPlannedTime;
    }

    List<ProjectPlanningTime> projectPlanningTimeList =
        planningTimeRepo
            .findByEmployeeProjectAndPeriod(
                employee, project, period.getFromDate(), period.getToDate())
            .fetch();

    if (ObjectUtils.notEmpty(projectPlanningTimeList)) {
      Unit dayUnit = appBaseService.getAppBase().getUnitDays();

      for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeList) {
        BigDecimal plannedTime =
            getPlannedTimeInTargetUnit(
                projectPlanningTime.getTimeUnit(),
                dayUnit,
                projectPlanningTime.getPlannedTime(),
                projectPlanningTime.getProject());

        BigDecimal prorata = computeProrata(projectPlanningTime, period, employee);

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
      ProjectPlanningTime projectPlanningTime, Period period, Employee employee) {
    if (period.getFromDate() == null || period.getToDate() == null) {
      return BigDecimal.ONE;
    }
    LocalDate startDate =
        Optional.of(projectPlanningTime)
            .map(ProjectPlanningTime::getStartDateTime)
            .map(LocalDateTime::toLocalDate)
            .orElse(period.getFromDate());
    LocalDate endDate =
        Optional.of(projectPlanningTime)
            .map(ProjectPlanningTime::getEndDateTime)
            .map(LocalDateTime::toLocalDate)
            .orElse(period.getToDate());
    LocalDate maxFromDate =
        period.getFromDate().isAfter(startDate) ? period.getFromDate() : startDate;
    LocalDate minToDate = period.getToDate().isBefore(endDate) ? period.getToDate() : endDate;
    BigDecimal jointDays = getWorkingDays(maxFromDate, minToDate, employee);
    BigDecimal totalDays = getWorkingDays(startDate, endDate, employee);
    BigDecimal prorata = BigDecimal.ONE;
    if (totalDays.signum() > 0) {
      prorata = jointDays.divide(totalDays, 2, RoundingMode.HALF_UP);
    }

    return prorata;
  }
}
