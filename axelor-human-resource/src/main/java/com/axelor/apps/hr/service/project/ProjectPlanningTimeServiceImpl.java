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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectConfig;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProjectPlanningTimeServiceImpl implements ProjectPlanningTimeService {

  protected ProjectPlanningTimeRepository planningTimeRepo;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected UnitConversionRepository unitConversionRepository;
  protected AppProjectService appProjectService;
  protected ProjectConfigService projectConfigService;
  protected PlannedTimeValueService plannedTimeValueService;
  protected ICalendarService iCalendarService;
  protected ICalendarEventRepository iCalendarEventRepository;
  protected ProjectTimeUnitService projectTimeUnitService;
  protected ProjectTaskRepository projectTaskRepo;

  @Inject
  public ProjectPlanningTimeServiceImpl(
      ProjectPlanningTimeRepository planningTimeRepo,
      AppProjectService appProjectService,
      ProjectConfigService projectConfigService,
      PlannedTimeValueService plannedTimeValueService,
      ICalendarService iCalendarService,
      ICalendarEventRepository iCalendarEventRepository,
      UnitConversionForProjectService unitConversionForProjectService,
      UnitConversionRepository unitConversionRepository,
      ProjectTimeUnitService projectTimeUnitService,
      ProjectTaskRepository projectTaskRepo) {
    this.planningTimeRepo = planningTimeRepo;
    this.appProjectService = appProjectService;
    this.projectConfigService = projectConfigService;
    this.plannedTimeValueService = plannedTimeValueService;
    this.iCalendarService = iCalendarService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.unitConversionRepository = unitConversionRepository;
    this.projectTimeUnitService = projectTimeUnitService;
    this.projectTaskRepo = projectTaskRepo;
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
                .map(ProjectPlanningTime::getPlannedTime)
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
                .map(ProjectPlanningTime::getPlannedTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
      }
    }

    return totalPlanned;
  }

  @Override
  @Transactional
  public void removeProjectPlanningLines(List<Integer> projectPlanningLineIds) {
    for (Integer id : projectPlanningLineIds) {
      removeProjectPlanningLine(planningTimeRepo.find(Long.valueOf(id)));
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
    ProjectConfig projectConfig = getProjectConfig(projectPlanningTime);
    if (projectConfig != null && projectConfig.getIsSelectionOnDisplayPlannedTime()) {
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
    Optional<BigDecimal> plannedTime =
        Optional.of(projectPlanningTime)
            .map(ProjectPlanningTime::getDisplayPlannedTimeRestricted)
            .map(PlannedTimeValue::getPlannedTime);
    if (projectPlanningTime.getDisplayTimeUnit() == null
        || projectPlanningTime.getTimeUnit() == null
        || plannedTime.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return unitConversionForProjectService.convert(
        projectPlanningTime.getDisplayTimeUnit(),
        projectPlanningTime.getTimeUnit(),
        plannedTime.get(),
        plannedTime.get().scale(),
        projectPlanningTime.getProject());
  }

  @Override
  public String computeDisplayTimeUnitDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    Unit unit = projectPlanningTime.getTimeUnit();
    if (unit == null) {
      if (projectPlanningTime.getProjectTask() != null) {
        unit =
            projectTimeUnitService.getTaskDefaultHoursTimeUnit(
                projectPlanningTime.getProjectTask());
      } else if (projectPlanningTime.getProject() != null) {
        unit =
            projectTimeUnitService.getProjectDefaultHoursTimeUnit(projectPlanningTime.getProject());
      }
    }

    return "self.id IN ("
        + StringHelper.getIdListString(computeAvailableDisplayTimeUnits(unit))
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
    if (unit == null) {
      return units;
    }
    units.add(unit);
    units.addAll(
        unitConversionRepository
            .all()
            .filter("self.entitySelect = :entitySelect AND self.startUnit = :startUnit")
            .bind("entitySelect", UnitConversionRepository.ENTITY_PROJECT)
            .bind("startUnit", unit)
            .fetch()
            .stream()
            .map(UnitConversion::getEndUnit)
            .collect(Collectors.toList()));
    units.addAll(
        unitConversionRepository
            .all()
            .filter("self.entitySelect = :entitySelect AND self.endUnit = :endUnit")
            .bind("entitySelect", UnitConversionRepository.ENTITY_PROJECT)
            .bind("endUnit", unit)
            .fetch()
            .stream()
            .map(UnitConversion::getStartUnit)
            .collect(Collectors.toList()));
    return units;
  }

  @Override
  public String computeDisplayPlannedTimeRestrictedDomain(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    ProjectConfig projectConfig = getProjectConfig(projectPlanningTime);
    String idListStr = "";
    if (projectConfig != null) {
      idListStr = StringHelper.getIdListString(projectConfig.getPlannedTimeValueList());
    }
    if (StringUtils.isEmpty(idListStr)) {
      idListStr = "0";
    }

    return String.format("self.id IN (%s)", idListStr);
  }

  public BigDecimal getDefaultPlanningTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    ProjectConfig projectConfig = getProjectConfig(projectPlanningTime);
    if (projectConfig != null) {
      projectConfig.getValueByDefaultOnDisplayPlannedTime();
    }
    return BigDecimal.ZERO;
  }

  public PlannedTimeValue getDefaultPlanningRestrictedTime(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    ProjectConfig projectConfig = getProjectConfig(projectPlanningTime);
    if (projectConfig != null) {
      return plannedTimeValueService.createPlannedTimeValue(
          projectConfig.getValueByDefaultOnDisplayPlannedTime());
    }
    return null;
  }

  protected ProjectConfig getProjectConfig(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    Optional<Company> optCompany =
        Optional.ofNullable(projectPlanningTime)
            .map(ProjectPlanningTime::getProject)
            .map(Project::getCompany);
    if (optCompany.isPresent()) {
      return projectConfigService.getProjectConfig(optCompany.get());
    }
    return null;
  }

  @Override
  public List<ProjectPlanningTime> getProjectPlanningTimeIdList(
      Employee employee, LocalDate fromDate, LocalDate toDate) {

    return planningTimeRepo
        .all()
        .filter(
            "self.project IS NOT NULL and self.project.manageTimeSpent is true "
                + "and self.employee = :employee "
                + "and ((self.startDateTime <= :fromDate and self.endDateTime >= :toDate) or self.startDateTime between :fromDate and :toDate or (self.endDateTime between :fromDate and :toDate))")
        .bind("employee", employee)
        .bind("fromDate", Optional.ofNullable(fromDate).map(LocalDate::atStartOfDay).orElse(null))
        .bind("toDate", Optional.ofNullable(toDate).map(date -> date.atTime(23, 59)).orElse(null))
        .fetch()
        .stream()
        .filter(
            distinctByTask(
                (it ->
                    new ProjectPlanningTimeObj(it.getProject(), it.getProjectTask()).toString())))
        .collect(Collectors.toList());
  }

  public static <T> Predicate<T> distinctByTask(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = Collections.synchronizedSet(new HashSet<>());
    return t -> seen.add(keyExtractor.apply(t));
  }

  static class ProjectPlanningTimeObj {
    String project;
    String projectTask;

    ProjectPlanningTimeObj(Project project, ProjectTask projectTask) {
      this.project =
          Optional.ofNullable(project).map(Project::getId).map(String::valueOf).orElse("");
      this.projectTask =
          Optional.ofNullable(projectTask).map(ProjectTask::getId).map(String::valueOf).orElse("");
    }

    @Override
    public String toString() {
      return "ProjectPlanningTime{project='" + project + "', projectTask=" + projectTask + '}';
    }
  }

  @Override
  public BigDecimal getOldBudgetedTime(ProjectTask projectTask) {
    BigDecimal oldBudgetedTime = projectTask.getOldBudgetedTime();
    if ((oldBudgetedTime == null || oldBudgetedTime.signum() == 0) && projectTask.getId() != null) {
      oldBudgetedTime = projectTaskRepo.find(projectTask.getId()).getBudgetedTime();
    }

    return oldBudgetedTime;
  }

  @Override
  public Unit getTimeUnit(ProjectTask projectTask) {
    Unit unit = projectTask.getTimeUnit();
    if (unit == null) {
      unit =
          Optional.of(projectTask)
              .map(ProjectTask::getProject)
              .map(Project::getProjectTimeUnit)
              .orElse(null);
    }

    return unit;
  }
}
