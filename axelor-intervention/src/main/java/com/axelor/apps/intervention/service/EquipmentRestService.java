package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.rest.dto.EquipmentPicturePutRequest;

public interface EquipmentRestService {

  void addPicture(EquipmentPicturePutRequest request, Equipment equipment) throws AxelorException;

  void removePicture(EquipmentPicturePutRequest request, Equipment equipment)
      throws AxelorException;
}
