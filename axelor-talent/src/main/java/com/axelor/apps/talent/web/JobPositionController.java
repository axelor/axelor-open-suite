package com.axelor.apps.talent.web;

import com.axelor.apps.talent.db.JobPosition;
import com.axelor.apps.talent.db.repo.JobPositionRepository;
import com.axelor.apps.talent.service.JobPositionWorkflowService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class JobPositionController {
  public void backToDraft(ActionRequest request, ActionResponse response) {
    try {
      JobPosition jobPosition = request.getContext().asType(JobPosition.class);

      jobPosition = Beans.get(JobPositionRepository.class).find(jobPosition.getId());

      Beans.get(JobPositionWorkflowService.class).backToDraft(jobPosition);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void open(ActionRequest request, ActionResponse response) {
    try {
      JobPosition jobPosition = request.getContext().asType(JobPosition.class);

      jobPosition = Beans.get(JobPositionRepository.class).find(jobPosition.getId());

      Beans.get(JobPositionWorkflowService.class).open(jobPosition);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void pause(ActionRequest request, ActionResponse response) {
    try {
      JobPosition jobPosition = request.getContext().asType(JobPosition.class);

      jobPosition = Beans.get(JobPositionRepository.class).find(jobPosition.getId());

      Beans.get(JobPositionWorkflowService.class).pause(jobPosition);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void close(ActionRequest request, ActionResponse response) {
    try {
      JobPosition jobPosition = request.getContext().asType(JobPosition.class);

      jobPosition = Beans.get(JobPositionRepository.class).find(jobPosition.getId());

      Beans.get(JobPositionWorkflowService.class).close(jobPosition);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      JobPosition jobPosition = request.getContext().asType(JobPosition.class);

      jobPosition = Beans.get(JobPositionRepository.class).find(jobPosition.getId());

      Beans.get(JobPositionWorkflowService.class).cancel(jobPosition);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
