package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.rest.dto.StockIncomingMovePutRequest;
import com.axelor.apps.stock.rest.dto.StockIncomingMoveResponse;
import com.axelor.apps.stock.rest.dto.StockInternalMovePostRequest;
import com.axelor.apps.stock.rest.dto.StockInternalMovePutRequest;
import com.axelor.apps.stock.rest.dto.StockInternalMoveResponse;
import com.axelor.apps.stock.rest.dto.StockMoveLinePostRequest;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.api.ConflictChecker;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.inject.Beans;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveRestController {

  // GENERAL STOCK MOVE REQUESTS

  // REALIZE STOCK MOVE :
  @Path("/realize/{id}")
  @PUT
  @HttpExceptionHandler
  public Response realizeStockMove(@PathParam("id") long stockMoveId, RequestStructure requestBody)
      throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId);

    ConflictChecker.checkVersion(stockmove, requestBody.getVersion());

    Beans.get(StockMoveService.class).realize(stockmove);

    return ResponseConstructor.build(
        Response.Status.OK, "Stock move with id " + stockmove.getId() + " successfully realized.");
  }

  // ADD NEW LINE TO STOCK MOVE :
  @Path("/add-line/{id}")
  @POST
  @HttpExceptionHandler
  public Response addLineStockMove(
      @PathParam("id") long stockMoveId, StockMoveLinePostRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).createAccess(StockMoveLine.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId);

    Beans.get(StockMoveService.class)
        .addLineStockMove(
            stockmove,
            requestBody.fetchProduct(),
            requestBody.fetchTrackingNumber(),
            requestBody.getExpectedQty(),
            requestBody.getRealQty(),
            requestBody.fetchUnit(),
            requestBody.getConformity());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Line successfully added to stock move with id "
            + stockmove.getId()
            + " successfully updated.");
  }

  // INTERNAL STOCK MOVE REQUESTS

  @Path("/internal/")
  @POST
  @HttpExceptionHandler
  public Response createInternalStockMove(StockInternalMovePostRequest requestBody)
      throws Exception {
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
        "Resource successfully created",
        new StockInternalMoveResponse(stockmove));
  }

  @Path("/internal/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateInternalStockMove(
      @PathParam("id") long stockMoveId, StockInternalMovePutRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId);

    ConflictChecker.checkVersion(stockmove, requestBody.getVersion());

    Beans.get(StockMoveService.class)
        .updateStockMoveMobility(stockmove, requestBody.getMovedQty(), requestBody.fetchUnit());

    if (requestBody.getStatus() != null) {
      Beans.get(StockMoveService.class).updateStatus(stockmove, requestBody.getStatus());
    }

    return ResponseConstructor.build(
        Response.Status.OK, "Successfully updated", new StockInternalMoveResponse(stockmove));
  }

  // INCOMING STOCK MOVE REQUESTS

  @Path("/incoming/update-destination/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateIncomingStockMove(
      @PathParam("id") long stockMoveId, StockIncomingMovePutRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMove stockmove = ObjectFinder.find(StockMove.class, stockMoveId);

    ConflictChecker.checkVersion(stockmove, requestBody.getVersion());

    Beans.get(StockMoveService.class)
        .updateStockMoveDestinationLocation(stockmove, requestBody.fetchToStockLocation());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Destination stock location successfully updated.",
        new StockIncomingMoveResponse(stockmove));
  }
}
