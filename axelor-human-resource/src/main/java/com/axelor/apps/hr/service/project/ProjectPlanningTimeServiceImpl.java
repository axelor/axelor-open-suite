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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPlanningTimeServiceImpl implements ProjectPlanningTimeService {

  protected static final Logger LOG = LoggerFactory.getLogger(ProjectPlanningTimeService.class);

  protected ProjectPlanningTimeRepository planningTimeRepo;
  protected ProjectRepository projectRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayHrService holidayService;
  protected ProductRepository productRepo;
  protected EmployeeRepository employeeRepo;
  protected TimesheetLineRepository timesheetLineRepository;

  @Inject
  public ProjectPlanningTimeServiceImpl(
      ProjectPlanningTimeRepository planningTimeRepo,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskRepo,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService holidayService,
      ProductRepository productRepo,
      EmployeeRepository employeeRepo,
      TimesheetLineRepository timesheetLineRepository) {
    super();
    this.planningTimeRepo = planningTimeRepo;
    this.projectRepo = projectRepo;
    this.projectTaskRepo = projectTaskRepo;
    this.weeklyPlanningService = weeklyPlanningService;
    this.holidayService = holidayService;
    this.productRepo = productRepo;
    this.employeeRepo = employeeRepo;
    this.timesheetLineRepository = timesheetLineRepository;
  }

  @Override
  public BigDecimal getTaskPlannedHrs(ProjectTask task) {

    BigDecimal totalPlanned = BigDecimal.ZERO;
    if (task != null) {
      List<ProjectPlanningTime> plannings =
          planningTimeRepo.all().filter("self.projectTask = ?1", task).fetch();
      if (plannings != null) {
        totalPlanned =
            plannings.stream()
                .map(ProjectPlanningTime::getPlannedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }

    return totalPlanned;
  }

  @Override
  public BigDecimal getProjectPlannedHrs(Project project) {

    BigDecimal totalPlanned = BigDecimal.ZERO;
    if (project != null) {
      List<ProjectPlanningTime> plannings =
          planningTimeRepo
              .all()
              .filter(
                  "self.project = ?1 OR (self.project.parentProject = ?1 AND self.project.parentProject.isShowPhasesElements = ?2)",
                  project,
                  true)
              .fetch();
      if (plannings != null) {
        totalPlanned =
            plannings.stream()
                .map(ProjectPlanningTime::getPlannedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }

    return totalPlanned;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addMultipleProjectPlanningTime(Map<String, Object> datas) throws AxelorException {

    if (datas.get("project") == null
        || datas.get("employee") == null
        || datas.get("fromDate") == null
        || datas.get("toDate") == null) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    LocalDateTime fromDate = LocalDateTime.parse(datas.get("fromDate").toString(), formatter);
    LocalDateTime toDate = LocalDateTime.parse(datas.get("toDate").toString(), formatter);

    ProjectTask projectTask = null;

    Map<String, Object> objMap = (Map) datas.get("project");
    Project project = projectRepo.find(Long.parseLong(objMap.get("id").toString()));
    Integer timePercent = 0;

    if (datas.get("timepercent") != null) {
      timePercent = Integer.parseInt(datas.get("timepercent").toString());
    }

    objMap = (Map) datas.get("employee");
    Employee employee = employeeRepo.find(Long.parseLong(objMap.get("id").toString()));

    if (employee == null) {
      return;
    }

    if (datas.get("projectTask") != null) {
      objMap = (Map) datas.get("projectTask");
      projectTask = projectTaskRepo.find(Long.valueOf(objMap.get("id").toString()));
    }

    Product activity = null;
    if (datas.get("product") != null) {
      objMap = (Map) datas.get("product");
      activity = productRepo.find(Long.valueOf(objMap.get("id").toString()));
    }

    BigDecimal dailyWorkHrs = employee.getDailyWorkHours();

    while (fromDate.isBefore(toDate)) {

      LocalDate date = fromDate.toLocalDate();

      LOG.debug("Create Planning for the date: {}", date);

      double dayHrs = 0;
      if (employee.getWeeklyPlanning() != null) {
        dayHrs = weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);
      }

      if (dayHrs > 0 && !holidayService.checkPublicHolidayDay(date, employee)) {

        ProjectPlanningTime planningTime = new ProjectPlanningTime();

        planningTime.setProjectTask(projectTask);
        planningTime.setProduct(activity);
        planningTime.setTimepercent(timePercent);
        planningTime.setEmployee(employee);
        planningTime.setDate(date);
        planningTime.setProject(project);
        planningTime.setIsIncludeInTurnoverForecast(
            (Boolean) datas.get("isIncludeInTurnoverForecast"));

        BigDecimal totalHours = BigDecimal.ZERO;
        if (timePercent > 0) {
          totalHours =
              dailyWorkHrs.multiply(new BigDecimal(timePercent)).divide(new BigDecimal(100));
        }
        planningTime.setPlannedHours(totalHours);
        planningTimeRepo.save(planningTime);
      }

      fromDate = fromDate.plusDays(1);
    }
  }

  @Override
  @Transactional
  public void removeProjectPlanningLines(List<Map<String, Object>> projectPlanningLines) {

    for (Map<String, Object> line : projectPlanningLines) {
      ProjectPlanningTime projectPlanningTime =
          planningTimeRepo.find(Long.parseLong(line.get("id").toString()));
      planningTimeRepo.remove(projectPlanningTime);
    }
  }

  @Override
  public BigDecimal getDurationForCustomer(ProjectTask projectTask) {
    String query =
        "SELECT SUM(self.durationForCustomer) FROM TimesheetLine AS self WHERE self.timesheet.statusSelect = :statusSelect AND self.projectTask = :projectTask";
    BigDecimal durationForCustomer =
        JPA.em()
            .createQuery(query, BigDecimal.class)
            .setParameter("statusSelect", TimesheetRepository.STATUS_VALIDATED)
            .setParameter("projectTask", projectTask)
            .getSingleResult();
    return durationForCustomer != null ? durationForCustomer : BigDecimal.ZERO;
  }
}
