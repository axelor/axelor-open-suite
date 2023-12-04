package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.rest.dto.MassStockMoveProductToMoveFromStockMoveLinesPostRequest;
import com.axelor.apps.stock.rest.dto.MassStockMoveResponse;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.apps.stock.service.PickedProductService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).importProductFromStockLocation(massStockMove);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully created for this mass move.",
        new MassStockMoveResponse(massStockMove, null));
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
        massStockMove.getPickedProductList().stream()
            .anyMatch(it -> it.getPickedQty().compareTo(it.getCurrentQty()) == 1);
    boolean isQtyEqualZero =
        massStockMove.getPickedProductList().stream()
            .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getPickedQty()) == 0);

    Beans.get(MassStockMoveService.class).realizePicking(massStockMove);

    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO
              + ". "
              + StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove, null));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove, null));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove, null));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock Move for Picked Products has been successfully created for this mass move.",
        new MassStockMoveResponse(massStockMove, null));
  }

  @Operation(
      summary = "Cancel picking for all picked products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/cancel-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelMassPicking(@PathParam("id") Long massStockMoveId) {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).cancelPicking(massStockMove);
    Beans.get(MassStockMoveService.class).setStatusSelectToDraft(massStockMove);
    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully canceled for this mass move.",
        new MassStockMoveResponse(massStockMove, null));
  }

  @Operation(
      summary = "Realize storage for all stored products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/realize-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizeStorage(@PathParam("id") Long massStockMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(StoredProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);

    boolean isGreaterThan =
        massStockMove.getStoredProductList().stream()
            .anyMatch(it -> it.getStoredQty().compareTo(it.getCurrentQty()) == 1);
    boolean isQtyEqualZero =
        massStockMove.getStoredProductList().stream()
            .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getStoredQty()) == 0);
    Beans.get(MassStockMoveService.class).realizeStorage(massStockMove);
    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY
              + " "
              + StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove, null));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassStockMoveResponse(massStockMove, null));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassStockMoveResponse(massStockMove, null));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully created for this mass move",
        new MassStockMoveResponse(massStockMove, null));
  }

  @Operation(
      summary = "Cancel storage for all stored products of mass move",
      tags = {"Mass Stock Move"})
  @Path("/cancel-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelStorage(@PathParam("id") Long massStockMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(StoredProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassStockMoveService.class).cancelStorage(massStockMove);
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully canceled for this mass move",
        new MassStockMoveResponse(massStockMove, null));
  }

  @Operation(
      summary = "Fill product to move ",
      tags = {"Mass Stock Move"})
  @Path("/fill-product-to-move/")
  @POST
  @HttpExceptionHandler
  public Response fillProductToMove(
      MassStockMoveProductToMoveFromStockMoveLinesPostRequest requestBody) {
    new SecurityCheck()
        .writeAccess(MassStockMove.class)
        .createAccess(MassStockMoveNeed.class)
        .check();
    MassStockMove massStockMove = requestBody.fetchMassStockMove();
    Beans.get(MassStockMoveService.class)
        .useStockMoveLinesIdsToCreateMassStockMoveNeeds(
            massStockMove, requestBody.getStockMoveLinesIds());
    return ResponseConstructor.build(
        Response.Status.OK,
        "Mass move product to move has been successfully updated from stock move lines",
        new MassStockMoveResponse(massStockMove, null));
  }

  @Operation(
      summary = "Fetch stock move lines to create mass move needs",
      tags = {"Mass Stock Move"})
  @Path("/fetch-stock-move-lines/{id}")
  @POST
  @HttpExceptionHandler
  public Response fetchStockMoveLines(@PathParam("id") Long massStockMoveId) {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    List<StockMoveLine> stockMovLineToFetch = new ArrayList<>();
    if (massStockMove.getCommonFromStockLocation() != null
        && massStockMove.getCommonToStockLocation() != null) {
      stockMovLineToFetch =
          Beans.get(StockMoveLineRepository.class)
              .all()
              .filter(
                  "(self.stockMove.statusSelect =?1 "
                      + "AND self.stockMove.toStockLocation.id =?2) "
                      + "OR (self.stockMove.statusSelect =?3 "
                      + "AND self.stockMove.fromStockLocation.id =?4)",
                  StockMoveRepository.STATUS_REALIZED,
                  massStockMove.getCommonFromStockLocation().getId(),
                  StockMoveRepository.STATUS_PLANNED,
                  massStockMove.getCommonToStockLocation().getId().toString())
              .fetch();

    } else if (massStockMove.getCommonFromStockLocation() != null) {
      stockMovLineToFetch =
          Beans.get(StockMoveLineRepository.class)
              .all()
              .filter(
                  "self.stockMove.statusSelect =?1 " + "AND self.stockMove.toStockLocation.id =?2",
                  StockMoveRepository.STATUS_REALIZED,
                  massStockMove.getCommonFromStockLocation().getId())
              .fetch();

    } else if (massStockMove.getCommonToStockLocation() != null) {
      stockMovLineToFetch =
          Beans.get(StockMoveLineRepository.class)
              .all()
              .filter(
                  "self.stockMove.statusSelect =?1 "
                      + "AND self.stockMove.fromStockLocation.id =?2 ",
                  StockMoveRepository.STATUS_PLANNED,
                  massStockMove.getCommonToStockLocation().getId())
              .fetch();
    }
    List<Long> stockMoveLinesIds =
        stockMovLineToFetch != null
            ? stockMovLineToFetch.stream().map(StockMoveLine::getId).collect(Collectors.toList())
            : new ArrayList<>();
    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked lines have been successfully generated",
        new MassStockMoveResponse(massStockMove, stockMoveLinesIds));
  }

  @Operation(
      summary = "Generate picked Lines from mass move needs",
      tags = {"Mass Stock Move"})
  @Path("/generate-picked-lines/{id}")
  @POST
  @HttpExceptionHandler
  public Response generatePickedLines(@PathParam("id") Long massStockMoveId) {
    new SecurityCheck().writeAccess(MassStockMove.class).createAccess(PickedProduct.class).check();
    MassStockMove massStockMove =
        ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
    for (MassStockMoveNeed massStockMoveNeed : massStockMove.getProductToMoveList()) {
      Beans.get(PickedProductService.class)
          .createPickedProductFromMassStockMoveNeed(massStockMoveNeed);
    }
    Beans.get(MassStockMoveService.class).clearProductToMoveList(massStockMove);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked lines have been successfully generated",
        new MassStockMoveResponse(massStockMove, null));
  }
}
