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
package com.axelor.apps.maintenance.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.rest.dto.MaintenanceRequestPostRequest;
import com.axelor.apps.maintenance.rest.dto.MaintenanceRequestResponse;
import com.axelor.apps.maintenance.service.MaintenanceRequestCreateService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/maintenance-request")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MaintenanceRequestRestController {

  @Operation(
      summary = "Create a maintenance request",
      tags = {"Maintenance request"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createMaintenanceRequest(MaintenanceRequestPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(MaintenanceRequest.class)
        .writeAccess(MaintenanceRequest.class)
        .readAccess(EquipementMaintenance.class)
        .check();

    MaintenanceRequest maintenanceRequest =
        Beans.get(MaintenanceRequestCreateService.class)
            .createMaintenanceRequest(
                requestBody.fetchEquipmentMaintenance(),
                requestBody.getExpectedDate(),
                requestBody.getActionSelect());

    return ResponseConstructor.buildCreateResponse(
        maintenanceRequest, new MaintenanceRequestResponse(maintenanceRequest));
  }
}
