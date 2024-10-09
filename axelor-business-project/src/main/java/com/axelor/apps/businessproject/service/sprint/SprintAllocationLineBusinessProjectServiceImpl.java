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
package com.axelor.apps.businessproject.service.sprint;

import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.apps.project.service.sprint.SprintAllocationLineServiceImpl;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SprintAllocationLineBusinessProjectServiceImpl
    extends SprintAllocationLineServiceImpl {

  protected LeaveRequestService leaveRequestService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public SprintAllocationLineBusinessProjectServiceImpl(
      SprintAllocationLineRepository sprintAllocationLineRepo,
      LeaveRequestService leaveRequestService,
      WeeklyPlanningService weeklyPlanningService) {

    super(sprintAllocationLineRepo);

    this.leaveRequestService = leaveRequestService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  public HashMap<String, BigDecimal> computeSprintAllocationLine(
      SprintAllocationLine sprintAllocationLine) {

    Sprint sprint = sprintAllocationLine.getSprint();
    User user = sprintAllocationLine.getUser();

    HashMap<String, BigDecimal> valueMap = new HashMap<>();

    BigDecimal leaveDayCount = BigDecimal.ZERO;
    BigDecimal plannedTime = BigDecimal.ZERO;
    BigDecimal remainingTime = BigDecimal.ZERO;

    if (sprint != null && user != null && sprint.getSprintPeriod() != null) {
      SprintPeriod sprintPeriod = sprint.getSprintPeriod();
      Employee employee = user.getEmployee();
      double workingDay = 0;
      double workingDayCount = 0;

      for (LocalDate date = sprintPeriod.getFromDate();
          !date.isAfter(sprintPeriod.getToDate());
          date = date.plusDays(1)) {

        if (employee != null) {
          workingDay =
              weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);
          workingDayCount += workingDay;

          if (leaveRequestService.isLeaveDay(employee, date)) {
            leaveDayCount = leaveDayCount.add(new BigDecimal(workingDay));
          }
        }
      }

      List<ProjectTask> projectTaskList = sprint.getProjectTaskList();

      if (CollectionUtils.isNotEmpty(projectTaskList)) {
        plannedTime =
            projectTaskList.stream()
                .flatMap(task -> task.getProjectPlanningTimeList().stream())
                .filter(
                    planningTime ->
                        planningTime.getEmployee().getUser() != null
                            && planningTime.getEmployee().getUser().equals(user))
                .map(ProjectPlanningTime::getPlannedTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }

      remainingTime =
          BigDecimal.valueOf(workingDayCount)
              .subtract(leaveDayCount)
              .subtract(sprintAllocationLine.getAllocated());
    }

    valueMap.put("leaves", leaveDayCount);
    valueMap.put("plannedTime", plannedTime);
    valueMap.put("remainingTime", remainingTime);

    return valueMap;
  }
}
