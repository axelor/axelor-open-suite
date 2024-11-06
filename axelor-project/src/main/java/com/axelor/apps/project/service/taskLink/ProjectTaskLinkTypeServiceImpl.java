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
package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeRepository;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectTaskLinkTypeServiceImpl implements ProjectTaskLinkTypeService {

  protected ProjectTaskLinkTypeRepository projectTaskLinkTypeRepository;

  @Inject
  public ProjectTaskLinkTypeServiceImpl(
      ProjectTaskLinkTypeRepository projectTaskLinkTypeRepository) {
    this.projectTaskLinkTypeRepository = projectTaskLinkTypeRepository;
  }

  @Override
  public void generateOppositeLinkType(ProjectTaskLinkType projectTaskLinkType, String name) {
    if (projectTaskLinkType == null || StringUtils.isEmpty(name)) {
      return;
    }

    ProjectTaskLinkType opposite = new ProjectTaskLinkType();
    opposite.setName(name);

    manageOppositeLinkTypes(projectTaskLinkType, opposite);
  }

  @Override
  public void selectOppositeLinkType(
      ProjectTaskLinkType projectTaskLinkType, ProjectTaskLinkType opposite) {
    if (projectTaskLinkType == null || opposite == null) {
      return;
    }

    manageOppositeLinkTypes(projectTaskLinkType, opposite);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void manageOppositeLinkTypes(
      ProjectTaskLinkType projectTaskLinkType, ProjectTaskLinkType opposite) {
    opposite.setOppositeLinkType(projectTaskLinkType);
    opposite = projectTaskLinkTypeRepository.save(opposite);

    emptyOppositeLinkType(projectTaskLinkType);

    projectTaskLinkType.setOppositeLinkType(opposite);
    projectTaskLinkTypeRepository.save(projectTaskLinkType);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void emptyOppositeLinkType(ProjectTaskLinkType projectTaskLinkType) {
    if (projectTaskLinkType == null || projectTaskLinkType.getOppositeLinkType() == null) {
      return;
    }

    ProjectTaskLinkType opposite = projectTaskLinkType.getOppositeLinkType();
    opposite.setOppositeLinkType(null);
    projectTaskLinkTypeRepository.save(opposite);
    projectTaskLinkType.setOppositeLinkType(null);
    projectTaskLinkTypeRepository.save(projectTaskLinkType);
  }
}
