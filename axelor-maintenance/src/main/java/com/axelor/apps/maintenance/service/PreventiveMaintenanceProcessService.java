package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.production.db.ProductionBatch;

public interface PreventiveMaintenanceProcessService {

  /**
   * Process a single equipment for preventive maintenance.
   *
   * <p>Evaluates criteria, creates a maintenance request if triggered, and updates equipment fields
   * (nextMtnDate, lastMtnOperatingHoursRef).
   *
   * @param equipment the equipment to process
   * @param productionBatch the batch configuration
   * @return true if a maintenance request was created
   * @throws AxelorException if processing fails
   */
  boolean processEquipment(EquipementMaintenance equipment, ProductionBatch productionBatch)
      throws AxelorException;
}