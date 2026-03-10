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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskProgressUpdateService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectTaskBusinessProjectRepository extends ProjectTaskHRRepository {

  protected ProjectTaskProgressUpdateService projectTaskProgressUpdateService;
  private static final Logger log =
      LoggerFactory.getLogger(ProjectTaskBusinessProjectRepository.class);

  @Inject
  public ProjectTaskBusinessProjectRepository(
      ProjectTaskProgressUpdateService projectTaskProgressUpdateService) {
    this.projectTaskProgressUpdateService = projectTaskProgressUpdateService;
  }

  @Override
  public ProjectTask copy(ProjectTask entity, boolean deep) {
    ProjectTask task = super.copy(entity, deep);
    task.setSaleOrderLine(null);
    task.setInvoiceLineSet(Collections.emptySet());
    return task;
  }

  @Override
  public ProjectTask save(ProjectTask projectTask) {
    Integer version = projectTask.getVersion();
    boolean isNew = (version == null || version == 0);

    if (isNew) {
      if (hasMultipleUsers(projectTask)) {
        ProjectTask templateTask = createTemplateAndIndividualTask(projectTask);
        Beans.get(TaskReportService.class).updateAllTaskReported(templateTask);
        try {
          Beans.get(ProjectStatusChangeService.class)
              .updateProjectStatus(templateTask.getProject());
        } catch (AxelorAlertException e) {
          throw new PersistenceException(e.getMessage(), e);
        }
        return templateTask;
      } else {
        // We give priority to the Assigned Employees field if it's set
        if (projectTask.getAssignedEmployees() != null) {
          // Using ifPresent for a cleaner, null-safe flow
          projectTask.getAssignedEmployees().stream()
              .filter(Objects::nonNull)
              .findFirst()
              .ifPresent(
                  user -> {
                    projectTask.setAssignedTo(user);
                    projectTask.getAssignedEmployees().clear();
                    projectTask.setIsTemplate(false);
                  });
        }
      }
    }

    if (!isNew && Boolean.TRUE.equals(projectTask.getIsTemplate())) {
      return updateTemplateAndCascade(projectTask);
    }

    ProjectTask savedTask = super.save(projectTask);
    if (isNew) {
      Beans.get(TaskReportService.class).updateAllTaskReported(savedTask);
    }

    try {
      savedTask =
          projectTaskProgressUpdateService.updateChildrenProgress(
              savedTask, savedTask.getProgress());
      savedTask = projectTaskProgressUpdateService.updateParentsProgress(savedTask);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    // Update the project status to in progress when one of its task is set to in progress
    // At this point task templates are already filtered out.
    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(savedTask.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }

    return savedTask;
  }

  @Override
  public void remove(ProjectTask task) {
    Project project = task.getProject();
    super.remove(task);

    // When a task is deleted the project status needs to be updated to reflect this change
    // That is if it is not a template task
    if (task.getIsTemplate()) {
      try {
        Beans.get(ProjectStatusChangeService.class).updateProjectStatus(project);
      } catch (AxelorAlertException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    }
  }

  /** check if task has multiple users */
  private static boolean hasMultipleUsers(ProjectTask task) {
    return task.getAssignedEmployees() != null && task.getAssignedEmployees().size() > 1;
  }

  /** Create template task and individual assigned tasks */
  private ProjectTask createTemplateAndIndividualTask(ProjectTask entity) {
    log.debug("Starting auto creation of tasks for entity id={}", entity.getId());

    // Save template first
    entity.setIsTemplate(true);
    entity.setAssignedTo(null); // Template has no single assignee

    log.debug("Saving template task (isTemplate=true)");
    ProjectTask template = super.save(entity);
    log.debug("Template saved, version: {}", template.getVersion());

    log.debug(
        "Template saved with id={}, assignedEmployeesCount={}",
        template.getId(),
        template.getAssignedEmployees() != null ? template.getAssignedEmployees().size() : 0);

    // Create individual tasks for each user
    Set<User> users = new HashSet<>(template.getAssignedEmployees());
    log.debug("Creating individual tasks for {} users", users.size());

    for (User user : users) {
      log.debug("Creating task for user id={}, name={}", user.getId(), user.getFullName());
      createTaskForUser(template, user);
    }

    log.debug("Finished auto creation of tasks for template id={}", template.getId());
    return template;
  }

  /** Update template task and cascade changes to assigned tasks */
  private ProjectTask updateTemplateAndCascade(ProjectTask template) {
    log.debug("Updating template task id={}", template.getId());

    // Get existing individual assigned tasks
    List<ProjectTask> existingTask =
        JPA.all(ProjectTask.class)
            .filter("self.templateTaskId = :templateId AND self.isTemplate = false")
            .bind("templateId", template.getId().toString())
            .fetch();

    log.debug(
        "Found {} existing individual tasks for template id={}",
        existingTask.size(),
        template.getId());

    // Get users of already assigned tasks
    Set<User> existingUsers = new HashSet<>();
    for (ProjectTask task : existingTask) {
      existingUsers.add(task.getAssignedTo());
    }

    log.debug("Existing assigned users count={}", existingUsers.size());

    // Get the current assigned users
    Set<User> currentUsers =
        template.getAssignedEmployees() != null
            ? new HashSet<>(template.getAssignedEmployees())
            : new HashSet<>();

    log.debug("Current assigned users count={}", currentUsers.size());

    // Find users to add
    Set<User> usersToAdd = new HashSet<>(currentUsers);
    usersToAdd.removeAll(existingUsers);

    // Find users to remove
    Set<User> usersToRemove = new HashSet<>(existingUsers);
    usersToRemove.removeAll(currentUsers);

    log.debug("Users to add={}, users to remove={}", usersToAdd.size(), usersToRemove.size());

    log.debug("Saving updated template id={}", template.getId());
    ProjectTask savedTemplate = super.save(template);

    // Create individual tasks for new users
    for (User user : usersToAdd) {
      log.debug("Adding task for new user id={}, name={}", user.getId(), user.getFullName());
      createTaskForUser(savedTemplate, user);
    }

    // Remove existing user tasks
    for (User user : usersToRemove) {
      log.debug("Removing task for user: {}", user.getFullName());

      ProjectTask taskToRemove =
          existingTask.stream()
              .filter(t -> t.getAssignedTo().equals(user))
              .findFirst()
              .orElse(null);

      if (taskToRemove != null) {
        try {
          log.debug(
              "Removing task id: {} for user id: {}", taskToRemove.getId(), user.getFullName());
          remove(taskToRemove);
        } catch (PersistenceException e) {
          log.error(
              "Failed to remove task id={} for user: {}",
              taskToRemove.getId(),
              user.getFullName(),
              e);
        }
      } else {
        log.warn(
            "No task found to remove for user name: {}, id: {}", user.getId(), user.getFullName());
      }
    }

    // Cascade updates for remaining tasks
    for (ProjectTask task : existingTask) {
      if (currentUsers.contains(task.getAssignedTo())) {
        log.debug(
            "Cascading template updates to task id: {} (user: {})",
            task.getId(),
            task.getAssignedTo().getFullName());
        cascadeTemplateUpdatesToTask(savedTemplate, task);
      }
    }

    log.debug("Finished updating template id={}", savedTemplate.getId());
    return savedTemplate;
  }

  /** Create a new task for a specific user */
  private void createTaskForUser(ProjectTask template, User user) {
    log.debug(
        "Creating individual task from template id={} for user id={}, name={}",
        template.getId(),
        user.getFullName(),
        user.getFullName());

    ProjectTask userTask = new ProjectTask();

    userTask.setIsTemplate(false);
    userTask.setTemplateTaskId(template.getId().toString());
    userTask.setAssignedTo(user);

    // Copy fields from template
    copyTaskFields(template, userTask);

    ProjectTask savedTask = super.save(userTask);

    log.debug(
        "Individual task created with id={} for user={}", savedTask.getId(), user.getFullName());
  }

  /** Copy fields from source to target */
  private void copyTaskFields(ProjectTask source, ProjectTask target) {

    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setProject(source.getProject());
    target.setStatus(source.getStatus());
    target.setPriority(source.getPriority());
    target.setProjectTaskCategory(source.getProjectTaskCategory());

    target.setStartTime(source.getStartTime());
    target.setEndTime(source.getEndTime());
    target.setTaskDate(source.getTaskDate());
    target.setTaskEndDate(source.getTaskEndDate());
    target.setTaskDeadline(source.getTaskDeadline());
    target.setTaskDuration(source.getTaskDuration());

    target.setProduct(source.getProduct());
    target.setActiveSprint(source.getActiveSprint());
    target.setBudgetedTime(source.getBudgetedTime());
    target.setSite(source.getSite());
  }

  /** Cascade updates from template to individual tasks */
  private void cascadeTemplateUpdatesToTask(ProjectTask template, ProjectTask task) {

    // Update fields of interest
    task.setName(template.getName());
    task.setDescription(template.getDescription());
    task.setPriority(template.getPriority());
    task.setProjectTaskCategory(template.getProjectTaskCategory());

    // Time fields
    task.setStartTime(template.getStartTime());
    task.setEndTime(template.getEndTime());
    task.setTaskDate(template.getTaskDate());
    task.setTaskEndDate(template.getTaskEndDate());
    task.setTaskDeadline(template.getTaskDeadline());
    task.setTaskDuration(template.getTaskDuration());

    task.setProduct(template.getProduct());
    task.setSite(template.getSite());
    task.setActiveSprint(template.getActiveSprint());

    super.save(task);
  }
}
