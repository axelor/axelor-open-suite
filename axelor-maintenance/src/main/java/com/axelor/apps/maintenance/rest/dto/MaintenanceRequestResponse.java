package com.axelor.apps.maintenance.rest.dto;

import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.utils.api.ResponseStructure;

public class MaintenanceRequestResponse extends ResponseStructure {

  protected Long maintenanRequestId;

  public MaintenanceRequestResponse(MaintenanceRequest maintenanceRequest) {
    super(maintenanceRequest.getVersion());
    this.maintenanRequestId = maintenanceRequest.getId();
  }

  public Long getMaintenanRequestId() {
    return maintenanRequestId;
  }
}
