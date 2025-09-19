/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.rest.dto.SaleOrderResponse;
import com.axelor.apps.sale.service.saleorder.SaleOrderRestService;
import com.axelor.apps.supplychain.rest.dto.SaleOrderSupplychainPostRequest;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderGeneratorSupplychainService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/supplychain/sale-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleOrderSupplychainRestController {

  @Operation(
      summary = "Create a sale oder",
      tags = {"Sale order"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createSaleOrder(SaleOrderSupplychainPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(SaleOrder.class)
        .writeAccess(SaleOrder.class)
        .createAccess(SaleOrderLine.class)
        .check();

    SaleOrder saleOrder =
        Beans.get(SaleOrderGeneratorSupplychainService.class)
            .createSaleOrder(
                requestBody.fetchClientPartner(),
                requestBody.fetchDeliveredPartner(),
                requestBody.fetchCompany(),
                requestBody.fetchContact(),
                requestBody.fetchCurrency(),
                requestBody.getInAti(),
                requestBody.fetchPaymentMode(),
                requestBody.fetchPaymentCondition());

    saleOrder =
        Beans.get(SaleOrderRestService.class)
            .fetchAndAddSaleOrderLines(requestBody.getSaleOrderLineList(), saleOrder);

    return ResponseConstructor.buildCreateResponse(saleOrder, new SaleOrderResponse(saleOrder));
  }
}
