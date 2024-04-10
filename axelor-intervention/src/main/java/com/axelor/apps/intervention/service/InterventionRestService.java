package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.rest.dto.InterventionEquipmentPutRequest;
import com.axelor.apps.intervention.rest.dto.InterventionStatusPutRequest;
import com.google.inject.persist.Transactional;

public interface InterventionRestService {
  void updateStatus(InterventionStatusPutRequest request, Intervention intervention)
      throws AxelorException;

  void addEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException;

  @Transactional(rollbackOn = Exception.class)
  void removeEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException;
}
