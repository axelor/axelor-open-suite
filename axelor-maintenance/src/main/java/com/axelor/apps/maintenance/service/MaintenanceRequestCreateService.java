package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import java.time.LocalDate;

public interface MaintenanceRequestCreateService {

  MaintenanceRequest createMaintenanceRequest(
      EquipementMaintenance equipementMaintenance, LocalDate expectedDate, int actionSelect)
      throws AxelorException;
}
