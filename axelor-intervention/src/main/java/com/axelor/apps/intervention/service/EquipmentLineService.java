package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.util.List;

public interface EquipmentLineService {

  TrackingNumber createTrackingNumber(EquipmentLine equipmentLine) throws AxelorException;

  void changeEquipment(List<EquipmentLine> equipmentLineList, Equipment equipment);
}
