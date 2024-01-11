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

import com.axelor.apps.base.service.api.ResponseComputeService;
import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.rest.dto.StockCorrectionPostRequest;
import com.axelor.apps.stock.rest.dto.StockCorrectionPutRequest;
import com.axelor.apps.stock.rest.dto.StockCorrectionResponse;
import com.axelor.apps.stock.service.StockCorrectionService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/stock-correction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockCorrectionRestController {

  @Operation(
      summary = "Create stock correction",
      tags = {"Stock correction"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createStockCorrection(StockCorrectionPostRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(Arrays.asList(StockCorrection.class, StockMove.class)).check();

    StockCorrection stockCorrection =
        Beans.get(StockCorrectionService.class)
            .generateStockCorrection(
                requestBody.fetchStockLocation(),
                requestBody.fetchProduct(),
                requestBody.fetchTrackingNumber(),
                requestBody.getRealQty(),
                requestBody.fetchReason());

    if (requestBody.getStatus() == StockCorrectionRepository.STATUS_VALIDATED) {
      Beans.get(StockCorrectionService.class).validate(stockCorrection);
    }

    return ResponseConstructor.build(
        Response.Status.CREATED,
        Beans.get(ResponseComputeService.class).compute(stockCorrection),
        new StockCorrectionResponse(stockCorrection));
  }

  @Operation(
      summary = "Save stock correction",
      tags = {"Stock correction"})
  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response saveStockCorrection(
      @PathParam("id") long stockCorrectionId, StockCorrectionPutRequest requestBody)
      throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockCorrection.class).createAccess(StockMove.class).check();

    StockCorrection stockCorrection =
        ObjectFinder.find(StockCorrection.class, stockCorrectionId, requestBody.getVersion());

    String message = "";
    if (requestBody.getRealQty() != null) {
      Beans.get(StockCorrectionService.class)
          .updateCorrectionQtys(stockCorrection, requestBody.getRealQty());
      message += "Real qty updated; ";
    }

    if (requestBody.fetchReason() != null) {
      Beans.get(StockCorrectionService.class)
          .updateReason(stockCorrection, requestBody.fetchReason());
      message += "Reason updated; ";
    }

    // Stock correction is not already validated
    if (stockCorrection.getStatusSelect() != StockCorrectionRepository.STATUS_VALIDATED
        && requestBody.getStatus() != null) {
      int status = requestBody.getStatus();
      // user wants to validate stock correction
      if (status == StockCorrectionRepository.STATUS_VALIDATED) {
        if (Beans.get(StockCorrectionService.class).validate(stockCorrection)) {
          message += "Status updated; ";
        }
      }
    }

    StockCorrectionResponse objectBody = new StockCorrectionResponse(stockCorrection);
    return ResponseConstructor.build(Response.Status.OK, message, objectBody);
  }
}
