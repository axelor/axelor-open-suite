package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.rest.dto.StockMoveCreateRequest;
import com.axelor.apps.stock.rest.dto.StockMoveResponse;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
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
  public Response createInternalStockMove(StockMoveCreateRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(StockMove.class).check();
    StockMove stockmove =
        Beans.get(StockMoveService.class)
            .createStockMoveMobility(
                requestBody.getOriginStockLocation(),
                requestBody.getDestStockLocation(),
                requestBody.getCompany(),
                requestBody.getProduct(),
                requestBody.getTrackingNumber(),
                requestBody.getMovedQty(),
                requestBody.getUnit());

    return ResponseConstructor.build(
        Response.Status.CREATED, "Resource successfully created", new StockMoveResponse(stockmove));
  }
}
