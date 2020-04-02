/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPlanningServiceImpl implements ProjectPlanningService {

  private static final Logger log = LoggerFactory.getLogger(ProjectPlanningService.class);

  @Inject private PublicHolidayHrService holidayService;

  @Inject private WeeklyPlanningService weeklyPlanningService;

  @Inject private ProjectPlanningTimeRepository planningTimeRepo;

  @Inject private ProjectPlanningRepository planningRepo;

  @Inject private ProjectRepository projectRepo;

  @Inject private TeamTaskRepository teamTaskRepo;

  @Transactional
  public ProjectPlanning updatePlanningTime(ProjectPlanning planning) throws AxelorException {

    Integer timePercent = planning.getTimepercent();

    Employee employee = planning.getUser().getEmployee();

    LocalDateTime fromDate = planning.getFromDate();
    LocalDateTime toDate = planning.getToDate();

    if (employee != null && fromDate != null && toDate != null && planning.getId() != null) {

      BigDecimal dailyWorkHrs = employee.getDailyWorkHours();

      removeOldPlanningTime(planning);

      while (fromDate.isBefore(toDate)) {

        LocalDate date = fromDate.toLocalDate();

        log.debug("Create Planning for the date: {}", date);

        double dayHrs = weeklyPlanningService.workingDayValue(employee.getWeeklyPlanning(), date);

        if (dayHrs > 0 && !holidayService.checkPublicHolidayDay(date, employee)) {

          ProjectPlanningTime planningTime =
              planningTimeRepo
                  .all()
                  .filter("self.projectPlanning = ?1 and self.date = ?2", planning, date)
                  .fetchOne();
          if (planningTime == null) {
            planningTime = new ProjectPlanningTime();
            planningTime.setProjectPlanning(planning);
          }
          planningTime.setDate(date);
          BigDecimal totalHours = BigDecimal.ZERO;
          if (timePercent > 0) {
            totalHours =
                dailyWorkHrs.multiply(new BigDecimal(timePercent)).divide(new BigDecimal(100));
          }
          planningTime.setHours(totalHours);
          planningTimeRepo.save(planningTime);
        }

        fromDate = fromDate.plusDays(1);
      }
    }

    return planning;
  }

  @Transactional
  public void removeOldPlanningTime(ProjectPlanning planning) {

    if (planning.getId() == null) {
      return;
    }
    List<ProjectPlanningTime> planningTimes =
        planningTimeRepo
            .all()
            .filter(
                "self.date NOT BETWEEN self.projectPlanning.fromDate AND self.projectPlanning.toDate AND self.projectPlanning = ?1",
                planning)
            .fetch();

    for (ProjectPlanningTime planningTime : planningTimes) {
      planningTimeRepo.remove(planningTime);
    }
  }

  @Override
  @Transactional
  public void updateTaskPlannedHrs(TeamTask task) {

    if (task != null) {
      List<ProjectPlanning> plannings = planningRepo.all().filter("self.task = ?1", task).fetch();
      BigDecimal totalPlanned =
          plannings
              .stream()
              .map(ProjectPlanning::getTotalPlannedHrs)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      task.setTotalPlannedHrs(totalPlanned);
      teamTaskRepo.save(task);
    }
  }

  @Override
  @Transactional
  public void updateProjectPlannedHrs(Project project) {

    if (project != null) {
      List<ProjectPlanning> plannings =
          planningRepo.all().filter("self.project = ?1", project).fetch();
      BigDecimal totalPlanned =
          plannings
              .stream()
              .map(ProjectPlanning::getTotalPlannedHrs)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      project.setTotalPlannedHrs(totalPlanned);
      projectRepo.save(project);
    }
  }

  @Override
  public BigDecimal getTotalPlannedHrs(ProjectPlanning planning) {

    BigDecimal totalPlannedHrs = BigDecimal.ZERO;

    if (planning.getProjectPlanningTime() != null) {
      for (ProjectPlanningTime time : planning.getProjectPlanningTime()) {
        totalPlannedHrs = totalPlannedHrs.add(time.getHours());
      }
    }

    return totalPlannedHrs;
  }
}
