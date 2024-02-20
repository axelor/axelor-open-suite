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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimesheetContextProjectServiceImpl implements TimesheetContextProjectService {

  protected ProjectRepository projectRepository;
  protected ProjectService projectService;

  @Inject
  public TimesheetContextProjectServiceImpl(
      ProjectRepository projectRepository, ProjectService projectService) {
    this.projectRepository = projectRepository;
    this.projectService = projectService;
  }

  @Override
  public Set<Long> getContextProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project contextProject = currentUser.getContextProject();
    Set<Long> projectIdsSet = new HashSet<>();
    if (contextProject == null) {
      List<Project> allTimeSpentProjectList =
          projectRepository.all().filter("self.isShowTimeSpent = true").fetch();
      for (Project timeSpentProject : allTimeSpentProjectList) {
        projectService.getChildProjectIds(projectIdsSet, timeSpentProject);
      }
    } else {
      if (!currentUser.getIsIncludeSubContextProjects()) {
        projectIdsSet.add(contextProject.getId());
        return projectIdsSet;
      }
      projectService.getChildProjectIds(projectIdsSet, contextProject);
    }
    return projectIdsSet;
  }
}
