package com.axelor.apps.stock.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.dto.StockProductGetRequest;
import com.axelor.apps.stock.rest.dto.StockProductPutRequest;
import com.axelor.apps.stock.rest.dto.StockProductResponse;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.tool.api.*;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-product")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockProductRestController {

  @Path("/fetch-product-with-stock/{productId}")
  @POST
  @HttpExceptionHandler
  public Response fetchProductIndicators(
      @PathParam("productId") long productId, StockProductGetRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().readAccess(Product.class).check();

    Product product = ObjectFinder.find(Product.class, productId);
    Company company = requestBody.getCompany();
    StockLocation stockLocation = requestBody.getStockLocation();

    return Beans.get(StockProductRestService.class).getProductIndicators(product, company, stockLocation);
  }

  @Path("/modify-locker/{productId}")
  @PUT
  @HttpExceptionHandler
  public Response modifyProductLocker(
      @PathParam("productId") Long productId, StockProductPutRequest requestBody) throws Exception {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Product.class).check();

    Product product = ObjectFinder.find(Product.class, productId);

    Beans.get(StockLocationService.class)
        .changeProductLocker(requestBody.fetchStockLocation(), product, requestBody.getNewLocker());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Update locker for product with id "
            + product.getId()
            + " to "
            + requestBody.getNewLocker());
  }
}
