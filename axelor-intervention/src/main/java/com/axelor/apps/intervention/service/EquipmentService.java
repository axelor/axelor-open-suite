package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface EquipmentService {

  void removeEquipment(Equipment equipment) throws AxelorException;

  public MetaFile loadFormatFile();

  @Transactional(rollbackOn = Exception.class)
  void createAndRealizeStockMovesForEquipmentLines(List<EquipmentLine> equipmentLines)
      throws AxelorException;
}
