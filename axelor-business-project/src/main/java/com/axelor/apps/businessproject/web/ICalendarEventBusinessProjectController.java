package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.businessproject.service.ProjectPlanningTimeBusinessProjectService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ICalendarEventBusinessProjectController {

  public void loadLinkedPlanningTime(ActionRequest request, ActionResponse response) {
    ICalendarEvent event = request.getContext().asType(ICalendarEvent.class);

    ProjectPlanningTime projectPlanningTime =
        Beans.get(ProjectPlanningTimeBusinessProjectService.class).loadLinkedPlanningTime(event);

    if (projectPlanningTime != null) {
      response.setAttr("$_linkedProjectPlanningTime", "hidden", false);
      response.setValue("$_linkedProjectPlanningTime", projectPlanningTime);
    }
  }
}
