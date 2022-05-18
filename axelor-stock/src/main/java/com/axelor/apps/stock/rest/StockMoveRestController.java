package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.rest.dto.StockMovePostRequest;
import com.axelor.apps.stock.rest.dto.StockMovePutRequest;
import com.axelor.apps.stock.rest.dto.StockMoveResponse;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.api.*;
import com.axelor.inject.Beans;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveRestController {

  @Path("/internal/")
  @POST
  @HttpExceptionHandler
  public Response createInternalStockMove(StockMovePostRequest requestBody) throws Exception {
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
        Response.Status.CREATED, "Resource successfully created", new StockMoveResponse(stockmove));
  }

  @Path("/internal/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateInternalStockMove(
      @PathParam("id") long stockCorrectionId, StockMovePutRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockCorrectionId);

    ConflictChecker.checkVersion(stockmove, requestBody.getVersion());

    Beans.get(StockMoveService.class)
        .updateStockMoveMobility(stockmove, requestBody.getMovedQty(), requestBody.fetchUnit());

    if (requestBody.getStatus() != null) {
      Beans.get(StockMoveService.class).updateStatus(stockmove, requestBody.getStatus());
    }

    return ResponseConstructor.build(
        Response.Status.OK, "Successfully updated", new StockMoveResponse(stockmove));
  }
}
