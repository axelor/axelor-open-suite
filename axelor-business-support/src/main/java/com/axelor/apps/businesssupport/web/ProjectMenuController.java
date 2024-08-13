package com.axelor.apps.businesssupport.web;

import com.axelor.apps.businesssupport.service.ProjectMenuBusinessSupportService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectMenuController {

  public void myOpenProjectTickets(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuBusinessSupportService.class).getAllOpenProjectTickets());
  }
}
