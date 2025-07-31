package com.axelor.apps.maintenance.rest.dto;

import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.utils.api.ResponseStructure;

public class MaintenanceRequestResponse extends ResponseStructure {

  protected Long maintenanceRequestId;

  public MaintenanceRequestResponse(MaintenanceRequest maintenanceRequest) {
    super(maintenanceRequest.getVersion());
    this.maintenanceRequestId = maintenanceRequest.getId();
  }

  public Long getMaintenanceRequestId() {
    return maintenanceRequestId;
  }
}
