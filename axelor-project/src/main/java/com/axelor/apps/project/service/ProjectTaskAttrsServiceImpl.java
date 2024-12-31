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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.TagService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import java.util.Optional;

public class ProjectTaskAttrsServiceImpl implements ProjectTaskAttrsService {
  protected TagService tagService;

  @Inject
  public ProjectTaskAttrsServiceImpl(TagService tagService) {
    this.tagService = tagService;
  }

  @Override
  public String getTagDomain(ProjectTask projectTask) {
    Company company =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getCompany)
            .orElse(null);

    return tagService.getTagDomain("ProjectTask", company);
  }
}
