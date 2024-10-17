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
}
