package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.rest.dto.PickedProductsResponse;
import com.axelor.apps.stock.service.PickedProductsService;
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
@Path("/aos/picked-product")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PickedProductsRestController {

  @Operation(
      summary = "Realize picking for picked product",
      tags = {"Picked Product"})
  @Path("/realize-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response realizePicking(@PathParam("id") Long pickedProductId) throws AxelorException {
    new SecurityCheck().writeAccess(PickedProducts.class).createAccess(StockMove.class).check();
    PickedProducts pickedProduct =
        ObjectFinder.find(PickedProducts.class, pickedProductId, ObjectFinder.NO_VERSION);
    Beans.get(PickedProductsService.class)
        .createStockMoveAndStockMoveLine(pickedProduct.getMassStockMove(), pickedProduct);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Stock move successfully created for this Picked Product",
        new PickedProductsResponse(pickedProduct));
  }

  @Operation(
      summary = "Cancel picking for picked product",
      tags = {"Picked Product"})
  @Path("/cancel-picking/{id}")
  @POST
  @HttpExceptionHandler
  public Response cancelPicking(@PathParam("id") Long pickedProductId) throws AxelorException {
    new SecurityCheck().writeAccess(PickedProducts.class).writeAccess(StockMove.class).check();
    PickedProducts pickedProduct =
        ObjectFinder.find(PickedProducts.class, pickedProductId, ObjectFinder.NO_VERSION);
    Beans.get(PickedProductsService.class)
        .cancelStockMoveAndStockMoveLine(pickedProduct.getMassStockMove(), pickedProduct);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Picked Product successfully canceled",
        new PickedProductsResponse(pickedProduct));
  }
}
