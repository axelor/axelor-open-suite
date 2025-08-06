package com.axelor.apps.maintenance.rest.dto;

import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class MaintenanceRequestPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long equipementMaintenanceId;

  @NotNull private LocalDate expectedDate;

  @Min(MaintenanceRequestRepository.ACTION_CORRECTIVE)
  @Max(MaintenanceRequestRepository.ACTION_PREVENTIVE)
  private int actionSelect;

  public Long getEquipementMaintenanceId() {
    return equipementMaintenanceId;
  }

  public void setEquipementMaintenanceId(Long equipementMaintenanceId) {
    this.equipementMaintenanceId = equipementMaintenanceId;
  }

  public LocalDate getExpectedDate() {
    return expectedDate;
  }

  public void setExpectedDate(LocalDate expectedDate) {
    this.expectedDate = expectedDate;
  }

  public int getActionSelect() {
    return actionSelect;
  }

  public void setActionSelect(int actionSelect) {
    this.actionSelect = actionSelect;
  }

  public EquipementMaintenance fetchEquipmentMaintenance() {
    if (equipementMaintenanceId == null || equipementMaintenanceId == 0L) {
      return null;
    }
    return ObjectFinder.find(
        EquipementMaintenance.class, equipementMaintenanceId, ObjectFinder.NO_VERSION);
  }
}
