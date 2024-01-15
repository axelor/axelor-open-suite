package com.axelor.apps.hr.web;

import com.axelor.apps.hr.service.SchedulerCreationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class AppLeaveController {
  public void openSchedulerCreationWizard(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    int firstLeaveDayPeriod = (int) context.get("firstLeaveDayPeriod");
    int firstLeaveMonthPeriod = (int) context.get("firstLeaveMonthPeriod");
    response.setView(
        Beans.get(SchedulerCreationService.class)
            .openWizard(firstLeaveDayPeriod, firstLeaveMonthPeriod)
            .map());
  }
}
