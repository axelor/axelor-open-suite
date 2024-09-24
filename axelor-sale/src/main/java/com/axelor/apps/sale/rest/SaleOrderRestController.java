/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.rest.dto.SaleOrderAddLinePutRequest;
import com.axelor.apps.sale.rest.dto.SaleOrderLineResponse;
import com.axelor.apps.sale.rest.dto.SaleOrderPostRequest;
import com.axelor.apps.sale.rest.dto.SaleOrderPutRequest;
import com.axelor.apps.sale.rest.dto.SaleOrderResponse;
import com.axelor.apps.sale.service.SaleOrderRestService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGeneratorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.axelor.web.ITranslation;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/sale-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleOrderRestController {

  @Operation(
      summary = "Create a sale oder",
      tags = {"Sale order"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createSaleOrder(SaleOrderPostRequest requestBody)
      throws AxelorException, JsonProcessingException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(SaleOrder.class)
        .writeAccess(SaleOrder.class)
        .createAccess(SaleOrderLine.class)
        .check();

    SaleOrder saleOrder =
        Beans.get(SaleOrderGeneratorService.class)
            .createSaleOrder(
                requestBody.fetchClientPartner(),
                requestBody.fetchCompany(),
                requestBody.fetchContact(),
                requestBody.fetchCurrency(),
                requestBody.getInAti());

    saleOrder =
        Beans.get(SaleOrderRestService.class)
            .fetchAndAddSaleOrderLines(requestBody.getSaleOrderLineList(), saleOrder);

    return ResponseConstructor.buildCreateResponse(saleOrder, new SaleOrderResponse(saleOrder));
  }

  @Operation(
      summary = "Create an Sale order line",
      tags = {"Sale order"})
  @Path("add-line/{saleOrderId}")
  @PUT
  @HttpExceptionHandler
  public Response createSaleOrderLine(
      @PathParam("saleOrderId") Long saleOrderId, SaleOrderAddLinePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(SaleOrderLine.class)
        .writeAccess(SaleOrder.class, saleOrderId)
        .check();
    SaleOrder saleOrder = ObjectFinder.find(SaleOrder.class, saleOrderId, ObjectFinder.NO_VERSION);
    SaleOrderLineGeneratorService saleorderLineCreateService =
        Beans.get(SaleOrderLineGeneratorService.class);
    Product product = requestBody.getSaleOrderLine().fetchProduct();
    BigDecimal quantity = requestBody.getSaleOrderLine().getQuantity();
    SaleOrderLine saleOrderLine =
        saleorderLineCreateService.createSaleOrderLine(saleOrder, product, quantity);
    return ResponseConstructor.buildCreateResponse(
        saleOrderLine, new SaleOrderLineResponse(saleOrderLine));
  }

  @Operation(
      summary = "Update sale order status",
      tags = {"Sale order"})
  @Path("/status/{saleOrderId}")
  @PUT
  @HttpExceptionHandler
  public Response changeSaleOrderStatus(
      SaleOrderPutRequest requestBody, @PathParam("saleOrderId") Long saleOrderId)
      throws AxelorException {
    new SecurityCheck().writeAccess(SaleOrder.class, saleOrderId).check();
    RequestValidator.validateBody(requestBody);
    String toStatus = requestBody.getToStatus();
    SaleOrder saleOrder = ObjectFinder.find(SaleOrder.class, saleOrderId, requestBody.getVersion());
    if (SaleOrderPutRequest.SALE_ORDER_UPDATE_FINALIZE.equals(toStatus)) {
      Beans.get(SaleOrderFinalizeService.class).finalizeQuotation(saleOrder);
    }
    if (SaleOrderPutRequest.SALE_ORDER_UPDATE_CONFIRM.equals(toStatus)) {
      Beans.get(SaleOrderConfirmService.class).confirmSaleOrder(saleOrder);
    }
    return ResponseConstructor.build(Response.Status.OK, I18n.get(ITranslation.STATUS_CHANGE));
  }
}
