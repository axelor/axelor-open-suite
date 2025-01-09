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

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.ProjectCheckListTemplate;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectCheckListItemRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class ProjectCheckListTemplateServiceImpl implements ProjectCheckListTemplateService {

  protected ProjectCheckListItemRepository projectCheckListItemRepository;
  protected ProjectRepository projectRepository;

  @Inject
  public ProjectCheckListTemplateServiceImpl(
      ProjectCheckListItemRepository projectCheckListItemRepository,
      ProjectRepository projectRepository) {
    this.projectCheckListItemRepository = projectCheckListItemRepository;
    this.projectRepository = projectRepository;
  }

  @Override
  public void generateCheckListItemsFromTemplate(
      Project project, ProjectCheckListTemplate template) {
    if (project == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectCheckListItemList())) {
      return;
    }

    project.clearProjectCheckListItemList();

    for (ProjectCheckListItem item : template.getProjectCheckListItemList()) {
      ProjectCheckListItem copy = projectCheckListItemRepository.copy(item, true);
      copy.setProjectCheckListTemplate(null);
      project.addProjectCheckListItemListItem(copy);
    }
  }

  @Override
  public void generateCheckListItemsFromTemplate(
      ProjectTask projectTask, ProjectCheckListTemplate template) {
    if (projectTask == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectCheckListItemList())) {
      return;
    }

    projectTask.clearProjectCheckListItemList();

    for (ProjectCheckListItem item : template.getProjectCheckListItemList()) {
      ProjectCheckListItem copy = projectCheckListItemRepository.copy(item, true);
      copy.setProjectCheckListTemplate(null);
      projectTask.addProjectCheckListItemListItem(copy);
    }
  }
}
