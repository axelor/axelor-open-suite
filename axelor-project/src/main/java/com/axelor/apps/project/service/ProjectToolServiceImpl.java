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

import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProjectToolServiceImpl implements ProjectToolService {

  @Inject
  public ProjectToolServiceImpl() {}

  @Override
  public void getChildProjectIds(Set<Long> projectIdsSet, Project project) {
    if (projectIdsSet.contains(project.getId())) {
      return;
    }

    projectIdsSet.add(project.getId());
    if (!ObjectUtils.isEmpty(project.getChildProjectList())) {
      for (Project childProject : project.getChildProjectList()) {
        getChildProjectIds(projectIdsSet, childProject);
      }
    }
  }

  @Override
  public Set<Long> getActiveProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project activateProject =
        Optional.ofNullable(currentUser).map(User::getActiveProject).orElse(null);

    return getRelatedProjectIds(activateProject);
  }

  @Override
  public Set<Long> getRelatedProjectIds(Project project) {
    User currentUser = AuthUtils.getUser();

    Set<Long> projectIdsSet = new HashSet<>();
    if (project == null) {
      projectIdsSet.add(0l);
      return projectIdsSet;
    }
    if (currentUser != null && !currentUser.getIsIncludeSubProjects()) {
      projectIdsSet.add(project.getId());
      return projectIdsSet;
    }
    this.getChildProjectIds(projectIdsSet, project);
    return projectIdsSet;
  }

  @Override
  public String getProjectFormName(Project project) {
    return "project-form";
  }

  @Override
  public String getProjectGridName(Project project) {
    return "project-grid";
  }
}
