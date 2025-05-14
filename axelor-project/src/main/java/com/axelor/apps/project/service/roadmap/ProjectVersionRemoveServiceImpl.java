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
package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.repo.ProjectVersionRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectVersionRemoveServiceImpl implements ProjectVersionRemoveService {

  protected ProjectVersionRepository projectVersionRepository;

  @Inject
  public ProjectVersionRemoveServiceImpl(ProjectVersionRepository projectVersionRepository) {
    this.projectVersionRepository = projectVersionRepository;
  }

  @Override
  public void removeProjectFromRoadmap(Project project) {
    if (ObjectUtils.isEmpty(project.getRoadmapSet())) {
      return;
    }
    List<ProjectVersion> projectVersionList =
        project.getRoadmapSet().stream()
            .sorted(Comparator.comparing(ProjectVersion::getId))
            .collect(Collectors.toList());
    for (ProjectVersion projectVersion : projectVersionList) {
      removeProjectFromVersion(project, projectVersion);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void removeProjectFromVersion(Project project, ProjectVersion projectVersion) {
    project.removeRoadmapSetItem(projectVersion);
    projectVersion.removeProjectSetItem(project);
    if (ObjectUtils.isEmpty(projectVersion.getProjectSet()) && projectVersion.getId() != null) {
      projectVersionRepository.remove(projectVersionRepository.find(projectVersion.getId()));
    }
  }
}
