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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeCreateServiceImpl;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class ProjectPlanningTimeCreateBusinessProjectServiceImpl
    extends ProjectPlanningTimeCreateServiceImpl {

  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ProjectPlanningTimeCreateBusinessProjectServiceImpl(
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
      AppBusinessProjectService appBusinessProjectService) {
    super(
        planningTimeRepo,
        projectRepo,
        projectTaskRepo,
        weeklyPlanningService,
        holidayService,
        productRepo,
        employeeRepo,
        timesheetLineRepository,
        appBaseService,
        projectTimeUnitService);
    this.appBusinessProjectService = appBusinessProjectService;
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
    ProjectPlanningTime planningTime =
        super.createProjectPlanningTime(
            fromDate,
            projectTask,
            project,
            timePercent,
            employee,
            activity,
            dailyWorkHrs,
            taskEndDateTime,
            site,
            defaultTimeUnit);

    if (!appBusinessProjectService.isApp("business-project") || !project.getIsBusinessProject()) {
      return planningTime;
    }

    if (projectTask != null && projectTask.getTimeUnit() != null) {
      planningTime.setTimeUnit(projectTask.getTimeUnit());
    } else if (project.getProjectTimeUnit() != null) {
      planningTime.setTimeUnit(project.getProjectTimeUnit());
    } else {
      planningTime.setTimeUnit(appBaseService.getUnitHours());
    }

    if (planningTime.getTimeUnit().equals(appBaseService.getUnitDays())) {
      BigDecimal numberHoursADay = projectTimeUnitService.getDefaultNumberHoursADay(project);
      planningTime.setPlannedTime(
          planningTime.getPlannedTime().divide(numberHoursADay, 2, RoundingMode.HALF_UP));
    }

    return planningTime;
  }
}
