package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.rest.dto.MassMovePostRequest;
import com.axelor.apps.stock.rest.dto.MassMovePutRequest;
import com.axelor.apps.stock.rest.dto.MassMoveResponse;
import com.axelor.apps.stock.service.MassMoveService;
import com.axelor.apps.stock.service.PickedProductsService;
import com.axelor.apps.stock.service.StoredProductsService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.google.inject.persist.Transactional;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/mass-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MassMoveRestController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Operation(
      summary = "Mass Move update",
      tags = {"Mass Move"})
  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateMassMove(@PathParam("id") Long massMoveId, MassMovePutRequest requestBody) {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Arrays.asList(MassMove.class)).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, requestBody.getVersion());
    Beans.get(MassMoveService.class)
        .updateMassMoveMobility(
            massMove.getId(),
            requestBody.getStatusSelect(),
            requestBody.fetchCompany(),
            requestBody.fetchCarStockLocation(),
            requestBody.fetchCommonFromStockLocation(),
            requestBody.fetchCommonToStockLocation());

    return ResponseConstructor.build(
        Response.Status.OK, "Mass Move successfully updated", new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Add mass move",
      tags = {"Mass Move"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response addMassMove(MassMovePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(MassMove.class).createAccess(MassMove.class).check();
    LOG.debug(
        "tous les requestBody ---------> "
            + requestBody.getStatusSelect()
            + requestBody.getCompanyId()
            + requestBody.fetchCarStockLocation()
            + requestBody.fetchCommonFromStockLocation()
            + requestBody.fetchCommonToStockLocation());
    MassMove massMove =
        Beans.get(MassMoveService.class)
            .createMassMoveMobility(
                requestBody.getStatusSelect(),
                requestBody.fetchCompany(),
                requestBody.fetchCarStockLocation(),
                requestBody.fetchCommonFromStockLocation(),
                requestBody.fetchCommonToStockLocation());

    return ResponseConstructor.build(
        Response.Status.CREATED, "Mass Move successfully created.", new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Import product from stock location to mass move",
      tags = {"Mass Move"})
  @Path("/add-picked-product/{id}")
  @POST
  @HttpExceptionHandler
  public Response importProductFromStockLocation(@PathParam("id") Long massMoveId)
      throws AxelorException {
    new SecurityCheck().writeAccess(MassMove.class).createAccess(PickedProducts.class).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
    Beans.get(MassMoveService.class).importProductFromStockLocation(massMove);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully created for this mass move.",
        new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Realize picking for all picked products of mass move",
      tags = {"Mass Move"})
  @Path("/realize-picking/{id}")
  @POST
  @HttpExceptionHandler
  @Transactional
  public Response realizePicking(@PathParam("id") Long massMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassMove.class).createAccess(StockMove.class).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
    boolean isQtyEqualZero = false;
    boolean isGreaterThan = false;
    for (PickedProducts pickedProducts : massMove.getPickedProductsList()) {
      if (pickedProducts.getPickedQty().compareTo(BigDecimal.ZERO) == 0) {
        isQtyEqualZero = true;
      }
      if (pickedProducts.getPickedQty().compareTo(pickedProducts.getCurrentQty()) == 1) {
        isGreaterThan = true;
      }
      try {
        Beans.get(PickedProductsService.class)
            .createStockMoveAndStockMoveLine(massMove, pickedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO
              + ". "
              + StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassMoveResponse(massMove));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO,
          new MassMoveResponse(massMove));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassMoveResponse(massMove));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock Move for Picked Products has been successfully created for this mass move.",
        new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Cancel picking for all picked products of mass move",
      tags = {"Mass Move"})
  @Path("/cancel-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelMassPicking(@PathParam("id") Long massMoveId) {
    new SecurityCheck().writeAccess(MassMove.class).createAccess(PickedProducts.class).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
    for (PickedProducts pickedProducts : massMove.getPickedProductsList()) {
      Beans.get(PickedProductsService.class)
          .cancelStockMoveAndStockMoveLine(massMove, pickedProducts);
    }
    Beans.get(MassMoveService.class).setStatusSelectToDraft(massMove);
    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product  successfully canceled for this mass move.",
        new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Realize storage for all stored products of mass move",
      tags = {"Mass Move"})
  @Path("/realize-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizeStorage(@PathParam("id") Long massMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassMove.class).createAccess(StoredProducts.class).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
    boolean isGreaterThan = false;
    boolean isQtyEqualZero = false;
    for (StoredProducts storedProducts : massMove.getStoredProductsList()) {
      if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1) {
        isGreaterThan = true;
      }
      if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0) {
        isQtyEqualZero = true;
      }
      Beans.get(StoredProductsService.class).createStockMoveAndStockMoveLine(storedProducts);
    }
    if (isQtyEqualZero && isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY
              + " "
              + StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassMoveResponse(massMove));
    } else if (isQtyEqualZero) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO,
          new MassMoveResponse(massMove));
    } else if (isGreaterThan) {
      return ResponseConstructor.build(
          Response.Status.OK,
          StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY,
          new MassMoveResponse(massMove));
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully created for this mass move",
        new MassMoveResponse(massMove));
  }

  @Operation(
      summary = "Cancel storage for all stored products of mass move",
      tags = {"Mass Move"})
  @Path("/cancel-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelStorage(@PathParam("id") Long massMoveId) throws AxelorException {
    new SecurityCheck().writeAccess(MassMove.class).createAccess(StoredProducts.class).check();
    MassMove massMove = ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
    for (StoredProducts storedProducts : massMove.getStoredProductsList()) {
      Beans.get(StoredProductsService.class).cancelStockMoveAndStockMoveLine(storedProducts);
    }
    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move for stored product has been successfully canceled for this mass move",
        new MassMoveResponse(massMove));
  }
}
