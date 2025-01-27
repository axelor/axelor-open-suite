package com.axelor.apps.businessproduction.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproduction.service.SaleOrderBusinessProductionSyncService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectBusinessProductionController {
  public void solListOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    Beans.get(SaleOrderBusinessProductionSyncService.class).projectSoListOnChange(project);
    response.setValues(project);
  }
}
