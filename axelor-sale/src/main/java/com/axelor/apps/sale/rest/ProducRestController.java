package com.axelor.apps.sale.rest;


import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.rest.dto.ProductPostRequest;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import com.axelor.apps.sale.rest.dto.SaleOrderResponse;
import com.axelor.apps.sale.service.ProductRestService;
import com.axelor.apps.sale.service.SaleOrderGeneratorService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/sale-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProducRestController {
    @Operation(
            summary = "Get product price",
            tags = {"Product"})
    @Path("/")
    @POST
    @HttpExceptionHandler
    public Response getProductPrice(ProductPostRequest requestBody) {
        RequestValidator.validateBody(requestBody);
        new SecurityCheck().createAccess(SaleOrder.class).check();

        Product product =
                Beans.get(ProductRestService.class).fetchProductPrice(requestBody.fetchProduct(), requestBody.fetchPartner(),requestBody.fetchCompany());

        return ResponseConstructor.buildCreateResponse(product, new ProductResponse(product));
    }
}
