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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Adapter;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPlanningTimeCreateServiceImpl implements ProjectPlanningTimeCreateService {

  protected static final Logger LOG =
      LoggerFactory.getLogger(ProjectPlanningTimeCreateService.class);

  protected ProjectPlanningTimeRepository planningTimeRepo;
  protected ProjectRepository projectRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayHrService holidayService;
  protected ProductRepository productRepo;
  protected EmployeeRepository employeeRepo;
  protected TimesheetLineRepository timesheetLineRepository;
  protected AppBaseService appBaseService;
  protected ProjectTimeUnitService projectTimeUnitService;
  protected ProjectPlanningTimeToolService projectPlanningTimeToolService;

  @Inject
  public ProjectPlanningTimeCreateServiceImpl(
      ProjectPlanningTimeRepository planningTimeRepo,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskRepo,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService holidayService,
      ProductRepository productRepo,
      EmployeeRepository employeeRepo,
      TimesheetLineRepository timesheetLineRepository,
      AppBaseService appBaseService,
      ProjectTimeUnitService projectTimeUnitService,
      ProjectPlanningTimeToolService projectPlanningTimeToolService) {
    this.planningTimeRepo = planningTimeRepo;
    this.projectRepo = projectRepo;
    this.projectTaskRepo = projectTaskRepo;
    this.weeklyPlanningService = weeklyPlanningService;
    this.holidayService = holidayService;
    this.productRepo = productRepo;
    this.employeeRepo = employeeRepo;
    this.timesheetLineRepository = timesheetLineRepository;
    this.appBaseService = appBaseService;
    this.projectTimeUnitService = projectTimeUnitService;
    this.projectPlanningTimeToolService = projectPlanningTimeToolService;
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

    LocalDateTime fromDate =
        (LocalDateTime)
            Adapter.adapt(datas.get("fromDate"), LocalDateTime.class, LocalDateTime.class, null);
    LocalDateTime toDate =
        (LocalDateTime)
            Adapter.adapt(datas.get("toDate"), LocalDateTime.class, LocalDateTime.class, null);

    Project project = (Project) datas.get("project");
    project = projectRepo.find(project.getId());

    Integer timePercent = 0;
    if (datas.get("timepercent") != null) {
      timePercent = Integer.parseInt(datas.get("timepercent").toString());
    }

    Employee employee = (Employee) datas.get("employee");
    employee = employeeRepo.find(employee.getId());

    ProjectTask projectTask =
        Optional.ofNullable((ProjectTask) datas.get("projectTask"))
            .map(task -> projectTaskRepo.find(task.getId()))
            .orElse(null);

    Product activity =
        Optional.ofNullable((Product) datas.get("product"))
            .map(p -> productRepo.find(p.getId()))
            .orElse(null);

    Site site =
        Optional.ofNullable((Site) datas.get("site"))
            .map(s -> JPA.find(Site.class, s.getId()))
            .orElse(null);

    Unit timeUnit =
        Optional.ofNullable((Unit) datas.get("timeUnit"))
            .map(u -> JPA.find(Unit.class, u.getId()))
            .orElse(null);

    BigDecimal dailyWorkHrs = employee.getDailyWorkHours();

    while (fromDate.isBefore(toDate)) {

      LocalDate date = fromDate.toLocalDate();
      LocalDateTime taskEndDateTime =
          fromDate.withHour(toDate.getHour()).withMinute(toDate.getMinute());

      LOG.debug("Create Planning for the date: {}", date);

      double dayHrs = 0;
      if (employee.getWeeklyPlanning() != null) {
        dayHrs = weeklyPlanningService.getWorkingDayValueInDays(employee.getWeeklyPlanning(), date);
      }

      if (dayHrs > 0 && !holidayService.checkPublicHolidayDay(date, employee)) {

        ProjectPlanningTime planningTime =
            createProjectPlanningTime(
                fromDate,
                projectTask,
                project,
                timePercent,
                employee,
                activity,
                dailyWorkHrs,
                taskEndDateTime,
                site,
                timeUnit);
        planningTimeRepo.save(planningTime);
      }

      fromDate = fromDate.plusDays(1);
    }
  }

  @Override
  public ProjectPlanningTime createProjectPlanningTime(
      LocalDateTime fromDate,
      ProjectTask projectTask,
      Project project,
      Integer timePercent,
      Employee employee,
      Product activity,
      BigDecimal dailyWorkHrs,
      LocalDateTime taskEndDateTime,
      Site site,
      Unit defaultTimeUnit)
      throws AxelorException {
    ProjectPlanningTime planningTime = new ProjectPlanningTime();

    planningTime.setProjectTask(projectTask);
    planningTime.setProduct(activity);
    planningTime.setTimepercent(timePercent);
    planningTime.setEmployee(employee);
    planningTime.setStartDateTime(fromDate);
    planningTime.setEndDateTime(taskEndDateTime);
    planningTime.setProject(project);
    planningTime.setSite(site);
    planningTime.setTimeUnit(defaultTimeUnit);

    BigDecimal totalHours = BigDecimal.ZERO;
    if (timePercent > 0) {
      totalHours = dailyWorkHrs.multiply(new BigDecimal(timePercent)).divide(new BigDecimal(100));
    }

    if (defaultTimeUnit != null) {
      planningTime.setTimeUnit(defaultTimeUnit);
    } else {
      planningTime.setTimeUnit(projectPlanningTimeToolService.getDefaultTimeUnit(planningTime));
    }
    planningTime.setDisplayTimeUnit(planningTime.getTimeUnit());

    if (planningTime.getTimeUnit() == null) {
      return planningTime;
    }

    computePlannedTime(planningTime, totalHours);

    return planningTime;
  }

  protected void computePlannedTime(ProjectPlanningTime planningTime, BigDecimal totalHours)
      throws AxelorException {
    if (planningTime.getTimeUnit() == null) {
      return;
    }

    if (planningTime.getTimeUnit().equals(appBaseService.getUnitDays())) {
      BigDecimal numberHoursADay =
          projectTimeUnitService.getDefaultNumberHoursADay(planningTime.getProject());
      planningTime.setPlannedTime(totalHours.divide(numberHoursADay, 2, RoundingMode.HALF_UP));
    } else if (planningTime.getTimeUnit().equals(appBaseService.getUnitMinutes())) {
      planningTime.setPlannedTime(totalHours.multiply(new BigDecimal(60)));
    } else {
      planningTime.setPlannedTime(totalHours);
    }
  }
}
