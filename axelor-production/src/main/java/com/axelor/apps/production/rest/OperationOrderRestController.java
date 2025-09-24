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
package com.axelor.apps.production.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ManufOrderProductListResponse;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.apps.production.rest.dto.OperationOrderProductGetRequest;
import com.axelor.apps.production.rest.dto.OperationOrderPutRequest;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/aos/operation-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OperationOrderRestController {

  @Operation(
      summary = "Update manufacturing operation status",
      tags = {"Operation order"})
  @Path("/{operationOrderId}")
  @PUT
  @HttpExceptionHandler
  public Response updateOperationOrderStatus(
      @PathParam("operationOrderId") Long operationOrderId, OperationOrderPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(OperationOrder.class, operationOrderId).check();

    OperationOrder operationOrder =
        ObjectFinder.find(OperationOrder.class, operationOrderId, requestBody.getVersion());

    return Beans.get(OperationOrderRestService.class)
        .updateStatusOfOperationOrder(operationOrder, requestBody.getStatus());
  }

  @Operation(
      summary = "Fetch consumed product",
      tags = {"Operation Order"})
  @Path("/consumed-products/fetch")
  @POST
  @HttpExceptionHandler
  public Response fetchConsumedProducts(OperationOrderProductGetRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .readAccess(OperationOrder.class, requestBody.getOperationOrderId())
        .readAccess(ProdProduct.class)
        .check();

    List<ManufOrderProductResponse> consumedProductList =
        Beans.get(ManufOrderProductRestService.class)
            .getConsumedProductList(requestBody.fetchOperationOrder());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.REQUEST_COMPLETED),
        new ManufOrderProductListResponse(
            consumedProductList, requestBody.fetchOperationOrder().getVersion()));
  }
}
