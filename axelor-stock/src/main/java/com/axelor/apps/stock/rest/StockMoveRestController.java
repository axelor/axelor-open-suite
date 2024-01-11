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
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.rest.dto.StockInternalMovePostRequest;
import com.axelor.apps.stock.rest.dto.StockInternalMovePutRequest;
import com.axelor.apps.stock.rest.dto.StockInternalMoveResponse;
import com.axelor.apps.stock.rest.dto.StockMoveLinePostRequest;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveUpdateService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/stock-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveRestController {

  /** Realize a planified stock move. Full path to request is /ws/aos/stock-move/realize/{id} */
  @Operation(
      summary = "Realize stock move",
      tags = {"Stock move"})
  @Path("/realize/{id}")
  @PUT
  @HttpExceptionHandler
  public Response realizeStockMove(@PathParam("id") long stockMoveId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId, requestBody.getVersion());

    Beans.get(StockMoveService.class).realize(stockmove);

    return ResponseConstructor.build(
        Response.Status.OK, "Stock move with id " + stockmove.getId() + " successfully realized.");
  }

  /** Add new line in a stock move. Full path to request is /ws/aos/stock-move/add-line/{id} */
  @Operation(
      summary = "Add line to stock move",
      tags = {"Stock move"})
  @Path("/add-line/{id}")
  @POST
  @HttpExceptionHandler
  public Response addLineStockMove(
      @PathParam("id") long stockMoveId, StockMoveLinePostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).createAccess(StockMoveLine.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId, requestBody.getVersion());

    Beans.get(StockMoveLineService.class)
        .createStockMoveLine(
            stockmove,
            requestBody.fetchProduct(),
            requestBody.fetchTrackingNumber(),
            requestBody.getExpectedQty(),
            requestBody.getRealQty(),
            requestBody.fetchUnit(),
            requestBody.getConformity());

    Beans.get(StockMoveService.class).updateStocks(stockmove);

    return ResponseConstructor.build(
        Response.Status.OK, "Line successfully added to stock move with id " + stockmove.getId());
  }

  /**
   * Create new internal move with only one product. Full path to request is
   * /ws/aos/stock-move/internal/
   */
  @Operation(
      summary = "Create internal stock move",
      tags = {"Stock move"})
  @Path("/internal/")
  @POST
  @HttpExceptionHandler
  public Response createInternalStockMove(StockInternalMovePostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(StockMove.class).check();
    StockMove stockmove =
        Beans.get(StockMoveService.class)
            .createStockMoveMobility(
                requestBody.fetchOriginStockLocation(),
                requestBody.fetchDestStockLocation(),
                requestBody.fetchCompany(),
                requestBody.fetchProduct(),
                requestBody.fetchTrackingNumber(),
                requestBody.getMovedQty(),
                requestBody.fetchUnit());

    return ResponseConstructor.build(
        Response.Status.CREATED,
        Beans.get(ResponseComputeService.class).compute(stockmove),
        new StockInternalMoveResponse(stockmove));
  }

  /**
   * Update an internal stock move depending on the elements given in requestBody. Full path to
   * request is /ws/aos/stock-move/internal/{id}
   */
  @Operation(
      summary = "Update internal stock move",
      tags = {"Stock move"})
  @Deprecated
  @Path("/internal/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateInternalStockMove(
      @PathParam("id") long stockMoveId, StockInternalMovePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId, requestBody.getVersion());

    Beans.get(StockMoveUpdateService.class)
        .updateStockMoveMobility(stockmove, requestBody.getMovedQty(), requestBody.fetchUnit());

    if (requestBody.getStatus() != null) {
      Beans.get(StockMoveUpdateService.class).updateStatus(stockmove, requestBody.getStatus());
    }

    return ResponseConstructor.build(
        Response.Status.OK, "Successfully updated", new StockInternalMoveResponse(stockmove));
  }
}
