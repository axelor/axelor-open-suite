package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.AppTimesheet;
import com.axelor.apps.hr.service.app.AppTimesheetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AppTimesheetController {
  public void switchTimesheetEditors(ActionRequest request, ActionResponse response) {
    try {
      AppTimesheet appTimesheet = request.getContext().asType(AppTimesheet.class);
      Beans.get(AppTimesheetService.class)
          .switchTimesheetEditors(appTimesheet.getTimesheetEditor());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
