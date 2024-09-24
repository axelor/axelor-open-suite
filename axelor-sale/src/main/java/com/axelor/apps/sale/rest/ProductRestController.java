package com.axelor.apps.sale.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.rest.dto.ProductPostRequest;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import com.axelor.apps.sale.rest.dto.ProductResquest;
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

@Path("/aos/product")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductRestController {

  @Operation(
      summary = "Get products price",
      tags = {"Products"})
  @Path("/price")
  @POST
  @HttpExceptionHandler
  public Response getProductsPrice(ProductPostRequest requestBody)
      throws JSONException, AxelorException {
    RequestValidator.validateBody(requestBody);
    for (ProductResquest unitProductPostRequest : requestBody.getProductList()) {
      new SecurityCheck()
          .readAccess(Product.class, unitProductPostRequest.getProductId())
          .readAccess(Unit.class, unitProductPostRequest.getUnitId())
          .check();
    }
    List<ProductResquest> unitProducts = requestBody.getProductList();
    Partner partner = requestBody.fetchPartner();
    Company company = requestBody.fetchCompany();
    Currency currency = requestBody.fetchCurrency();
    List<ProductResponse> productResponses =
        Beans.get(ProductRestService.class)
            .computeProductResponse(company, unitProducts, partner, currency);
    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.PRODUCT_PRICE_INFORMATION), productResponses);
  }
}
