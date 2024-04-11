package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.rest.dto.InterventionEquipmentPutRequest;
import com.axelor.apps.intervention.rest.dto.InterventionStatusPutRequest;

public interface InterventionRestService {
  void updateStatus(InterventionStatusPutRequest request, Intervention intervention)
      throws AxelorException;

  Intervention addEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException;

  Intervention removeEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException;

  void updateSurvey(Intervention intervention);
}
