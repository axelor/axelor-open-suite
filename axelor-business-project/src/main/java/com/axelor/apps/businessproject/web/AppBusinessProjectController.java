package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AppBusinessProjectController {

  public void generateBusinessProjectConfigurations(
      ActionRequest request, ActionResponse response) {

    Beans.get(AppBusinessProjectService.class).generateBusinessProjectConfigurations();

    response.setReload(true);
  }
}
