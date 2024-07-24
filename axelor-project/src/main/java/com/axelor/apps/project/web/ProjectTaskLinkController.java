package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTaskLinkService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Optional;

public class ProjectTaskLinkController {

  @ErrorException
  public void setLinkTypeDomain(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    if (parentContext == null || !ProjectTask.class.equals(parentContext.getContextClass())) {
      return;
    }

    Project project =
        Optional.of(parentContext.asType(ProjectTask.class))
            .map(ProjectTask::getProject)
            .orElse(null);

    String domain = Beans.get(ProjectTaskLinkService.class).getLinkTypeDomain(project);

    response.setAttr("projectTaskLinkType", "domain", domain);
  }

  @ErrorException
  public void setTaskDomain(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    if (parentContext == null || !ProjectTask.class.equals(parentContext.getContextClass())) {
      return;
    }

    ProjectTask projectTask = parentContext.asType(ProjectTask.class);
    String domain = Beans.get(ProjectTaskLinkService.class).getProjectTaskDomain(projectTask);

    response.setAttr("projectTask1", "domain", domain);
    response.setAttr("projectTask2", "domain", domain);
  }
}
