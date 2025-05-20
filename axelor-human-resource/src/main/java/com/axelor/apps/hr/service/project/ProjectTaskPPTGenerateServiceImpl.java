package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
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
  protected ProjectPlanningTimeService projectPlanningTimeService;

  @Inject
  public ProjectTaskPPTGenerateServiceImpl(
      AppProjectService appProjectService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      ProjectPlanningTimeService projectPlanningTimeService) {
    this.appProjectService = appProjectService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.projectPlanningTimeService = projectPlanningTimeService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createUpdatePlanningTimeWithoutSprint(ProjectTask projectTask)
      throws AxelorException {

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        getProjectPlanningTimeOnOldDuration(
            projectTask,
            projectTask.getTaskDate(),
            projectPlanningTimeService.getOldBudgetedTime(projectTask));
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
            projectPlanningTimeService.getTimeUnit(projectTask));
    LocalDateTime taskEndDateTime =
        projectPlanningTimeComputeService.computeEndDateTime(
            projectPlanningTime, projectTask.getProject());
    projectPlanningTime.setEndDateTime(taskEndDateTime);

    projectPlanningTime.setDisplayPlannedTime(projectTask.getBudgetedTime());
    Unit timeUnit = projectPlanningTimeService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
    projectTask.addProjectPlanningTimeListItem(projectPlanningTime);
  }

  @Override
  public void updateProjectPlanningTimeDatesAndDurationWithoutSprint(
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
    Unit timeUnit = projectPlanningTimeService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
  }

  @Override
  public Set<ProjectPlanningTime> getProjectPlanningTimeOnOldDuration(
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
