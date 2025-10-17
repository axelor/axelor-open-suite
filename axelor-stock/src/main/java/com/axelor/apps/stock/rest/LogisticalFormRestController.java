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
package com.axelor.apps.stock.rest;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.dto.LogisticalFormPostRequest;
import com.axelor.apps.stock.rest.dto.LogisticalFormResponse;
import com.axelor.apps.stock.service.LogisticalFormCreateService;
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

@Path("/aos/logistical-form")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LogisticalFormRestController {

  @Operation(
      summary = "Create logistical form",
      tags = {"Logistical form"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createLogisticalForm(LogisticalFormPostRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(LogisticalForm.class)
        .readAccess(StockLocation.class)
        .readAccess(Partner.class)
        .check();

    LogisticalForm logisticalForm =
        Beans.get(LogisticalFormCreateService.class)
            .createLogisticalForm(
                requestBody.fetchCarrierPartner(),
                requestBody.fetchDeliverToCustomerPartner(),
                requestBody.fetchStockLocation(),
                requestBody.getCollectionDate(),
                requestBody.getInternalDeliveryComment(),
                requestBody.getExternalDeliveryComment());

    return ResponseConstructor.buildCreateResponse(
        logisticalForm, new LogisticalFormResponse(logisticalForm));
  }
}
