package com.axelor.apps.project.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.repo.ProjectBatchRepository;
import com.axelor.apps.project.service.batch.ProjectBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      ProjectBatch projectBatch = request.getContext().asType(ProjectBatch.class);

      projectBatch = Beans.get(ProjectBatchRepository.class).find(projectBatch.getId());
      ProjectBatchService projectBatchService = Beans.get(ProjectBatchService.class);
      projectBatchService.setBatchModel(projectBatch);
      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();
      Batch batch = controllerCallableTool.runInSeparateThread(projectBatchService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
