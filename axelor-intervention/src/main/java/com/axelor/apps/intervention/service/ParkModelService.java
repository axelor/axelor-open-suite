package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.ParkModel;
import java.util.List;
import java.util.Map;

public interface ParkModelService {
  List<Map<String, Object>> getEquipmentList(ParkModel parkModel);
}
