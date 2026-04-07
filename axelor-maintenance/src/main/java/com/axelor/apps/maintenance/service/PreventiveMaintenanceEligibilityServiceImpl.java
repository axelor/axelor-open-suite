package com.axelor.apps.maintenance.service;

import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.db.Query;
import com.google.inject.Inject;


public class PreventiveMaintenanceEligibilityServiceImpl
    implements PreventiveMaintenanceEligibilityService {

  protected final EquipementMaintenanceRepository equipementMaintenanceRepository;

  @Inject
  public PreventiveMaintenanceEligibilityServiceImpl(
      EquipementMaintenanceRepository equipementMaintenanceRepository) {
    this.equipementMaintenanceRepository = equipementMaintenanceRepository;
  }

  @Override
  public Query<EquipementMaintenance> getEligibleEquipmentQuery(ProductionBatch productionBatch) {
    String filter =
        "(self.mtnEachDay > 0 OR (self.mtnEachDuration > 0 AND self.machine IS NOT NULL))"
            + " AND NOT EXISTS ("
            + "   SELECT mr FROM MaintenanceRequest mr"
            + "   WHERE mr.equipementMaintenance = self"
            + "   AND mr.actionSelect = :preventive"
            + "   AND mr.statusSelect IN (:planned, :inProgress)"
            + " )";

    return equipementMaintenanceRepository
        .all()
        .filter(filter)
        .bind("preventive", MaintenanceRequestRepository.ACTION_PREVENTIVE)
        .bind("planned", MaintenanceRequestRepository.STATUS_PLANNED)
        .bind("inProgress", MaintenanceRequestRepository.STATUS_IN_PROGRESS);
  }
}