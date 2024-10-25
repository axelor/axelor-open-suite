/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.sprint;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveDaysService;
import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.AllocationLineRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class AllocationLineServiceImpl implements AllocationLineService {

  protected AllocationLineRepository allocationLineRepo;
  protected LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService;
  protected LeaveRequestRepository leaveRequestRepo;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public AllocationLineServiceImpl(
      AllocationLineRepository allocationLineRepo,
      LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService,
      LeaveRequestRepository leaveRequestRepo,
      WeeklyPlanningService weeklyPlanningService) {

    this.allocationLineRepo = allocationLineRepo;
    this.leaveRequestComputeLeaveDaysService = leaveRequestComputeLeaveDaysService;
    this.leaveRequestRepo = leaveRequestRepo;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public AllocationLine createAllocationLineIfNotExists(
      Project project, AllocationPeriod period, User user) throws AxelorException {

    if (allocationLineRepo.findByProjectAndPeriodAndUser(project, period, user) == null) {
      AllocationLine allocationLine = new AllocationLine();

      allocationLine.setProject(project);
      allocationLine.setAllocationPeriod(period);
      allocationLine.setUser(user);

      BigDecimal leaves = getLeaves(period, user);
      BigDecimal alreadyAllocated = getAlreadyAllocated(project, period, user);
      BigDecimal availableAllocation =
          getAvailableAllocation(period, user, leaves, alreadyAllocated);

      allocationLine.setAllocated(BigDecimal.ZERO);
      allocationLine.setLeaves(leaves);
      allocationLine.setAlreadyAllocated(alreadyAllocated);
      allocationLine.setAvailableAllocation(availableAllocation);

      return allocationLineRepo.save(allocationLine);
    }

    return null;
  }

  @Override
  public BigDecimal getLeaves(AllocationPeriod period, User user) throws AxelorException {

    BigDecimal leaveDayCount = BigDecimal.ZERO;

    if (period != null && user != null && user.getEmployee() != null) {
      Employee employee = user.getEmployee();
      LocalDate toDate = period.getToDate();
      LocalDate fromDate = period.getFromDate();

      List<LeaveRequest> leaveRequestList =
          leaveRequestRepo
              .all()
              .filter(
                  "self.statusSelect = ?4 AND self.employee = ?3 AND ((self.fromDateT BETWEEN ?2 AND ?1 OR self.toDateT BETWEEN ?2 AND ?1) OR (?1 BETWEEN self.fromDateT AND self.toDateT OR ?2 BETWEEN self.fromDateT AND self.toDateT))",
                  toDate,
                  fromDate,
                  employee,
                  LeaveRequestRepository.STATUS_VALIDATED)
              .fetch();

      if (CollectionUtils.isNotEmpty(leaveRequestList)) {

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
  public BigDecimal getAlreadyAllocated(Project project, AllocationPeriod period, User user) {

    BigDecimal alreadyAllocated = BigDecimal.ZERO;

    if (project != null && period != null && user != null) {
      List<AllocationLine> allocationLineList =
          allocationLineRepo.findByPeriodAndUser(period, user).fetch();

      if (CollectionUtils.isNotEmpty(allocationLineList)) {
        alreadyAllocated =
            allocationLineList.stream()
                .filter(line -> !line.getProject().equals(project))
                .map(AllocationLine::getAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }

    return alreadyAllocated.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getAvailableAllocation(
      AllocationPeriod period, User user, BigDecimal leaves, BigDecimal alreadyAllocated) {

    BigDecimal availableAllocation = BigDecimal.ZERO;

    if (period != null && user != null && user.getEmployee() != null) {
      BigDecimal workingDays = getWorkingDays(period, user);
      availableAllocation = workingDays.subtract(leaves).subtract(alreadyAllocated);
    }

    return availableAllocation.setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal getWorkingDays(AllocationPeriod period, User user) {

    BigDecimal workingDays = BigDecimal.ZERO;

    if (period != null && user != null && user.getEmployee() != null) {
      Employee employee = user.getEmployee();

      for (LocalDate date = period.getFromDate();
          !date.isAfter(period.getToDate());
          date = date.plusDays(1)) {
        double workingDay =
            weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);
        workingDays = workingDays.add(BigDecimal.valueOf(workingDay));
      }
    }

    return workingDays.setScale(2, RoundingMode.HALF_UP);
  }
}
