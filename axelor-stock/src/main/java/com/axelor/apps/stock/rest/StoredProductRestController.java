package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.rest.dto.StoredProductResponse;
import com.axelor.apps.stock.service.StoredProductService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/stored-product")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StoredProductRestController {

  @Operation(
      summary = "Realize storage for stored product",
      tags = {"Stored Product"})
  @Path("/realize-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizeStorage(@PathParam("id") Long storedProductId) throws AxelorException {
    new SecurityCheck().writeAccess(StoredProduct.class).createAccess(StockMove.class).check();
    StoredProduct storedProduct =
        ObjectFinder.find(StoredProduct.class, storedProductId, ObjectFinder.NO_VERSION);
    Beans.get(StoredProductService.class).createStockMoveAndStockMoveLine(storedProduct);
    return ResponseConstructor.build(
        Response.Status.OK,
        "stored product  for mass move has been successfully created for this mass move",
        new StoredProductResponse(storedProduct));
  }

  @Operation(
      summary = "Cancel storage for stored product",
      tags = {"Stored Product"})
  @Path("/cancel-storage/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelStorage(@PathParam("id") Long storedProductId) throws AxelorException {
    new SecurityCheck().writeAccess(StoredProduct.class).createAccess(StockMove.class).check();
    StoredProduct storedProduct =
        ObjectFinder.find(StoredProduct.class, storedProductId, ObjectFinder.NO_VERSION);
    Beans.get(StoredProductService.class).cancelStockMoveAndStockMoveLine(storedProduct);
    return ResponseConstructor.build(
        Response.Status.OK,
        "stored product  for mass move has been successfully canceled for this mass move",
        new StoredProductResponse(storedProduct));
  }
}
