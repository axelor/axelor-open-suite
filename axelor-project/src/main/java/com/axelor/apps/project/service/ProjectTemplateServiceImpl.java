/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.module.ProjectModule;
import com.axelor.common.ObjectUtils;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

@Alternative
@Priority(ProjectModule.PRIORITY)
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
