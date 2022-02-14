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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.ResourceBooking;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.apps.project.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectServiceImpl implements ProjectService {

  public static final int MAX_LEVEL_OF_PROJECT = 10;

  private ProjectRepository projectRepository;

  @Inject
  public ProjectServiceImpl(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Inject WikiRepository wikiRepo;
  @Inject ProjectTaskService projectTaskService;
  @Inject ResourceBookingService resourceBookingService;

  @Override
  public Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner clientPartner) {
    Project project;
    project = projectRepository.findByName(fullName);
    if (project != null) {
      return project;
    }
    project = new Project();
    project.setStatusSelect(ProjectRepository.STATE_NEW);
    project.setParentProject(parentProject);
    if (parentProject != null) {
      parentProject.addChildProjectListItem(project);
      project.setProjectTypeSelect(ProjectRepository.TYPE_PHASE);
    } else {
      project.setProjectTypeSelect(ProjectRepository.TYPE_PROJECT);
    }
    if (Strings.isNullOrEmpty(fullName)) {
      fullName = "project";
    }
    project.setName(fullName);
    project.setFullName(project.getName());
    project.setCompany(company);
    project.setClientPartner(clientPartner);
    project.setAssignedTo(assignedTo);
    project.setProgress(BigDecimal.ZERO);
    return project;
  }

  @Override
  @Transactional
  public Project generateProject(Partner partner) {
    Preconditions.checkNotNull(partner);
    User user = AuthUtils.getUser();
    Project project =
        Beans.get(ProjectService.class)
            .generateProject(
                null, getUniqueProjectName(partner), user, user.getActiveCompany(), partner);
    return projectRepository.save(project);
  }

  private String getUniqueProjectName(Partner partner) {
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

    Project project = new Project();
    project.setName(projectTemplate.getName());

    if (projectRepository.all().filter("self.code = ?", projectCode).count() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, ITranslation.PROJECT_CODE_ERROR);
    } else {

      project.setCode(projectCode);
      project.setClientPartner(clientPartner);
      if (clientPartner != null
          && clientPartner.getContactPartnerSet() != null
          && !clientPartner.getContactPartnerSet().isEmpty()) {
        project.setContactPartner(clientPartner.getContactPartnerSet().iterator().next());
      }
      project.setDescription(projectTemplate.getDescription());
      project.setTeam(projectTemplate.getTeam());
      project.setProjectFolderSet(new HashSet<>(projectTemplate.getProjectFolderSet()));
      project.setAssignedTo(projectTemplate.getAssignedTo());
      project.setProjectTaskCategorySet(new HashSet<>(projectTemplate.getProjectTaskCategorySet()));
      project.setSynchronize(projectTemplate.getSynchronize());
      project.setMembersUserSet(new HashSet<>(projectTemplate.getMembersUserSet()));
      project.setImputable(projectTemplate.getImputable());
      project.setCompany(projectTemplate.getCompany());
      project.setProductSet(new HashSet<>(projectTemplate.getProductSet()));
      project.setExcludePlanning(projectTemplate.getExcludePlanning());
      project.setProjectTypeSelect(ProjectRepository.TYPE_PROJECT);

      List<Wiki> wikiList = projectTemplate.getWikiList();

      if (wikiList != null && !wikiList.isEmpty()) {

        for (Wiki wiki : wikiList) {
          wiki = wikiRepo.copy(wiki, false);
          wiki.setProjectTemplate(null);
          project.addWikiListItem(wiki);
        }
      }

      projectRepository.save(project);

      Set<TaskTemplate> taskTemplateSet = projectTemplate.getTaskTemplateSet();
      if (ObjectUtils.isEmpty(taskTemplateSet)) {
        return project;
      }
      List<TaskTemplate> taskTemplateList = new ArrayList<TaskTemplate>(taskTemplateSet);
      Collections.sort(
          taskTemplateList,
          new Comparator<TaskTemplate>() {

            @Override
            public int compare(TaskTemplate taskTemplatet1, TaskTemplate taskTemplate2) {
              return taskTemplatet1.getParentTaskTemplate() == null || taskTemplate2 == null
                  ? 1
                  : taskTemplatet1.getParentTaskTemplate().equals(taskTemplate2) ? -1 : 1;
            }
          });

      if (taskTemplateList != null) {
        for (TaskTemplate taskTemplate : taskTemplateList) {
          createTask(taskTemplate, project, taskTemplateSet);
        }
      }

      return project;
    }
  }

  public ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet) {

    if (!ObjectUtils.isEmpty(project.getProjectTaskList())) {
      for (ProjectTask projectTask : project.getProjectTaskList()) {
        if (projectTask.getName().equals(taskTemplate.getName())) {
          return projectTask;
        }
      }
    }
    ProjectTask task =
        projectTaskService.create(taskTemplate.getName(), project, taskTemplate.getAssignedTo());
    task.setDescription(taskTemplate.getDescription());
    ProjectTaskCategory projectTaskCategory = taskTemplate.getProjectTaskCategory();
    if (projectTaskCategory != null) {
      task.setProjectTaskCategory(projectTaskCategory);
      project.addProjectTaskCategorySetItem(projectTaskCategory);
    }

    TaskTemplate parentTaskTemplate = taskTemplate.getParentTaskTemplate();

    if (parentTaskTemplate != null && taskTemplateSet.contains(parentTaskTemplate)) {
      task.setParentTask(this.createTask(parentTaskTemplate, project, taskTemplateSet));
      return task;
    }
    return task;
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
}
