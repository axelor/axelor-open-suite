/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.maintenance.service.MaintenanceRequestService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MaintenanceRequestController {

  @Inject private MaintenanceRequestRepository maintenanceRequestRepo;

  @Inject private MaintenanceRequestService maintenanceRequestService;

  public void start(ActionRequest request, ActionResponse response) {

    MaintenanceRequest maintenanceRequest = request.getContext().asType(MaintenanceRequest.class);

    if (maintenanceRequest.getId() != null) {
      maintenanceRequest = maintenanceRequestRepo.find(maintenanceRequest.getId());
      maintenanceRequestService.start(maintenanceRequest);
      response.setReload(true);
    }
  }

  public void complete(ActionRequest request, ActionResponse response) {

    MaintenanceRequest maintenanceRequest = request.getContext().asType(MaintenanceRequest.class);

    if (maintenanceRequest.getId() != null) {
      maintenanceRequest = maintenanceRequestRepo.find(maintenanceRequest.getId());
      maintenanceRequestService.complete(maintenanceRequest);
      response.setReload(true);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {

    MaintenanceRequest maintenanceRequest = request.getContext().asType(MaintenanceRequest.class);

    if (maintenanceRequest.getId() != null) {
      maintenanceRequest = maintenanceRequestRepo.find(maintenanceRequest.getId());
      maintenanceRequestService.cancel(maintenanceRequest);
      response.setReload(true);
    }
  }

  public void replan(ActionRequest request, ActionResponse response) {

    MaintenanceRequest maintenanceRequest = request.getContext().asType(MaintenanceRequest.class);

    if (maintenanceRequest.getId() != null) {
      maintenanceRequest = maintenanceRequestRepo.find(maintenanceRequest.getId());
      maintenanceRequestService.replan(maintenanceRequest);
      response.setReload(true);
    }
  }
}
