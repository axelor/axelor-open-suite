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
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TimesheetContextProjectServiceImpl implements TimesheetContextProjectService {

  protected ProjectRepository projectRepository;
  protected ProjectToolService projectToolService;

  @Inject
  public TimesheetContextProjectServiceImpl(
      ProjectRepository projectRepository, ProjectToolService projectToolService) {
    this.projectRepository = projectRepository;
    this.projectToolService = projectToolService;
  }

  @Override
  public Set<Long> getActiveProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project contextProject =
        Optional.ofNullable(currentUser).map(User::getActiveProject).orElse(null);
    Set<Long> projectIdsSet = new HashSet<>();
    if (contextProject == null) {
      List<Project> allTimeSpentProjectList =
          projectRepository.all().filter("self.manageTimeSpent = true").fetch();
      for (Project timeSpentProject : allTimeSpentProjectList) {
        projectToolService.getChildProjectIds(projectIdsSet, timeSpentProject);
      }
    } else {
      if (!currentUser.getIsIncludeSubProjects()) {
        projectIdsSet.add(contextProject.getId());
        return projectIdsSet;
      }
      projectToolService.getChildProjectIds(projectIdsSet, contextProject);
    }
    return projectIdsSet;
  }
}
