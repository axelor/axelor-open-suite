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

import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class ProjectTemplateServiceImpl implements ProjectTemplateService {

  protected ProjectTemplateRepository projectTemplateRepo;
  protected TaskTemplateService taskTemplateService;

  @Inject
  public ProjectTemplateServiceImpl(
      ProjectTemplateRepository projectTemplateRepo, TaskTemplateService taskTemplateService) {
    this.projectTemplateRepo = projectTemplateRepo;
    this.taskTemplateService = taskTemplateService;
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
}
