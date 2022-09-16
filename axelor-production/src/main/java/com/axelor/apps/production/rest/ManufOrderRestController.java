package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ConsumedProductListResponse;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.production.rest.dto.ManufOrderProductGetRequest;
import com.axelor.apps.production.rest.dto.ManufOrderProductPutRequest;
import com.axelor.apps.production.rest.dto.ManufOrderPutRequest;
import com.axelor.apps.production.rest.dto.ManufOrderResponse;
import com.axelor.apps.production.rest.dto.ManufOrderStockMoveLineResponse;
import com.axelor.apps.production.rest.dto.WastedProductPostRequest;
import com.axelor.apps.production.rest.dto.WastedProductResponse;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/manuf-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ManufOrderRestController {

  @Path("/consumed-products/fetch")
  @POST
  @HttpExceptionHandler
  public Response fetchConsumedProducts(ManufOrderProductGetRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .readAccess(Arrays.asList(ManufOrder.class, StockMove.class, ProdProduct.class))
        .check();

    List<ConsumedProductResponse> consumedProductList =
        Beans.get(ManufOrderProductRestService.class)
            .getConsumedProductList(requestBody.fetchManufOrder());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Request successfully completed",
        new ConsumedProductListResponse(consumedProductList, requestBody.fetchManufOrder()));
  }

  @Path("/update-product-qty")
  @PUT
  @HttpExceptionHandler
  public Response updateProductQuantity(ManufOrderProductPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Arrays.asList(ManufOrder.class, StockMoveLine.class)).check();

    StockMoveLine stockMoveLine =
        Beans.get(ManufOrderProductRestService.class)
            .updateStockMoveLineQty(
                requestBody.fetchStockMoveLine(), requestBody.getProdProductQty());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Quantity successfully updated.",
        new ManufOrderStockMoveLineResponse(stockMoveLine));
  }

  @Path("/{manufOrderId}")
  @PUT
  @HttpExceptionHandler
  public Response updateManufOrderStatus(
      @PathParam("manufOrderId") long manufOrderId, ManufOrderPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(ManufOrder.class).check();

    ManufOrder manufOrder =
        ObjectFinder.find(ManufOrder.class, manufOrderId, requestBody.getVersion());

    Beans.get(ManufOrderRestService.class)
        .updateStatusOfManufOrder(manufOrder, requestBody.getStatus());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Manufacturing order successfully updated.",
        new ManufOrderResponse(manufOrder));
  }

  @Path("/{manufOrderId}/waste-product/")
  @POST
  @HttpExceptionHandler
  public Response addWastedProduct(
      @PathParam("manufOrderId") long manufOrderId, WastedProductPostRequest requestBody) {
    RequestValidator.validateBody(requestBody);

    new SecurityCheck().writeAccess(ManufOrder.class).createAccess(ProdProduct.class).check();
    ManufOrder manufOrder =
        ObjectFinder.find(ManufOrder.class, manufOrderId, requestBody.getVersion());

    ProdProduct prodProduct =
        new ProdProduct(
            requestBody.fetchProduct(), requestBody.getQty(), requestBody.fetchProduct().getUnit());

    Beans.get(ManufOrderProductRestService.class).addWasteProduct(manufOrder, prodProduct);

    return ResponseConstructor.build(
        Response.Status.CREATED,
        "Waste product successfully added to manufacturing order",
        new WastedProductResponse(prodProduct));
  }
}
