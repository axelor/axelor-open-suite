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
package com.axelor.apps.maintenance.service.batch;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.apps.maintenance.service.PreventiveMaintenanceEligibilityService;
import com.axelor.apps.maintenance.service.PreventiveMaintenanceProcessService;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.apps.production.service.batch.BatchStrategy;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.util.List;

public class BatchGeneratePreventiveMaintenanceRequest extends BatchStrategy {

  protected final PreventiveMaintenanceEligibilityService eligibilityService;
  protected final PreventiveMaintenanceProcessService processService;

  @Inject
  public BatchGeneratePreventiveMaintenanceRequest(
      PreventiveMaintenanceEligibilityService eligibilityService,
      PreventiveMaintenanceProcessService processService) {
    this.eligibilityService = eligibilityService;
    this.processService = processService;
  }

  @Override
  protected void process() {
    ProductionBatch productionBatch = batch.getProductionBatch();

    Query<EquipementMaintenance> query =
        eligibilityService.getEligibleEquipmentQuery(productionBatch);

    List<EquipementMaintenance> equipmentList;
    int offset = 0;

    while (!(equipmentList = query.order("id").fetch(getFetchLimit(), offset)).isEmpty()) {
      for (EquipementMaintenance equipment : equipmentList) {
        ++offset;
        try {
          if (processService.processEquipment(equipment, productionBatch)) {
            incrementDone();
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, "Preventive maintenance request generation", batch.getId());
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Override
  protected void stop() {
    addComment(
        String.format(
            I18n.get(MaintenanceExceptionMessage.BATCH_PREVENTIVE_MAINTENANCE_REPORT),
            batch.getDone(),
            batch.getAnomaly()));
    super.stop();
  }
}
