package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.rest.dto.MassStockMoveResponse;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.math.BigDecimal;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/mass-stock-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MassStockMoveRestController {

  @Operation(
      summary = "Import product from stock location to mass move",
      tags = {"Mass Stock Move"})
  @Path("/add-picked-product/{id}")
  @POST
  @HttpExceptionHandler
  public Response importProductFromStockLocation(@PathParam("id") Long massStockMoveId)
      throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProducts.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).importProductFromStockLocation(massStockMove);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully created for this mass move.",
        new MassStockMoveResponse(massStockMove));
  }

  @Operation(
      summary = "Realize picking for all picked products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/realize-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizePicking(@PathParam("id") Long massStockMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(StockMove.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    boolean isGreaterThan =
        massStockMove.getPickedProductsList().stream()
            .anyMatch(it -> it.getPickedQty().compareTo(it.getCurrentQty()) == 1);
    boolean isQtyEqualZero =
        massStockMove.getPickedProductsList().stream()
            .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getPickedQty()) == 0);

    Beans.get(MassStockMoveService.class).realizePicking(massStockMove);

    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO
              + ". "
              + StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock Move for Picked Products has been successfully created for this mass move.",
        new MassStockMoveResponse(massStockMove));
  }

  @Operation(
      summary = "Cancel picking for all picked products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/cancel-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelMassPicking(@PathParam("id") Long massStockMoveId) {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProducts.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).cancelPicking(massStockMove);
    Beans.get(MassStockMoveService.class).setStatusSelectToDraft(massStockMove);
    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully canceled for this mass move.",
        new MassStockMoveResponse(massStockMove));
  }

  @Operation(
      summary = "Realize storage for all stored products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/realize-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizeStorage(@PathParam("id") Long massStockMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(StoredProducts.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);

    boolean isGreaterThan =
        massStockMove.getStoredProductsList().stream()
            .anyMatch(it -> it.getStoredQty().compareTo(it.getCurrentQty()) == 1);
    boolean isQtyEqualZero =
        massStockMove.getStoredProductsList().stream()
            .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getStoredQty()) == 0);
    Beans.get(MassStockMoveService.class).realizeStorage(massStockMove);
    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY
              + " "
              + StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully created for this mass move",
        new MassStockMoveResponse(massStockMove));
  }

  @Operation(
      summary = "Cancel storage for all stored products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/cancel-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelStorage(@PathParam("id") Long massStockMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(StoredProducts.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).cancelStorage(massStockMove);
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully canceled for this mass move",
        new MassStockMoveResponse(massStockMove));
  }
}
