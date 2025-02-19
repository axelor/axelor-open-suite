package com.axelor.apps.project.web;

import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.repo.ProjectBatchRepository;
import com.axelor.apps.project.web.tool.ProjectBatchControllerTool;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    ProjectBatch projectBatch = request.getContext().asType(ProjectBatch.class);
    projectBatch = Beans.get(ProjectBatchRepository.class).find(projectBatch.getId());

    ProjectBatchControllerTool.runBatch(projectBatch, response);
  }
}
