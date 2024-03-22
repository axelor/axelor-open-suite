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
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
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
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ProjectPlanningTimeBusinessProjectServiceImpl extends ProjectPlanningTimeServiceImpl
    implements ProjectPlanningTimeBusinessProjectService {

  protected AppBusinessProjectService appBusinessProjectService;
  protected ICalendarService iCalendarService;
  protected ICalendarEventRepository iCalendarEventRepository;

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
      AppBusinessProjectService appBusinessProjectService,
      ICalendarService iCalendarService,
      ICalendarEventRepository iCalendarEventRepository) {
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
    this.iCalendarService = iCalendarService;
    this.iCalendarEventRepository = iCalendarEventRepository;
  }

  @Override
  protected ProjectPlanningTime createProjectPlanningTime(
      LocalDateTime fromDate,
      ProjectTask projectTask,
      Project project,
      Integer timePercent,
      Employee employee,
      Product activity,
      BigDecimal dailyWorkHrs,
      LocalDateTime taskEndDateTime)
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
            taskEndDateTime);

    if (!appBusinessProjectService.isApp("business-project")) {
      return planningTime;
    }

    if (projectTask != null) {
      Unit timeUnit = projectTask.getTimeUnit();
      if (Objects.isNull(timeUnit)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_NO_UNIT_FOUND),
            projectTask.getName());
      }
      planningTime.setTimeUnit(timeUnit);

    } else {
      planningTime.setTimeUnit(appBusinessProjectService.getHoursUnit());
    }
    if (planningTime.getTimeUnit().equals(appBusinessProjectService.getDaysUnit())) {
      BigDecimal numberHoursADay = project.getNumberHoursADay();
      if (numberHoursADay.signum() <= 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BusinessProjectExceptionMessage.PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING));
      }
      planningTime.setPlannedTime(
          planningTime.getPlannedTime().divide(numberHoursADay, 2, RoundingMode.HALF_UP));
    }
    if (appBusinessProjectService.getAppBusinessProject().getEnableEventCreation()) {
      createICalendarEvent(planningTime);
    }

    return planningTime;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addSingleProjectPlanningTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    if (appBusinessProjectService.getAppBusinessProject().getEnableEventCreation()) {
      createICalendarEvent(projectPlanningTime);
    }
    planningTimeRepo.save(projectPlanningTime);
  }

  @Override
  @Transactional
  public void removeProjectPlanningLine(ProjectPlanningTime projectPlanningTime) {
    if (!JPA.em().contains(projectPlanningTime)) {
      projectPlanningTime = planningTimeRepo.find(projectPlanningTime.getId());
    }

    if (projectPlanningTime.getIcalendarEvent() != null) {
      ICalendarEvent event = projectPlanningTime.getIcalendarEvent();
      projectPlanningTime.setIcalendarEvent(null);
      iCalendarEventRepository.remove(iCalendarEventRepository.find(event.getId()));
    }

    planningTimeRepo.remove(projectPlanningTime);
  }

  protected void createICalendarEvent(ProjectPlanningTime planningTime) {
    String subject = computeSubjectFromGroovy(planningTime).toString();
    ICalendarEvent event =
        iCalendarService.createEvent(
            planningTime.getStartDateTime(),
            planningTime.getEndDateTime(),
            planningTime.getEmployee().getUser(),
            planningTime.getDescription(),
            0,
            subject);
    iCalendarEventRepository.save(event);
    planningTime.setIcalendarEvent(event);
  }

  @Override
  @Transactional
  public void updateProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String description) {
    if (startDateTime != null) {
      projectPlanningTime.setStartDateTime(startDateTime);
    }
    if (endDateTime != null) {
      projectPlanningTime.setEndDateTime(endDateTime);
    }
    if (description != null) {
      projectPlanningTime.setDescription(description);
    }
    planningTimeRepo.save(projectPlanningTime);
  }

  @Override
  @Transactional
  public void updateLinkedEvent(ProjectPlanningTime projectPlanningTime) {
    ICalendarEvent icalendarEvent = projectPlanningTime.getIcalendarEvent();
    if (appBusinessProjectService.getAppBusinessProject().getEnableEventCreation()
        && icalendarEvent != null) {

      icalendarEvent.setStartDateTime(projectPlanningTime.getStartDateTime());
      icalendarEvent.setEndDateTime(projectPlanningTime.getEndDateTime());
      icalendarEvent.setDescription(projectPlanningTime.getDescription());
      User user = projectPlanningTime.getEmployee().getUser();
      if (user != null) {
        icalendarEvent.setUser(user);
        icalendarEvent.setCalendar(user.getiCalendar());
      }

      iCalendarEventRepository.save(icalendarEvent);
    }
  }

  @Override
  @Transactional
  public void deleteLinkedProjectPlanningTime(List<Long> ids) {
    List<ProjectPlanningTime> projectPlanningTimeList =
        planningTimeRepo
            .all()
            .filter("self.icalendarEvent.id IN :icalendarEvent")
            .bind("icalendarEvent", ids)
            .fetch();

    if (projectPlanningTimeList.isEmpty()) {
      return;
    }
    for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeList) {
      projectPlanningTime.setIcalendarEvent(null);
      planningTimeRepo.remove(projectPlanningTime);
    }
  }

  protected Object computeSubjectFromGroovy(ProjectPlanningTime projectPlanningTime) {

    Context scriptContext =
        new Context(Mapper.toMap(projectPlanningTime), projectPlanningTime.getClass());
    GroovyScriptHelper groovyScriptHelper = new GroovyScriptHelper(scriptContext);

    String subjectGroovyFormula =
        appBusinessProjectService.getAppBusinessProject().getEventSubjectGroovyFormula();
    if (StringUtils.isBlank(subjectGroovyFormula)) {
      subjectGroovyFormula = "project.fullName" + "-" + "projectTask.fullName";
    }
    return groovyScriptHelper.eval(subjectGroovyFormula);
  }

  @Override
  public ProjectPlanningTime loadLinkedPlanningTime(ICalendarEvent event) {
    return planningTimeRepo
        .all()
        .filter("self.icalendarEvent = :icalendarEvent")
        .bind("icalendarEvent", event)
        .fetchOne();
  }
}
