package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ConsumedProductListResponse;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.production.rest.dto.ManufOrderProductGetRequest;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
}
