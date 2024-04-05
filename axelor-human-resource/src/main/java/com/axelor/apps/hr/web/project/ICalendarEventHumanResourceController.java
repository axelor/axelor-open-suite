package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ICalendarEventHumanResourceController {

    public void loadLinkedPlanningTime(ActionRequest request, ActionResponse response) {
        ICalendarEvent event = request.getContext().asType(ICalendarEvent.class);

        ProjectPlanningTime projectPlanningTime =
                Beans.get(ProjectPlanningTimeService.class).loadLinkedPlanningTime(event);

        if (projectPlanningTime != null) {
            response.setAttr("$_linkedProjectPlanningTime", "hidden", false);
            response.setValue("$_linkedProjectPlanningTime", projectPlanningTime);
        }
    }
}
