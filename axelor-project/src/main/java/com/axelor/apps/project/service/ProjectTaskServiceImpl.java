/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectTaskServiceImpl implements ProjectTaskService {

  protected ProjectTaskRepository projectTaskRepo;
  protected FrequencyRepository frequencyRepo;
  protected FrequencyService frequencyService;
  protected AppBaseService appBaseService;

  @Inject
  public ProjectTaskServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService) {
    this.projectTaskRepo = projectTaskRepo;
    this.frequencyRepo = frequencyRepo;
    this.frequencyService = frequencyService;
    this.appBaseService = appBaseService;
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
    nextProjectTask.setTeam(projectTask.getTeam());
    nextProjectTask.setPriority(projectTask.getPriority());
    nextProjectTask.setStatus(projectTask.getStatus());
    nextProjectTask.setTaskDuration(projectTask.getTaskDuration());
    nextProjectTask.setAssignedTo(projectTask.getAssignedTo());
    nextProjectTask.setDescription(projectTask.getDescription());

    nextProjectTask.setFullName(projectTask.getFullName());
    nextProjectTask.setProject(projectTask.getProject());
    nextProjectTask.setProjectTaskCategory(projectTask.getProjectTaskCategory());
    nextProjectTask.setProgressSelect(0);

    projectTask.getMembersUserSet().forEach(nextProjectTask::addMembersUserSetItem);

    nextProjectTask.setParentTask(projectTask.getParentTask());
    nextProjectTask.setProduct(projectTask.getProduct());
    nextProjectTask.setUnit(projectTask.getUnit());
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
    task.setTaskDate(appBaseService.getTodayDate(project.getCompany()));
    task.setStatus(ProjectTaskRepository.STATUS_NEW);
    task.setPriority(ProjectTaskRepository.PRIORITY_NORMAL);
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
    newProjectTask.setProgressSelect(0);
    newProjectTask.setTaskEndDate(date);
  }

  @Override
  @Transactional
  public void deleteProjectTask(ProjectTask projectTask) {
    projectTaskRepo.remove(projectTask);
  }
}
