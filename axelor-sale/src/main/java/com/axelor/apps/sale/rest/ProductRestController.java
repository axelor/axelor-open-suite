package com.axelor.apps.sale.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.rest.dto.CurrencyResponse;
import com.axelor.apps.sale.rest.dto.PriceResponse;
import com.axelor.apps.sale.rest.dto.ProductPostRequest;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import com.axelor.apps.sale.service.ProductRestService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.axelor.web.ITranslation;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;

@Path("/aos/product-price")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductRestController {
  @Operation(
      summary = "Get product price",
      tags = {"Product"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response getProductPrice(ProductPostRequest requestBody) throws JSONException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(Product.class)
        .createAccess(Partner.class)
        .createAccess(Company.class)
        .check();
    Product product = requestBody.fetchProduct();
    Partner partner = requestBody.fetchPartner();
    Company company = requestBody.fetchCompany();

    CurrencyResponse currencyResponse = new CurrencyResponse(company);
    List<PriceResponse> prices =
        Beans.get(ProductRestService.class).fetchProductPrice(product, partner, company);
    return ResponseConstructor.build(
        Response.Status.OK,
            String.format( I18n.get(ITranslation.PRODUCT_PRICE_INFORMATION),product.getId()),
        new ProductResponse(requestBody.getProductId(), prices, currencyResponse));
  }
}
