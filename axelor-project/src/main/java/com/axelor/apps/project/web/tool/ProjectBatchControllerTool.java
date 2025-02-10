package com.axelor.apps.project.web.tool;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.service.batch.ProjectBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;

public class ProjectBatchControllerTool {

  public static void runBatch(ProjectBatch projectBatch, ActionResponse response) {
    try {
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
