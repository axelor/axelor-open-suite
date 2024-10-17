package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.UserProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class UserController {

  public void setActiveProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      Beans.get(UserProjectService.class).setActiveProject(AuthUtils.getUser(), project);
      response.setNotify(
          String.format(I18n.get("Active project changed to %s"), project.getName()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
