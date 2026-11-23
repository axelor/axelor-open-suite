/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.production.db.ProductionBatch;

public interface PreventiveMaintenanceProcessService {

  /**
   * Process a single equipment for preventive maintenance.
   *
   * <p>Evaluates criteria and creates a maintenance request if triggered. The equipment is updated
   * when the request is completed, see {@link #updateEquipmentOnCompletion}.
   *
   * @param equipment the equipment to process
   * @param productionBatch the batch configuration
   * @return true if a maintenance request was created
   * @throws AxelorException if processing fails
   */
  boolean processEquipment(EquipementMaintenance equipment, ProductionBatch productionBatch)
      throws AxelorException;

  /**
   * Update the equipment of a completed preventive maintenance request: next maintenance date is
   * the completion date plus the calendar cadence, and the operating hours reference is the current
   * machine operating hours.
   *
   * @param maintenanceRequest the completed request
   */
  void updateEquipmentOnCompletion(MaintenanceRequest maintenanceRequest);
}
