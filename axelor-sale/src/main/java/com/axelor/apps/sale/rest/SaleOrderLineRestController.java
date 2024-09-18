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
import com.axelor.apps.sale.rest.dto.SaleOrderLinePostRequest;
import com.axelor.apps.sale.rest.dto.SaleOrderLineResponse;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGeneratorService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/sale-order-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleOrderLineRestController {

  @Operation(
      summary = "Create an Sale order line",
      tags = {"Sale order line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createSaleOrderLine(SaleOrderLinePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(SaleOrderLine.class).writeAccess(SaleOrder.class).check();

    SaleOrderLineGeneratorService saleorderLineCreateService =
        Beans.get(SaleOrderLineGeneratorService.class);
    Product product = requestBody.fetchProduct();
    SaleOrder saleOrder = requestBody.fetchsaleOrder();
    BigDecimal quantity = requestBody.getQuantity();
    SaleOrderLine saleOrderLine =
        saleorderLineCreateService.createSaleOrderLine(saleOrder, product, quantity);

    return ResponseConstructor.buildCreateResponse(
        saleOrderLine, new SaleOrderLineResponse(saleOrderLine));
  }
}
