package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.HashSet;
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

    for (Project childProject : project.getChildProjectList()) {
      getChildProjectIds(projectIdsSet, childProject);
    }
  }

  @Override
  public Set<Long> getActiveProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project activateProject = currentUser.getActiveProject();
    Set<Long> projectIdsSet = new HashSet<>();
    if (activateProject == null) {
      projectIdsSet.add(0l);
      return projectIdsSet;
    }
    if (!currentUser.getIsIncludeSubContextProjects()) {
      projectIdsSet.add(activateProject.getId());
      return projectIdsSet;
    }
    this.getChildProjectIds(projectIdsSet, activateProject);
    return projectIdsSet;
  }
}
