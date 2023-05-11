/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeServiceImpl;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectPlanningTimeBusinessProjectServiceImpl extends ProjectPlanningTimeServiceImpl {

  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ProjectPlanningTimeBusinessProjectServiceImpl(
      ProjectPlanningTimeRepository planningTimeRepo,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskRepo,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService holidayService,
      ProductRepository productRepo,
      EmployeeRepository employeeRepo,
      TimesheetLineRepository timesheetLineRepository,
      AppBusinessProjectService appBusinessProjectService) {
    super(
        planningTimeRepo,
        projectRepo,
        projectTaskRepo,
        weeklyPlanningService,
        holidayService,
        productRepo,
        employeeRepo,
        timesheetLineRepository);
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  protected ProjectPlanningTime createProjectPlaningTime(
      LocalDateTime fromDate,
      ProjectTask projectTask,
      Project project,
      Integer timePercent,
      Employee employee,
      Product activity,
      BigDecimal dailyWorkHrs,
      LocalDateTime taskEndDateTime) {
    ProjectPlanningTime planningTime =
        super.createProjectPlaningTime(
            fromDate,
            projectTask,
            project,
            timePercent,
            employee,
            activity,
            dailyWorkHrs,
            taskEndDateTime);
    if (projectTask != null) {
      planningTime.setTimeUnit(projectTask.getTimeUnit());
    } else {
      planningTime.setTimeUnit(appBusinessProjectService.getAppBusinessProject().getHoursUnit());
    }
    if (planningTime
        .getTimeUnit()
        .equals(appBusinessProjectService.getAppBusinessProject().getDaysUnit())) {
      planningTime.setPlannedTime(
          planningTime.getPlannedTime().divide(project.getNumberHoursADay()));
    }
    return planningTime;
  }
}
