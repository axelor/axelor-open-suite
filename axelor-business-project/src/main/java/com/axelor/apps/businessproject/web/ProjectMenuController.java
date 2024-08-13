package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.ProjectMenuBusinessService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectMenuController {
  public void myBusinessProjects(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuBusinessService.class).getMyBusinessProjects());
  }

  public void myInvoicingProjects(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuBusinessService.class).getMyInvoicingProjects());
  }
}
