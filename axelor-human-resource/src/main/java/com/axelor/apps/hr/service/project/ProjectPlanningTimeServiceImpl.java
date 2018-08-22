/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPlanningTimeServiceImpl implements ProjectPlanningTimeService {

  private static final Logger log = LoggerFactory.getLogger(ProjectPlanningTimeService.class);

  @Inject private ProjectPlanningTimeRepository planningTimeRepo;

  @Inject private ProjectRepository projectRepo;

  @Inject private TeamTaskRepository teamTaskRepo;

  @Inject private WeeklyPlanningService weeklyPlanningService;

  @Inject private PublicHolidayHrService holidayService;

  @Inject private ProductRepository productRepo;

  @Inject private UserRepository userRepo;

  @Override
  @Transactional
  public void updateTaskPlannedHrs(TeamTask task) {

    if (task != null) {
      List<ProjectPlanningTime> plannings =
          planningTimeRepo.all().filter("self.task = ?1", task).fetch();
      BigDecimal totalPlanned =
          plannings.stream().map(p -> p.getPlannedHours()).reduce(BigDecimal.ZERO, BigDecimal::add);
      task.setTotalPlannedHrs(totalPlanned);
      task = teamTaskRepo.save(task);
    }
  }

  @Override
  @Transactional
  public void updateProjectPlannedHrs(Project project) {

    if (project != null) {
      List<ProjectPlanningTime> plannings =
          planningTimeRepo.all().filter("self.project = ?1", project).fetch();
      BigDecimal totalPlanned =
          plannings.stream().map(p -> p.getPlannedHours()).reduce(BigDecimal.ZERO, BigDecimal::add);
      project.setTotalPlannedHrs(totalPlanned);
      project = projectRepo.save(project);
    }
  }

  @Override
  @Transactional
  public void addLines(Map<String, Object> datas) throws AxelorException {

    List<ProjectPlanningTime> planningTimeList = new ArrayList<>();
    BigDecimal totalPlannedHours = BigDecimal.ZERO;

    Employee employee = null;
    TeamTask teamTask = null;
    Product activity = null;
    User user = null;

    if (datas.get("user") != null) {
      user = userRepo.find(Long.valueOf(datas.get("user").toString()));
      employee = user.getEmployee();
    }

    LocalDateTime fromDate = (LocalDateTime) datas.get("fromDate");
    LocalDateTime toDate = (LocalDateTime) datas.get("toDate");

    if (datas.get("teamTask") != null) {
      teamTask = teamTaskRepo.find(Long.valueOf(datas.get("teamTask").toString()));
    }

    if (datas.get("activity") != null) {
      activity = productRepo.find(Long.valueOf(datas.get("activity").toString()));
    }

    Integer timePercent = (Integer) datas.get("timePercent");
    Object modelObject = datas.get("modelObject");
    String model = datas.get("model").toString();

    if (employee != null && fromDate != null && toDate != null) {

      BigDecimal dailyWorkHrs = employee.getDailyWorkHours();

      while (fromDate.isBefore(toDate)) {

        LocalDate date = fromDate.toLocalDate();

        log.debug("Create Planning for the date: {}", date);

        double dayHrs = 0;
        if (employee.getWeeklyPlanning() != null) {
          dayHrs = weeklyPlanningService.workingDayValue(employee.getWeeklyPlanning(), date);
        }

        if (dayHrs > 0 && !holidayService.checkPublicHolidayDay(date, employee)) {

          ProjectPlanningTime planningTime = new ProjectPlanningTime();
          planningTime.setTask(teamTask);
          planningTime.setProduct(activity);
          planningTime.setTimepercent(timePercent);
          planningTime.setUser(user);
          planningTime.setDate(date);

          BigDecimal totalHours = BigDecimal.ZERO;
          if (timePercent > 0) {
            totalHours =
                dailyWorkHrs.multiply(new BigDecimal(timePercent)).divide(new BigDecimal(100));
          }
          totalPlannedHours = totalPlannedHours.add(totalHours);
          planningTime.setPlannedHours(totalHours);
          planningTimeList.add(planningTime);
        }

        fromDate = fromDate.plusDays(1);
      }

      Map<String, Object> obj = (Map<String, Object>) modelObject;
      Long objId = Long.valueOf(obj.get("id").toString());

      if (model.equals("com.axelor.apps.project.db.Project")) {
        Project project = projectRepo.find(objId);
        project.setTotalPlannedHrs(project.getTotalPlannedHrs().add(totalPlannedHours));
        planningTimeList.forEach(it -> project.addProjectPlanningTimeListItem(it));
      }

      if (model.equals("com.axelor.team.db.TeamTask")) {
        TeamTask task = teamTaskRepo.find(objId);
        task.setTotalPlannedHrs(task.getTotalPlannedHrs().add(totalPlannedHours));
        planningTimeList.forEach(
            it -> {
              it.setProject(task.getProject());
              task.addProjectPlanningTimeListItem(it);
            });
      }
    }
  }
}
