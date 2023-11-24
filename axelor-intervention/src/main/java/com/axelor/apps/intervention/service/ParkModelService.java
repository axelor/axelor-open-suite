package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.ParkModel;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ParkModelService {
  List<Map<String, Object>> getEquipmentList(ParkModel parkModel);

  @Transactional(rollbackOn = Exception.class)
  List<Long> generateEquipments(
      ParkModel parkModel,
      Partner partner,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Contract contract,
      Map<Long, Integer> quantitiesMap);
}
