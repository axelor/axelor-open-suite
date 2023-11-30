package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.nio.file.Path;

public interface EquipmentService {

  void removeEquipment(Equipment equipment) throws AxelorException;

  Path importEquipments(Long partnerId, MetaFile metaFile) throws AxelorException, IOException;

  public MetaFile loadFormatFile();

  String getProcessedFields();
}
