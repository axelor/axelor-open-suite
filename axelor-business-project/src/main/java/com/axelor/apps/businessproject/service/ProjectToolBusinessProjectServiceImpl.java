package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectToolServiceImpl;
import com.google.inject.Inject;
import java.util.Objects;

public class ProjectToolBusinessProjectServiceImpl extends ProjectToolServiceImpl {

  @Inject
  public ProjectToolBusinessProjectServiceImpl() {}

  @Override
  public String getProjectFormName(Project project) {
    if (Objects.isNull(project) || !project.getIsBusinessProject()) {
      return super.getProjectFormName(project);
    }

    return "business-project-form";
  }

  @Override
  public String getProjectGridName(Project project) {
    if (Objects.isNull(project) || !project.getIsBusinessProject()) {
      return super.getProjectGridName(project);
    }

    return "business-project-grid";
  }
}
