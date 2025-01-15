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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPriority;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.apps.project.db.repo.ProjectPriorityRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusProgressByCategoryRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTaskServiceImpl implements ProjectTaskService {

  protected ProjectTaskRepository projectTaskRepo;
  protected FrequencyRepository frequencyRepo;
  protected FrequencyService frequencyService;
  protected AppBaseService appBaseService;
  protected ProjectRepository projectRepository;
  protected AppProjectService appProjectService;
  protected TaskStatusToolService taskStatusToolService;
  protected TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository;

  private static final String TASK_LINK = "<a href=\"#/ds/all.open.project.tasks/edit/%s\">@%s</a>";

  @Inject
  public ProjectTaskServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      AppProjectService appProjectService,
      TaskStatusToolService taskStatusToolService,
      TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository) {
    this.projectTaskRepo = projectTaskRepo;
    this.frequencyRepo = frequencyRepo;
    this.frequencyService = frequencyService;
    this.appBaseService = appBaseService;
    this.projectRepository = projectRepository;
    this.appProjectService = appProjectService;
    this.taskStatusToolService = taskStatusToolService;
    this.taskStatusProgressByCategoryRepository = taskStatusProgressByCategoryRepository;
  }

  @Override
  @Transactional
  public void generateTasks(ProjectTask projectTask, Frequency frequency) {
    List<LocalDate> taskDateList =
        frequencyService.getDates(frequency, projectTask.getTaskDate(), frequency.getEndDate());

    taskDateList.removeIf(date -> date.equals(projectTask.getTaskDate()));

    // limit how many ProjectTask will be generated at once
    Integer limitNumberTasksGenerated = appBaseService.getAppBase().getLimitNumberTasksGenerated();
    if (taskDateList.size() > limitNumberTasksGenerated) {
      taskDateList = taskDateList.subList(0, limitNumberTasksGenerated);
    }

    ProjectTask lastTask = projectTask;
    for (LocalDate date : taskDateList) {
      ProjectTask newProjectTask = projectTaskRepo.copy(projectTask, false);
      setModuleFields(projectTask, date, newProjectTask);
      projectTaskRepo.save(newProjectTask);

      lastTask.setNextProjectTask(newProjectTask);
      projectTaskRepo.save(lastTask);
      lastTask = newProjectTask;
    }
  }

  @Override
  @Transactional
  public void updateNextTask(ProjectTask projectTask) {
    ProjectTask nextProjectTask = projectTask.getNextProjectTask();
    if (nextProjectTask != null) {
      updateModuleFields(projectTask, nextProjectTask);

      projectTaskRepo.save(nextProjectTask);
      updateNextTask(nextProjectTask);
    }
  }

  protected void updateModuleFields(ProjectTask projectTask, ProjectTask nextProjectTask) {
    nextProjectTask.setName(projectTask.getName());
    nextProjectTask.setPriority(projectTask.getPriority());
    nextProjectTask.setStatus(projectTask.getStatus());
    nextProjectTask.setTaskDuration(projectTask.getTaskDuration());
    nextProjectTask.setAssignedTo(projectTask.getAssignedTo());
    nextProjectTask.setDescription(projectTask.getDescription());

    nextProjectTask.setFullName(projectTask.getFullName());
    nextProjectTask.setProject(projectTask.getProject());
    nextProjectTask.setProjectTaskCategory(projectTask.getProjectTaskCategory());
    nextProjectTask.setProgress(BigDecimal.ZERO);

    projectTask.getMembersUserSet().forEach(nextProjectTask::addMembersUserSetItem);

    nextProjectTask.setParentTask(projectTask.getParentTask());
    nextProjectTask.setProduct(projectTask.getProduct());
    nextProjectTask.setInvoicingUnit(projectTask.getInvoicingUnit());
    nextProjectTask.setQuantity(projectTask.getQuantity());
    nextProjectTask.setUnitPrice(projectTask.getUnitPrice());
    nextProjectTask.setTaskEndDate(projectTask.getTaskEndDate());
    nextProjectTask.setBudgetedTime(projectTask.getBudgetedTime());
    nextProjectTask.setCurrency(projectTask.getCurrency());
  }

  @Override
  @Transactional
  public void removeNextTasks(ProjectTask projectTask) {
    List<ProjectTask> projectTaskList = getAllNextTasks(projectTask);
    projectTask.setNextProjectTask(null);
    projectTask.setHasDateOrFrequencyChanged(false);
    projectTaskRepo.save(projectTask);

    for (ProjectTask projectTaskToRemove : projectTaskList) {
      projectTaskRepo.remove(projectTaskToRemove);
    }
  }

  /** Returns next tasks from given {@link ProjectTask}. */
  public List<ProjectTask> getAllNextTasks(ProjectTask projectTask) {
    List<ProjectTask> projectTaskList = new ArrayList<>();

    ProjectTask current = projectTask;
    while (current.getNextProjectTask() != null) {
      current = current.getNextProjectTask();
      projectTaskList.add(current);
    }

    for (ProjectTask task : projectTaskList) {
      task.setNextProjectTask(null);
      projectTaskRepo.save(task);
    }

    return projectTaskList;
  }

  @Override
  @Transactional
  public ProjectTask create(String subject, Project project, User assignedTo) {
    ProjectTask task = new ProjectTask();
    task.setName(subject);
    task.setAssignedTo(assignedTo);
    task.setTaskDate(appBaseService.getTodayDate(null));
    task.setStatus(getStatus(project, task));
    task.setPriority(getPriority(project));
    project.addProjectTaskListItem(task);
    projectTaskRepo.save(task);
    return task;
  }

  protected void setModuleFields(
      ProjectTask projectTask, LocalDate date, ProjectTask newProjectTask) {

    newProjectTask.setIsFirst(false);
    newProjectTask.setHasDateOrFrequencyChanged(false);
    newProjectTask.setDoApplyToAllNextTasks(false);
    newProjectTask.setFrequency(frequencyRepo.copy(projectTask.getFrequency(), false));
    newProjectTask.setTaskDate(date);
    newProjectTask.setTaskDeadline(date);
    newProjectTask.setNextProjectTask(null);
    // Module 'project' fields
    newProjectTask.setProgress(BigDecimal.ZERO);
    newProjectTask.setTaskEndDate(date);
  }

  @Override
  public TaskStatus getStatus(Project project, ProjectTask projectTask) {
    if (project == null) {
      return null;
    }

    project = projectRepository.find(project.getId());
    Set<TaskStatus> projectStatusSet = taskStatusToolService.getTaskStatusSet(project, projectTask);

    return ObjectUtils.isEmpty(projectStatusSet)
        ? null
        : projectStatusSet.stream()
            .min(Comparator.comparingInt(TaskStatus::getSequence))
            .orElse(null);
  }

  @Override
  public ProjectPriority getPriority(Project project) {
    if (project == null) {
      return null;
    }

    project = projectRepository.find(project.getId());

    return ObjectUtils.isEmpty(project.getProjectTaskPrioritySet())
        ? null
        : project.getProjectTaskPrioritySet().stream()
            .filter(
                priority ->
                    priority.getTechnicalTypeSelect()
                        == ProjectPriorityRepository.PROJECT_PRIORITY_NORMAL)
            .findAny()
            .orElse(null);
  }

  @Transactional
  public void deleteProjectTask(ProjectTask projectTask) {
    projectTaskRepo.remove(projectTask);
  }

  @Override
  public void deleteProjectTasks(List<Integer> projectTasksIds) {
    if (ObjectUtils.isEmpty(projectTasksIds)) {
      return;
    }

    for (Integer id : projectTasksIds) {
      deleteProjectTask(projectTaskRepo.find(Long.valueOf(id)));
    }
  }

  @Override
  public String getTaskLink(String value) {
    if (StringUtils.isEmpty(value)) {
      return value;
    }
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = Pattern.compile("@([^\\s]+)").matcher(value);
    Matcher nonMatcher = Pattern.compile("@([^\\s]+)(?=<\\/a>)").matcher(value);
    while (matcher.find()) {
      String matchedValue = matcher.group(1);
      String ticketNumber = matchedValue.replaceAll("\\<.*?\\>", "");
      if (nonMatcher.find() && ticketNumber.equals(nonMatcher.group(1))) {
        continue;
      }
      ProjectTask task =
          projectTaskRepo.all().filter("self.ticketNumber = ?1", ticketNumber).fetchOne();
      if (task != null) {
        matcher.appendReplacement(buffer, String.format(TASK_LINK, task.getId(), matchedValue));
      }
    }

    String result = buffer.toString();
    return StringUtils.isEmpty(result) ? value : result;
  }

  @Override
  public void fillSubtask(ProjectTask projectTask) {
    ProjectTask parentTask = projectTaskRepo.find(projectTask.getParentTask().getId());
    projectTask.setParentTask(parentTask);
    projectTask.setProjectTaskCategory(parentTask.getProjectTaskCategory());
    projectTask.setPriority(parentTask.getPriority());
    projectTask.setTagSet(parentTask.getTagSet());
    projectTask.setAssignedTo(parentTask.getAssignedTo());
    projectTask.setTargetVersion(projectTask.getParentTask().getTargetVersion());
  }

  @Override
  public void changeProgress(ProjectTask projectTask, Project project) {
    if (projectTask == null) {
      return;
    }

    projectTask.setProgress(getNewProgress(projectTask, project));
  }

  protected BigDecimal getNewProgress(ProjectTask projectTask, Project project) {
    AppProject appProject = appProjectService.getAppProject();
    if (appProject != null
        && appProject.getSelectAutoProgressOnProjectTask()
        && projectTask.getStatus() != null) {
      BigDecimal newProgress = projectTask.getStatus().getDefaultProgress();
      if (appProject.getEnableStatusManagementByTaskCategory()
          && project != null
          && project.getEnableStatusProgressByCategory()
          && projectTask.getProjectTaskCategory() != null) {
        TaskStatusProgressByCategory taskStatusProgressByCategory =
            taskStatusProgressByCategoryRepository.findByCategoryAndStatus(
                projectTask.getProjectTaskCategory(), projectTask.getStatus());
        if (taskStatusProgressByCategory != null) {
          newProgress = taskStatusProgressByCategory.getProgress();
        }
      }

      return newProgress;
    }

    return projectTask.getProgress();
  }
}
