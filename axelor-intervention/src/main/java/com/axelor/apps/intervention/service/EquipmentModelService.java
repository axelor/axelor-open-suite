package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.Equipment;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EquipmentModelService {

  List<Long> generate(
      Long parentId,
      List<Long> modelIds,
      Map<Long, Integer> quantitiesMap,
      Long partnerId,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Long contractId);

  Equipment generate(
      Long parentId,
      Long modelId,
      Long partnerId,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Long contractId);
}
