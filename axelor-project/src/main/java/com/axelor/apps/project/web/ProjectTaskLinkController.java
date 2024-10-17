package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectTaskLinkController {

  @ErrorException
  public void removeLink(ActionRequest request, ActionResponse response) {
    ProjectTaskLink projectTaskLink = request.getContext().asType(ProjectTaskLink.class);
    Beans.get(ProjectTaskLinkService.class).removeLink(projectTaskLink);

    response.setReload(true);
  }
}
