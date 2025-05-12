package com.axelor.apps.hr.service.project;

import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectPlanningTimeWarningServiceImpl implements ProjectPlanningTimeWarningService {
  protected AppProjectService appProjectService;
  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectPlanningTimeService projectPlanningTimeService;
  protected ProjectTaskSprintService projectTaskSprintService;
  protected ProjectTaskPPTGenerateService projectTaskPPTGenerateService;

  @Inject
  public ProjectPlanningTimeWarningServiceImpl(
      AppProjectService appProjectService,
      ProjectTaskRepository projectTaskRepository,
      ProjectPlanningTimeService projectPlanningTimeService,
      ProjectTaskSprintService projectTaskSprintService,
      ProjectTaskPPTGenerateService projectTaskPPTGenerateService) {
    this.appProjectService = appProjectService;
    this.projectTaskRepository = projectTaskRepository;
    this.projectPlanningTimeService = projectPlanningTimeService;
    this.projectTaskSprintService = projectTaskSprintService;
    this.projectTaskPPTGenerateService = projectTaskPPTGenerateService;
  }

  @Override
  public String getSprintWarning(ProjectTask projectTask) {
    if (projectTask.getActiveSprint() == null) {
      return getWarningWithoutSprint(projectTask);
    } else {
      return getSprintOnChangeWarning(projectTask);
    }
  }

  protected String getSprintOnChangeWarning(ProjectTask projectTask) {
    if (projectTaskSprintService.validateConfigAndSprint(projectTask) == null) {
      return "";
    }

    Sprint savedSprint = projectTaskSprintService.getOldActiveSprint(projectTask);
    BigDecimal oldBudgetedTime = projectPlanningTimeService.getOldBudgetedTime(projectTask);

    Sprint backlogSprint =
        Optional.of(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getBacklogSprint)
            .orElse(null);

    if (projectTask.getActiveSprint().equals(backlogSprint)) {
      return "";
    }

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        projectTaskSprintService.getProjectPlanningTimeOnOldSprint(projectTask, savedSprint);

    String warning =
        getBudgetedTimeOnChangeWarning(projectPlanningTimeSet, oldBudgetedTime, projectTask);
    if (StringUtils.notEmpty(warning)) {
      return warning;
    }

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)
        || savedSprint == null
        || savedSprint.equals(backlogSprint)
        || savedSprint.equals(projectTask.getActiveSprint())) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }
    } else {
      return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_ON_OLD_SPRINT);
    }
  }

  protected String getWarningWithoutSprint(ProjectTask projectTask) {
    BigDecimal oldBudgetedTime = projectPlanningTimeService.getOldBudgetedTime(projectTask);

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        projectTaskPPTGenerateService.getProjectPlanningTimeOnOldDuration(
            projectTask, projectTask.getTaskDate(), oldBudgetedTime);

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }
    } else {
      if (projectPlanningTimeSet.size() == 1) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_UPDATE);
      }
      return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_ON_DURATION);
    }
  }

  protected String getBudgetedTimeOnChangeWarning(
      Set<ProjectPlanningTime> projectPlanningTimeSet,
      BigDecimal oldBudgetedTime,
      ProjectTask projectTask) {
    if (projectTask.getBudgetedTime().signum() == 0
        || projectTask.getBudgetedTime().compareTo(oldBudgetedTime) == 0
        || ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      return "";
    }

    projectPlanningTimeSet =
        projectPlanningTimeSet.stream()
            .filter(ppt -> ppt.getDisplayPlannedTime().compareTo(oldBudgetedTime) == 0)
            .collect(Collectors.toSet());

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }

    } else {
      return I18n.get(
          HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_WITH_OLD_DURATION);
    }
  }
}
