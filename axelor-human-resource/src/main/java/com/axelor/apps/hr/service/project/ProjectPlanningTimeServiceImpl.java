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
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Adapter;
import com.axelor.db.mapper.Mapper;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected UnitConversionRepository unitConversionRepository;

  protected AppProjectService appProjectService;
  protected ProjectConfigService projectConfigService;
  protected ICalendarService iCalendarService;
  protected ICalendarEventRepository iCalendarEventRepository;

  @Inject
  public ProjectPlanningTimeServiceImpl(
      ProjectPlanningTimeRepository planningTimeRepo,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskRepo,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService holidayService,
      ProductRepository productRepo,
      EmployeeRepository employeeRepo,
      TimesheetLineRepository timesheetLineRepository,
      AppProjectService appProjectService,
      ProjectConfigService projectConfigService,
      ICalendarService iCalendarService,
      ICalendarEventRepository iCalendarEventRepository,
      UnitConversionForProjectService unitConversionForProjectService,
      UnitConversionRepository unitConversionRepository) {
    super();
    this.planningTimeRepo = planningTimeRepo;
    this.projectRepo = projectRepo;
    this.projectTaskRepo = projectTaskRepo;
    this.weeklyPlanningService = weeklyPlanningService;
    this.holidayService = holidayService;
    this.productRepo = productRepo;
    this.employeeRepo = employeeRepo;
    this.timesheetLineRepository = timesheetLineRepository;
    this.appProjectService = appProjectService;
    this.projectConfigService = projectConfigService;
    this.iCalendarService = iCalendarService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.unitConversionRepository = unitConversionRepository;
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

    Site site = null;
    if (datas.get("site") != null) {
      objMap = (Map) datas.get("site");
      site = JPA.find(Site.class, Long.valueOf(objMap.get("id").toString()));
    }

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
                site);
        planningTimeRepo.save(planningTime);
      }

      fromDate = fromDate.plusDays(1);
    }
  }

  protected ProjectPlanningTime createProjectPlanningTime(
      LocalDateTime fromDate,
      ProjectTask projectTask,
      Project project,
      Integer timePercent,
      Employee employee,
      Product activity,
      BigDecimal dailyWorkHrs,
      LocalDateTime taskEndDateTime,
      Site site)
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

    BigDecimal totalHours = BigDecimal.ZERO;
    if (timePercent > 0) {
      totalHours = dailyWorkHrs.multiply(new BigDecimal(timePercent)).divide(new BigDecimal(100));
    }
    planningTime.setPlannedTime(totalHours);
    return planningTime;
  }

  @Override
  @Transactional
  public void removeProjectPlanningLines(List<Integer> projectPlanningLineIds) {
    for (Integer id : projectPlanningLineIds) {
      removeProjectPlanningLine(planningTimeRepo.find(Long.valueOf(id)));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addSingleProjectPlanningTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    if (appProjectService.getAppProject().getEnableEventCreation()) {
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
    if (appProjectService.getAppProject().getEnableEventCreation() && icalendarEvent != null) {

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

    String subjectGroovyFormula = appProjectService.getAppProject().getEventSubjectGroovyFormula();
    if (StringUtils.isBlank(subjectGroovyFormula)) {
      subjectGroovyFormula = "project.fullName +" + "\"-\"" + "+ projectTask.fullName";
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

  @Override
  public BigDecimal computePlannedTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    if (projectConfigService
        .getProjectConfig(projectPlanningTime.getProject().getCompany())
        .getIsSelectionOnDisplayPlannedTime()) {
      return computePlannedTimeFromDisplayRestricted(projectPlanningTime);
    }
    return computePlannedTimeFromDisplay(projectPlanningTime);
  }

  protected BigDecimal computePlannedTimeFromDisplay(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    if (projectPlanningTime.getDisplayTimeUnit() == null
        || projectPlanningTime.getTimeUnit() == null
        || projectPlanningTime.getDisplayPlannedTime() == null) {
      return BigDecimal.ZERO;
    }
    return unitConversionForProjectService.convert(
        projectPlanningTime.getDisplayTimeUnit(),
        projectPlanningTime.getTimeUnit(),
        projectPlanningTime.getDisplayPlannedTime(),
        projectPlanningTime.getDisplayPlannedTime().scale(),
        projectPlanningTime.getProject());
  }

  protected BigDecimal computePlannedTimeFromDisplayRestricted(
      ProjectPlanningTime projectPlanningTime) throws AxelorException {
    if (projectPlanningTime.getDisplayTimeUnit() == null
        || projectPlanningTime.getTimeUnit() == null
        || projectPlanningTime.getDisplayPlannedTimeRestricted() == null
        || projectPlanningTime.getDisplayPlannedTimeRestricted().getPlannedTime() == null) {
      return BigDecimal.ZERO;
    }
    return unitConversionForProjectService.convert(
        projectPlanningTime.getDisplayTimeUnit(),
        projectPlanningTime.getTimeUnit(),
        projectPlanningTime.getDisplayPlannedTimeRestricted().getPlannedTime(),
        projectPlanningTime.getDisplayPlannedTimeRestricted().getPlannedTime().scale(),
        projectPlanningTime.getProject());
  }

  @Override
  public String computeDisplayTimeUnitDomain(ProjectPlanningTime projectPlanningTime) {
    return "self.id IN ("
        + StringHelper.getIdListString(
            computeAvailableDisplayTimeUnits(projectPlanningTime.getTimeUnit()))
        + ")";
  }

  @Override
  public List<Long> computeAvailableDisplayTimeUnitIds(Unit unit) {
    return computeAvailableDisplayTimeUnits(unit).stream()
        .map(Unit::getId)
        .collect(Collectors.toList());
  }

  protected List<Unit> computeAvailableDisplayTimeUnits(Unit unit) {
    List<Unit> units = new ArrayList<>();
    units.add(unit);
    units.addAll(
        unitConversionRepository.all()
            .filter("self.entitySelect = :entitySelect AND self.startUnit = :startUnit")
            .bind("entitySelect", UnitConversionRepository.ENTITY_PROJECT).bind("startUnit", unit)
            .fetch().stream()
            .map(UnitConversion::getEndUnit)
            .collect(Collectors.toList()));
    units.addAll(
        unitConversionRepository.all()
            .filter("self.entitySelect = :entitySelect AND self.endUnit = :endUnit")
            .bind("entitySelect", UnitConversionRepository.ENTITY_PROJECT).bind("endUnit", unit)
            .fetch().stream()
            .map(UnitConversion::getStartUnit)
            .collect(Collectors.toList()));
    return units;
  }

  @Override
  public String computeDisplayPlannedTimeRestrictedDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    return "self.id IN ("
        + StringHelper.getIdListString(
            projectConfigService
                .getProjectConfig(projectPlanningTime.getProject().getCompany())
                .getPlannedTimeValueList())
        + ")";
  }
}
