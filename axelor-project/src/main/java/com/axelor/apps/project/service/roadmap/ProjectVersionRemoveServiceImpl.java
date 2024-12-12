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
