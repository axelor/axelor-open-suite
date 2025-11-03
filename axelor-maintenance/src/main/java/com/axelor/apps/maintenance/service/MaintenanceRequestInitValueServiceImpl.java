/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
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
