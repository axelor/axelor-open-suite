package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.db.repo.*;
import com.axelor.apps.stock.rest.dto.StockCorrectionResponse;
import com.axelor.apps.stock.rest.dto.StockMoveCreateRequest;
import com.axelor.apps.stock.rest.dto.StockMoveResponse;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseBody;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.service.TraceBackService;
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
  public Response createInternalStockMove(StockMoveCreateRequest requestBody) {
    try {
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

      return ResponseConstructor.build(201, "Resource successfully created", new StockMoveResponse(stockmove));
    } catch (ForbiddenException e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(403, e.getMessage(), null);
    } catch (Exception e) {
      int codeStatus = 500;
      ResponseBody responseBody = new ResponseBody(codeStatus, "Error while creating resource");
      return Response.status(codeStatus)
          .type(MediaType.APPLICATION_JSON)
          .entity(responseBody)
          .build();
    }
  }
}
