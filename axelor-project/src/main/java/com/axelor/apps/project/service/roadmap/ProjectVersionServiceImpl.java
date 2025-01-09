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
package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.repo.ProjectVersionRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProjectVersionServiceImpl implements ProjectVersionService {

  protected ProjectVersionRepository projectVersionRepository;

  @Inject
  public ProjectVersionServiceImpl(ProjectVersionRepository projectVersionRepository) {
    this.projectVersionRepository = projectVersionRepository;
  }

  @Override
  public String checkIfProjectOrVersionConflicts(ProjectVersion projectVersion) {
    if (CollectionUtils.isEmpty(projectVersion.getProjectSet())) {
      return null;
    }
    List<ProjectVersion> projectVersionList = getProjectVersionList(projectVersion);
    if (CollectionUtils.isEmpty(projectVersionList)) {
      return null;
    }
    Project conflictingProject = findConflictingProject(projectVersion, projectVersionList);
    if (conflictingProject != null) {
      return String.format(
          I18n.get(ProjectExceptionMessage.PROJECT_VERSION_WITH_SAME_PROJECT_ALREADY_EXISTS),
          projectVersion.getTitle(),
          conflictingProject.getName());
    }
    return null;
  }

  protected Project findConflictingProject(
      ProjectVersion projectVersion, List<ProjectVersion> projectVersionList) {
    Project conflictingProject = null;
    Set<Project> projectVersionProjectSet = projectVersion.getProjectSet();

    for (ProjectVersion version : projectVersionList) {
      Set<Project> versionProjectSet = version.getProjectSet();
      if (version.getId().equals(projectVersion.getId())
          || CollectionUtils.isEmpty(versionProjectSet)
          || Collections.disjoint(projectVersionProjectSet, versionProjectSet)) {
        continue;
      }
      conflictingProject =
          versionProjectSet.stream()
              .filter(projectVersionProjectSet::contains)
              .findFirst()
              .orElse(null);
      if (conflictingProject != null) {
        break;
      }
    }
    return conflictingProject;
  }

  @Override
  public List<ProjectVersion> getProjectVersionList(ProjectVersion projectVersion) {
    return projectVersionRepository
        .all()
        .filter("self.title = :title")
        .bind("title", projectVersion.getTitle())
        .fetch();
  }
}
