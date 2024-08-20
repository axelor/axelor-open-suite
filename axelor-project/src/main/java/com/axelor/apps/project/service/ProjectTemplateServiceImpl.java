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
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.studio.db.AppProject;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProjectTemplateServiceImpl implements ProjectTemplateService {

  protected ProjectTemplateRepository projectTemplateRepo;
  protected TaskTemplateService taskTemplateService;
  protected ProjectService projectService;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectTemplateServiceImpl(
      ProjectTemplateRepository projectTemplateRepo,
      TaskTemplateService taskTemplateService,
      ProjectService projectService,
      AppProjectService appProjectService) {
    this.projectTemplateRepo = projectTemplateRepo;
    this.taskTemplateService = taskTemplateService;
    this.projectService = projectService;
    this.appProjectService = appProjectService;
  }

  @Override
  public ProjectTemplate addParentTaskTemplate(ProjectTemplate projectTemplate) {
    Set<TaskTemplate> taskTemplateSet = projectTemplate.getTaskTemplateSet();
    if (ObjectUtils.isEmpty(taskTemplateSet)) {
      return projectTemplate;
    }

    if (projectTemplate.getId() != null) {
      Set<TaskTemplate> oldTaskTemplateSet =
          projectTemplateRepo.find(projectTemplate.getId()).getTaskTemplateSet();
      if (!ObjectUtils.isEmpty(oldTaskTemplateSet)
          && oldTaskTemplateSet.size() > taskTemplateSet.size()) {
        return projectTemplate;
      }
    }

    for (TaskTemplate taskTemplate : new HashSet<>(taskTemplateSet)) {
      taskTemplateSet.addAll(
          taskTemplateService.getParentTaskTemplateFromTaskTemplate(
              taskTemplate.getParentTaskTemplate(), taskTemplateSet));
    }
    return projectTemplate;
  }

  @Override
  public Map<String, Object> createProjectFromTemplateView(ProjectTemplate projectTemplate)
      throws AxelorException {

    if (isWizardNeeded(projectTemplate)) {
      projectTemplate = projectTemplateRepo.find(projectTemplate.getId());
      Project project = projectService.createProjectFromTemplate(projectTemplate, null, null);
      return ActionView.define(I18n.get("Project"))
          .model(Project.class.getName())
          .add("form", "project-form")
          .add("grid", "project-grid")
          .param("search-filters", "project-filters")
          .context("_showRecord", project.getId())
          .map();
    }

    Map<String, Object> contextMap = computeWizardContext(projectTemplate);
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Create project from this template"))
            .model(Wizard.class.getName())
            .add("form", "project-template-wizard-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("width", "large")
            .param("popup-save", "false");

    if (!ObjectUtils.isEmpty(contextMap)) {
      for (Map.Entry<String, Object> contextItem : contextMap.entrySet()) {
        builder.context(contextItem.getKey(), contextItem.getValue());
      }
    }

    return builder.map();
  }

  @Override
  public boolean isWizardNeeded(ProjectTemplate projectTemplate) {
    return projectTemplate != null
        && projectTemplate.getId() != null
        && Optional.ofNullable(appProjectService.getAppProject())
            .map(AppProject::getGenerateProjectSequence)
            .orElse(false);
  }

  @Override
  public Map<String, Object> computeWizardContext(ProjectTemplate projectTemplate) {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("_projectTemplate", projectTemplate);

    return contextMap;
  }
}
