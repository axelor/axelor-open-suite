package com.axelor.apps.intervention.web;

import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AppInterventionController {

  public void getProcessedFields(ActionRequest request, ActionResponse response) {
    response.setValue("$_xProcessedFields", Beans.get(EquipmentService.class).getProcessedFields());
  }
}
