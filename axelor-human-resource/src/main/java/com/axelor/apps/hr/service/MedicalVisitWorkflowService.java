package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.MedicalVisit;

public interface MedicalVisitWorkflowService {

  void plan(MedicalVisit medicalVisit) throws AxelorException;

  void realize(MedicalVisit medicalVisit) throws AxelorException;

  void cancel(MedicalVisit medicalVisit) throws AxelorException;
}
