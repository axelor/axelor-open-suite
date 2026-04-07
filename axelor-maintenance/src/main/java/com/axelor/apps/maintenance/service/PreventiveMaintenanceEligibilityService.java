package com.axelor.apps.maintenance.service;

import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.db.Query;

public interface PreventiveMaintenanceEligibilityService {

  /**
   * Build a query returning all equipment eligible for preventive maintenance evaluation.
   *
   * <p>Eligible means: (mtnEachDay > 0) OR (mtnEachDuration > 0 AND machine is not null), AND no
   * existing planned/in-progress preventive request exists.
   *
   * @param productionBatch the batch configuration
   * @return query of eligible equipment
   */
  Query<EquipementMaintenance> getEligibleEquipmentQuery(ProductionBatch productionBatch);
}