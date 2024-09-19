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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class BusinessProjectSprintAllocationLineServiceImpl
    implements BusinessProjectSprintAllocationLineService {

  public LeaveRequestService leaveRequestService;
  public WeeklyPlanningService weeklyPlanningService;
  public SprintAllocationLineRepository sprintAllocationLineRepo;

  @Inject
  public BusinessProjectSprintAllocationLineServiceImpl(
      LeaveRequestService leaveRequestService,
      WeeklyPlanningService weeklyPlanningService,
      SprintAllocationLineRepository sprintAllocationLineRepo) {

    this.leaveRequestService = leaveRequestService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.sprintAllocationLineRepo = sprintAllocationLineRepo;
  }

  @Override
  public HashMap<String, BigDecimal> computeFields(SprintAllocationLine sprintAllocationLine) {

    Sprint sprint = sprintAllocationLine.getSprint();
    User user = sprintAllocationLine.getUser();

    HashMap<String, BigDecimal> valueMap = new HashMap<>();
    valueMap.put("plannedTime", BigDecimal.ZERO);
    valueMap.put("remainingTime", BigDecimal.ZERO);

    if (sprint != null && user != null && sprint.getSprintPeriod() != null) {
      SprintPeriod sprintPeriod = sprint.getSprintPeriod();
      Employee employee = user.getEmployee();

      BigDecimal leaveDayCount = BigDecimal.ZERO;
      double workingDayCount = 0;

      for (LocalDate date = sprintPeriod.getFromDate();
          !date.isAfter(sprintPeriod.getToDate());
          date = date.plusDays(1)) {
        workingDayCount +=
            weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);

        if (leaveRequestService.isLeaveDay(employee, date)) {
          leaveDayCount = leaveDayCount.add(BigDecimal.ONE);
        }
      }

      BigDecimal plannedTime =
          sprint.getProjectTaskList().stream()
              .flatMap(task -> task.getProjectPlanningTimeList().stream())
              .filter(planningTime -> planningTime.getEmployee().getUser().equals(user))
              .map(ProjectPlanningTime::getPlannedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal remainingTime =
          BigDecimal.valueOf(workingDayCount)
              .subtract(leaveDayCount)
              .subtract(sprintAllocationLine.getAllocated());

      valueMap.put("leaves", leaveDayCount);
      valueMap.put("plannedTime", plannedTime);
      valueMap.put("remainingTime", remainingTime);
    }

    return valueMap;
  }

  @Override
  @Transactional
  public void sprintOnChange(Project project, Sprint sprint) {

    Set<User> membersUserSet = project.getMembersUserSet();

    if (CollectionUtils.isNotEmpty(membersUserSet)) {
      List<SprintAllocationLine> sprintAllocationLineList =
          sprintAllocationLineRepo.all().filter("self.sprint = ?1", sprint).fetch();

      for (User member : membersUserSet) {
        SprintAllocationLine sprintAllocationLine =
            sprintAllocationLineList.stream()
                .filter(line -> line.getUser().equals(member))
                .findFirst()
                .orElse(null);

        if (sprintAllocationLine == null) {
          sprintAllocationLine = new SprintAllocationLine();
          sprintAllocationLine.setSprint(sprint);
          sprintAllocationLine.setUser(member);
        }

        sprintAllocationLineRepo.save(sprintAllocationLine);
      }
    }
  }
}
