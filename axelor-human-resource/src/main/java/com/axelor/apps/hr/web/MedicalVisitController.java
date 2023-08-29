package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.MedicalVisit;
import com.axelor.apps.hr.db.repo.MedicalVisitRepository;
import com.axelor.apps.hr.service.MedicalVisitWorkflowService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MedicalVisitController {

  public void plan(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).plan(medicalVisit);
    response.setReload(true);
  }

  public void realize(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).realize(medicalVisit);
    response.setReload(true);
  }

  public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).cancel(medicalVisit);
    response.setReload(true);
  }
}
