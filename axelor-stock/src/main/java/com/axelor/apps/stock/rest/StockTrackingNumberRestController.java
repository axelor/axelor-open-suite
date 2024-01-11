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
package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.api.ResponseComputeService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.rest.dto.StockTrackingNumberPostRequest;
import com.axelor.apps.stock.rest.dto.StockTrackingNumberResponse;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/stock-tracking-number")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockTrackingNumberRestController {

  /**
   * Create new tracking number for given product. Full path to request is
   * /ws/aos/stock-tracking-number/
   */
  @Operation(
      summary = "Create tracking number",
      tags = {"Stock tracking number"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createTrackingNumber(StockTrackingNumberPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(TrackingNumber.class).check();

    TrackingNumber trackingNumber =
        Beans.get(TrackingNumberService.class)
            .generateTrackingNumber(
                requestBody.fetchProduct(),
                requestBody.fetchCompany(),
                Beans.get(AppBaseService.class).getTodayDate(requestBody.fetchCompany()),
                requestBody.getOrigin(),
                requestBody.getNotes());

    return ResponseConstructor.build(
        Response.Status.CREATED,
        Beans.get(ResponseComputeService.class).compute(trackingNumber),
        new StockTrackingNumberResponse(trackingNumber));
  }
}
