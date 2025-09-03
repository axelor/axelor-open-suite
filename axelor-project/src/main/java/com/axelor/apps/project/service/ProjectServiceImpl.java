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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.ResourceBooking;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.studio.db.AppProject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProjectServiceImpl implements ProjectService {

  public static final int MAX_LEVEL_OF_PROJECT = 10;

  protected ProjectRepository projectRepository;
  protected ProjectStatusRepository projectStatusRepository;
  protected AppProjectService appProjectService;
  protected ProjectCreateTaskService projectCreateTaskService;
  protected WikiRepository wikiRepo;
  protected ResourceBookingService resourceBookingService;
  protected ProjectNameComputeService projectNameComputeService;

  @Inject
  public ProjectServiceImpl(
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      AppProjectService appProjectService,
      ProjectCreateTaskService projectCreateTaskService,
      WikiRepository wikiRepo,
      ResourceBookingService resourceBookingService,
      ProjectNameComputeService projectNameComputeService) {
    this.projectRepository = projectRepository;
    this.projectStatusRepository = projectStatusRepository;
    this.appProjectService = appProjectService;
    this.projectCreateTaskService = projectCreateTaskService;
    this.wikiRepo = wikiRepo;
    this.resourceBookingService = resourceBookingService;
    this.projectNameComputeService = projectNameComputeService;
  }

  @Override
  public Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner clientPartner)
      throws AxelorException {
    Project project;
    project = projectRepository.findByName(fullName);
    if (project != null) {
      return project;
    }
    project = new Project();
    project.setParentProject(parentProject);
    if (parentProject != null) {
      parentProject.addChildProjectListItem(project);
    }
    if (Strings.isNullOrEmpty(fullName)) {
      fullName = "project";
    }
    project.setName(fullName);
    project.setFullName(projectNameComputeService.setProjectFullName(project));
    project.setClientPartner(clientPartner);
    project.setAssignedTo(assignedTo);
    project.setProjectStatus(getDefaultProjectStatus());

    manageTaskStatus(project, parentProject);

    project.setProjectTaskPrioritySet(
        new HashSet<>(appProjectService.getAppProject().getDefaultPrioritySet()));
    project.setCompletedTaskStatus(appProjectService.getAppProject().getCompletedTaskStatus());
    // add default sites on new project
    if (appProjectService.getAppBase().getEnableSiteManagementForProject()) {
      for (Site site : appProjectService.getAppBase().getDefaultSitesSet()) {
        project.addSiteSetItem(site);
      }
    }
    return project;
  }

  @Override
  @Transactional
  public Project generateProject(Partner partner) throws AxelorException {
    Preconditions.checkNotNull(partner);
    User user = AuthUtils.getUser();
    Project project =
        Beans.get(ProjectService.class)
            .generateProject(
                null, getUniqueProjectName(partner), user, user.getActiveCompany(), partner);
    return projectRepository.save(project);
  }

  protected String getUniqueProjectName(Partner partner) {
    String baseName = String.format(I18n.get("%s project"), partner.getName());
    long count =
        projectRepository.all().filter(String.format("self.name LIKE '%s%%'", baseName)).count();

    if (count == 0) {
      return baseName;
    }

    String name;

    do {
      name = String.format("%s %d", baseName, ++count);
    } while (projectRepository.findByName(name) != null);

    return name;
  }

  @Override
  @Transactional
  public Project createProjectFromTemplate(
      ProjectTemplate projectTemplate, String projectCode, Partner clientPartner)
      throws AxelorException {
    if (projectRepository.findByCode(projectCode) != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProjectExceptionMessage.PROJECT_CODE_ERROR));
    }

    Project project = generateProject(projectTemplate, projectCode, clientPartner);
    setWikiItems(project, projectTemplate);
    projectRepository.save(project);

    Set<TaskTemplate> taskTemplateSet = projectTemplate.getTaskTemplateSet();
    if (ObjectUtils.isEmpty(taskTemplateSet)) {
      return project;
    }
    List<TaskTemplate> taskTemplateList = new ArrayList<>(taskTemplateSet);
    Collections.sort(
        taskTemplateList,
        (taskTemplatet1, taskTemplate2) ->
            taskTemplatet1.getParentTaskTemplate() == null || taskTemplate2 == null
                ? 1
                : taskTemplatet1.getParentTaskTemplate().equals(taskTemplate2) ? -1 : 1);

    if (!ObjectUtils.isEmpty(taskTemplateList)) {
      for (TaskTemplate taskTemplate : taskTemplateList) {
        projectCreateTaskService.createTask(taskTemplate, project, taskTemplateSet);
      }
    }
    return project;
  }

  @Override
  public Map<String, Object> getTaskView(
      Project project, String title, String domain, Map<String, Object> context) {
    ActionViewBuilder builder =
        ActionView.define(I18n.get(title))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(domain)
            .param("details-view", "true");

    if (project.getIsShowKanbanPerCategory() && project.getIsShowCalendarPerCategory()) {
      builder.add("kanban", "task-per-category-kanban");
      builder.add("calendar", "project-task-per-category-calendar");
    } else {
      builder.add("kanban", "project-task-kanban");
      builder.add("calendar", "project-task-per-status-calendar");
    }

    if (ObjectUtils.notEmpty(context)) {
      context.forEach(builder::context);
    }
    return builder.map();
  }

  protected void setWikiItems(Project project, ProjectTemplate projectTemplate) {
    List<Wiki> wikiList = projectTemplate.getWikiList();
    if (ObjectUtils.notEmpty(wikiList)) {
      for (Wiki wiki : wikiList) {
        wiki = wikiRepo.copy(wiki, false);
        wiki.setProjectTemplate(null);
        project.addWikiListItem(wiki);
      }
    }
  }

  @Override
  public Project generateProject(
      ProjectTemplate projectTemplate, String projectCode, Partner clientPartner) {
    Project project = new Project();
    project.setName(projectTemplate.getName());
    project.setCode(projectCode);
    project.setClientPartner(clientPartner);
    project.setDescription(projectTemplate.getDescription());
    project.setTeam(projectTemplate.getTeam());
    project.setAssignedTo(projectTemplate.getAssignedTo());
    project.setProjectTaskCategorySet(new HashSet<>(projectTemplate.getProjectTaskCategorySet()));
    project.setSynchronize(projectTemplate.getSynchronize());
    project.setMembersUserSet(new HashSet<>(projectTemplate.getMembersUserSet()));
    project.setProductSet(new HashSet<>(projectTemplate.getProductSet()));
    project.setProjectStatus(getDefaultProjectStatus());

    manageTaskStatus(project, projectTemplate);

    project.setProjectTaskPrioritySet(
        new HashSet<>(appProjectService.getAppProject().getDefaultPrioritySet()));
    project.setCompletedTaskStatus(appProjectService.getAppProject().getCompletedTaskStatus());
    if (clientPartner != null && ObjectUtils.notEmpty(clientPartner.getContactPartnerSet())) {
      project.setContactPartner(clientPartner.getContactPartnerSet().iterator().next());
    }
    return project;
  }

  @Override
  public Map<String, Object> getPerStatusKanban(Project project, Map<String, Object> context) {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("All tasks"))
            .model(ProjectTask.class.getName())
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain("self.typeSelect = :_typeSelect AND self.project = :_project");

    if (ObjectUtils.notEmpty(context)) {
      context.forEach(builder::context);
    }
    return builder.map();
  }

  @Override
  public ProjectStatus getDefaultProjectStatus() {
    return projectStatusRepository.all().order("sequence").fetchOne();
  }

  public boolean checkIfResourceBooked(Project project) {

    List<ResourceBooking> resourceBookingList = project.getResourceBookingList();
    if (resourceBookingList != null) {
      for (ResourceBooking resourceBooking : resourceBookingList) {
        if (resourceBooking.getFromDate() != null
            && resourceBooking.getToDate() != null
            && (resourceBookingService.checkIfResourceBooked(resourceBooking)
                || checkIfResourceBookedInList(resourceBookingList, resourceBooking))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkIfResourceBookedInList(
      List<ResourceBooking> resourceBookingList, ResourceBooking resourceBooking) {

    return resourceBookingList.stream()
        .anyMatch(
            x ->
                !x.equals(resourceBooking)
                    && x.getResource().equals(resourceBooking.getResource())
                    && x.getFromDate() != null
                    && x.getToDate() != null
                    && ((resourceBooking.getFromDate().compareTo(x.getFromDate()) >= 0
                            && resourceBooking.getFromDate().compareTo(x.getToDate()) <= 0)
                        || (resourceBooking.getToDate().compareTo(x.getFromDate()) >= 0)
                            && resourceBooking.getToDate().compareTo(x.getToDate()) <= 0));
  }

  protected void manageTaskStatus(Project project, ProjectTemplate projectTemplate) {
    Integer taskStatusManagement =
        Optional.ofNullable(projectTemplate)
            .map(ProjectTemplate::getTaskStatusManagementSelect)
            .orElse(ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT);

    Set<TaskStatus> taskStatusSet = null;
    if (taskStatusManagement == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT) {
      taskStatusSet =
          Optional.ofNullable(projectTemplate)
              .map(ProjectTemplate::getProjectTaskStatusSet)
              .orElse(
                  Optional.ofNullable(appProjectService.getAppProject())
                      .map(AppProject::getDefaultTaskStatusSet)
                      .orElse(null));
    }

    initTaskStatus(project, taskStatusManagement, taskStatusSet);
  }

  protected void manageTaskStatus(Project project, Project parentProject) {
    Integer taskStatusManagement =
        Optional.ofNullable(parentProject)
            .map(Project::getTaskStatusManagementSelect)
            .orElse(ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT);

    Set<TaskStatus> taskStatusSet = null;
    if (taskStatusManagement == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT) {
      taskStatusSet =
          Optional.ofNullable(parentProject)
              .map(Project::getProjectTaskStatusSet)
              .orElse(
                  Optional.ofNullable(appProjectService.getAppProject())
                      .map(AppProject::getDefaultTaskStatusSet)
                      .orElse(null));
    }

    initTaskStatus(project, taskStatusManagement, taskStatusSet);
  }

  protected void initTaskStatus(
      Project project, Integer taskStatusManagement, Set<TaskStatus> taskStatusSet) {
    project.setTaskStatusManagementSelect(taskStatusManagement);

    if (!ObjectUtils.isEmpty(taskStatusSet)) {
      project.setProjectTaskStatusSet(new HashSet<>(taskStatusSet));
    }
  }
}
