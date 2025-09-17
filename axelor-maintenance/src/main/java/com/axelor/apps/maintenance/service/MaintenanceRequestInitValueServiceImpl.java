package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MaintenanceRequestInitValueServiceImpl implements MaintenanceRequestInitValueService {

  protected final AppBaseService appBaseService;

  @Inject
  public MaintenanceRequestInitValueServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> getDefaultValues(MaintenanceRequest maintenanceRequest) {

    User user = AuthUtils.getUser();

    maintenanceRequest.setAssignedTo(user);
    maintenanceRequest.setRequestBy(user);
    maintenanceRequest.setRequestDate(appBaseService.getTodayDate(null));

    Map<String, Object> values = new HashMap<>();
    values.put("assignedTo", maintenanceRequest.getAssignedTo());
    values.put("requestBy", maintenanceRequest.getRequestBy());
    values.put("requestDate", maintenanceRequest.getRequestDate());
    return values;
  }
}
