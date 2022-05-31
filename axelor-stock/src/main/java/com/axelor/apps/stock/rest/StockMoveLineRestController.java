package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.rest.dto.StockIncomingMoveLinePutRequest;
import com.axelor.apps.stock.rest.dto.StockIncomingMoveLineResponse;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.tool.api.ConflictChecker;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.inject.Beans;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-move-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveLineRestController {

  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateIncomingStockMoveLine(
      @PathParam("id") long stockMoveLineId, StockIncomingMoveLinePutRequest requestBody)
      throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMoveLine stockmoveLine = ObjectFinder.find(StockMoveLine.class, stockMoveLineId);

    ConflictChecker.checkVersion(stockmoveLine, requestBody.getVersion());

    Beans.get(StockMoveLineService.class)
        .updateStockMoveLine(stockmoveLine, requestBody.getRealQty(), requestBody.getConformity());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Line successfully updated.",
        new StockIncomingMoveLineResponse(stockmoveLine));
  }
}
