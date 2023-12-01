package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;

public interface EquipmentService {

  void removeEquipment(Equipment equipment) throws AxelorException;
}
