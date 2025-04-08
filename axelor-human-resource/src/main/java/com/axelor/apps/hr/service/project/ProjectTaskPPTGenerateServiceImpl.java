package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskPPTGenerateServiceImpl implements ProjectTaskPPTGenerateService {
  protected AppProjectService appProjectService;
  protected ProjectPlanningTimeComputeService projectPlanningTimeComputeService;
  protected ProjectPlanningTimeCreateService projectPlanningTimeCreateService;

  @Inject
  public ProjectTaskPPTGenerateServiceImpl(
      AppProjectService appProjectService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService) {
    this.appProjectService = appProjectService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
  }

  @Override
  public String getSprintOnChangeWarningWithoutSprint(ProjectTask projectTask) {
    BigDecimal oldBudgetedTime = projectPlanningTimeCreateService.getOldBudgetedTime(projectTask);

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        getProjectPlanningTimeOnOldDuration(
            projectTask, projectTask.getTaskDate(), oldBudgetedTime);

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }
    } else {
      if (projectPlanningTimeSet.size() == 1)
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_UPDATE);
      return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_ON_DURATION);
    }
  }

  @Override
  @Transactional
  public void createUpdatePlanningTimeWithoutSprint(ProjectTask projectTask)
      throws AxelorException {

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        getProjectPlanningTimeOnOldDuration(
            projectTask,
            projectTask.getTaskDate(),
            projectPlanningTimeCreateService.getOldBudgetedTime(projectTask));
    if (projectPlanningTimeSet.size() == 1) {
      updateProjectPlanningTimeDatesAndDurationWithoutSprint(
          projectPlanningTimeSet.stream().findFirst().get(), projectTask);
    }
    if (CollectionUtils.isEmpty(projectPlanningTimeSet)) {
      createPlanningTime(projectTask);
    }
  }

  protected void createPlanningTime(ProjectTask projectTask) throws AxelorException {
    LocalDate startDateTime = projectTask.getTaskDate();
    Optional<Employee> employee =
        Optional.of(projectTask).map(ProjectTask::getAssignedTo).map(User::getEmployee);
    if (startDateTime == null || employee.isEmpty()) {
      return;
    }
    ProjectPlanningTime projectPlanningTime =
        projectPlanningTimeCreateService.createProjectPlanningTime(
            startDateTime.atStartOfDay(),
            projectTask,
            projectTask.getProject(),
            100,
            employee.get(),
            projectTask.getProduct(),
            employee.get().getDailyWorkHours(),
            null,
            projectTask.getSite(),
            projectPlanningTimeCreateService.getTimeUnit(projectTask));
    LocalDateTime taskEndDateTime =
        projectPlanningTimeComputeService.computeEndDateTime(
            projectPlanningTime, projectTask.getProject());
    projectPlanningTime.setEndDateTime(taskEndDateTime);

    projectPlanningTime.setDisplayPlannedTime(projectTask.getBudgetedTime());
    Unit timeUnit = projectPlanningTimeCreateService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
    projectTask.addProjectPlanningTimeListItem(projectPlanningTime);
  }

  protected void updateProjectPlanningTimeDatesAndDurationWithoutSprint(
      ProjectPlanningTime projectPlanningTime, ProjectTask projectTask) throws AxelorException {
    if (projectTask.getTaskDate() == null || projectTask.getBudgetedTime() == null) {
      return;
    }
    projectPlanningTime.setStartDateTime(projectTask.getTaskDate().atStartOfDay());
    projectPlanningTime.setDisplayPlannedTime(projectTask.getBudgetedTime());
    LocalDateTime taskEndDateTime =
        projectPlanningTimeComputeService.computeEndDateTime(
            projectPlanningTime, projectTask.getProject());
    projectPlanningTime.setEndDateTime(taskEndDateTime);
    Unit timeUnit = projectPlanningTimeCreateService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
  }

  protected Set<ProjectPlanningTime> getProjectPlanningTimeOnOldDuration(
      ProjectTask projectTask, LocalDate fromDate, BigDecimal duration) {
    Set<ProjectPlanningTime> projectPlanningTimeSet = new HashSet<>();
    if (!ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
      if (fromDate != null && duration != null) {
        return projectTask.getProjectPlanningTimeList().stream()
            .filter(
                ppt ->
                    ppt.getStartDateTime().toLocalDate().equals(fromDate)
                        && ppt.getPlannedTime().equals(duration))
            .collect(Collectors.toSet());
      }
    }

    return projectPlanningTimeSet;
  }
}
