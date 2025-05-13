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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.Optional;

public class ProjectTaskAttrsServiceImpl implements ProjectTaskAttrsService {

  protected AppBaseService appBaseService;
  protected MetaModelRepository metaModelRepository;

  @Inject
  public ProjectTaskAttrsServiceImpl(
      AppBaseService appBaseService, MetaModelRepository metaModelRepository) {
    this.appBaseService = appBaseService;
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public String getTagDomain(ProjectTask projectTask) {
    Company company =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getCompany)
            .orElse(null);
    String domain =
        String.format(
            "(self.concernedModelSet IS EMPTY OR %s member of self.concernedModelSet)",
            metaModelRepository.findByName("ProjectTask").getId());

    if (company != null) {
      domain =
          domain.concat(
              String.format(
                  " AND (self.companySet IS EMPTY OR %s member of self.companySet)",
                  company.getId()));
    }

    return domain;
  }

  @Override
  public String getActiveSprintDomain(ProjectTask projectTask) {
    String domain = "self.id = 0";

    String sprintManagementSelect =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getSprintManagementSelect)
            .orElse(ProjectRepository.SPRINT_MANAGEMENT_NONE);
    if (StringUtils.isEmpty(sprintManagementSelect)
        || ProjectRepository.SPRINT_MANAGEMENT_NONE.equals(sprintManagementSelect)) {
      return domain;
    }

    if (ProjectRepository.SPRINT_MANAGEMENT_VERSION.equals(sprintManagementSelect)) {
      domain = "(self.targetVersion = :targetVersion";
    } else {
      domain = "(self.project = :project";
    }

    domain =
        domain.concat(
            String.format(
                " AND self.toDate > '%s')",
                appBaseService.getTodayDate(
                    Optional.of(projectTask)
                        .map(ProjectTask::getProject)
                        .map(Project::getCompany)
                        .orElse(null))));

    domain =
        domain.concat(
            String.format(
                " OR self.id = %s",
                Optional.of(projectTask)
                    .map(ProjectTask::getProject)
                    .map(Project::getBacklogSprint)
                    .map(Sprint::getId)
                    .orElse(0L)));

    return domain;
  }
}
