package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.ProjectPlanningTimeBusinessProjectService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectPlanningTimeBusinessProjectController {

  public void updateEvent(ActionRequest request, ActionResponse response) {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    Beans.get(ProjectPlanningTimeBusinessProjectService.class)
        .updateLinkedEvent(projectPlanningTime);
  }
}
