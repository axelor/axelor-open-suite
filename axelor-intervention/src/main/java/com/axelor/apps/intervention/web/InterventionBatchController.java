package com.axelor.apps.intervention.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.InterventionBatch;
import com.axelor.apps.intervention.db.repo.InterventionBatchRepository;
import com.axelor.apps.intervention.service.batch.InterventionBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InterventionBatchController {

  public void generateInterventions(ActionRequest request, ActionResponse response) {
    try {
      InterventionBatch interventionBatch = request.getContext().asType(InterventionBatch.class);
      interventionBatch =
          Beans.get(InterventionBatchRepository.class).find(interventionBatch.getId());

      InterventionBatchService interventionBatchService = Beans.get(InterventionBatchService.class);
      interventionBatchService.setBatchModel(interventionBatch);

      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();
      Batch batch = controllerCallableTool.runInSeparateThread(interventionBatchService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
