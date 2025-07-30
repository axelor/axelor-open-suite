package com.axelor.apps.maintenance.service;

import com.axelor.apps.maintenance.db.MaintenanceRequest;
import java.util.Map;

public interface MaintenanceRequestInitValueService {
  Map<String, Object> getDefaultValues(MaintenanceRequest maintenanceRequest);
}
