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
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectVersionServiceImpl implements ProjectVersionService {

  protected ProjectVersionRepository projectVersionRepository;

  @Inject
  public ProjectVersionServiceImpl(ProjectVersionRepository projectVersionRepository) {
    this.projectVersionRepository = projectVersionRepository;
  }

  @Override
  public String checkIfProjectOrVersionConflicts(ProjectVersion projectVersion, Project project) {
    Set<Project> projectSet =
        ObjectUtils.notEmpty(projectVersion.getProjectSet())
            ? projectVersion.getProjectSet()
            : new HashSet<>();
    projectSet.add(project);

    Set<ProjectVersion> projectVersionSet = getProjectVersionSet(project, projectVersion);

    if (CollectionUtils.isEmpty(projectVersionSet) || CollectionUtils.isEmpty(projectSet)) {
      return null;
    }
    String conflictingProjects =
        findConflictingProject(projectVersion.getId(), projectSet, projectVersionSet);
    if (StringUtils.notEmpty(conflictingProjects)) {
      return String.format(
          I18n.get(ProjectExceptionMessage.PROJECT_VERSION_WITH_SAME_PROJECT_ALREADY_EXISTS),
          projectVersion.getTitle(),
          conflictingProjects);
    }
    return "";
  }

  protected Set<ProjectVersion> getProjectVersionSet(
      Project project, ProjectVersion projectVersion) {
    Set<ProjectVersion> projectVersionSet =
        new HashSet<>(projectVersionRepository.findByTitle(projectVersion.getTitle()).fetch());

    if (project == null) {
      return projectVersionSet;
    }

    projectVersionSet.stream()
        .filter(version -> version.getProjectSet().contains(project))
        .collect(Collectors.toSet())
        .forEach(projectVersionSet::remove);
    if (ObjectUtils.isEmpty(project.getRoadmapSet())) {
      return projectVersionSet;
    }

    Set<ProjectVersion> projectProjectVersionSet =
        project.getRoadmapSet().stream()
            .filter(version -> Objects.equals(version.getTitle(), projectVersion.getTitle()))
            .collect(Collectors.toSet());
    projectVersionSet.addAll(projectProjectVersionSet);
    return projectVersionSet;
  }

  protected String findConflictingProject(
      Long versionId, Set<Project> projectSet, Set<ProjectVersion> projectVersionSet) {
    StringJoiner stringJoiner = new StringJoiner(", ");

    for (ProjectVersion version : projectVersionSet) {
      Set<Project> versionProjectSet = version.getProjectSet();
      if (version.getId() != null && version.getId().equals(versionId)
          || CollectionUtils.isEmpty(versionProjectSet)
          || Collections.disjoint(projectSet, versionProjectSet)) {
        continue;
      }
      stringJoiner.add(
          versionProjectSet.stream()
              .filter(projectSet::contains)
              .findFirst()
              .map(Project::getCode)
              .orElse(null));
    }
    return stringJoiner.toString();
  }
}
