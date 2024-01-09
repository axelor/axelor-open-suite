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
