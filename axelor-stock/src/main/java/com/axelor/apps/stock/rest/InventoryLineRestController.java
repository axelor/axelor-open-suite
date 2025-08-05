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
package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.rest.dto.InventoryLinePostRequest;
import com.axelor.apps.stock.rest.dto.InventoryLinePutRequest;
import com.axelor.apps.stock.rest.dto.InventoryLineResponse;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.apps.stock.translation.ITranslation;
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

@Path("/aos/inventory-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InventoryLineRestController {

  @Operation(
      summary = "Inventory line update",
      tags = {"Inventory Line"})
  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateInventoryLine(
      @PathParam("id") Long inventoryLineId, InventoryLinePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(InventoryLine.class, inventoryLineId).check();

    InventoryLine inventoryLine =
        ObjectFinder.find(InventoryLine.class, inventoryLineId, requestBody.getVersion());

    Beans.get(InventoryLineService.class)
        .updateInventoryLine(
            inventoryLine,
            requestBody.getRealQty(),
            requestBody.getDescription(),
            requestBody.fetchStockLocation());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.INVENTORY_LINE_UPDATED),
        new InventoryLineResponse(inventoryLine));
  }

  @Operation(
      summary = "Add inventory line",
      tags = {"Inventory Line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response addLineToInventory(InventoryLinePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .writeAccess(Inventory.class, requestBody.getInventoryId())
        .createAccess(InventoryLine.class)
        .check();

    InventoryLine inventoryLine =
        Beans.get(InventoryLineService.class)
            .addLine(
                requestBody.fetchInventory(),
                requestBody.fetchProduct(),
                requestBody.fetchTrackingNumber(),
                requestBody.getRack(),
                requestBody.getRealQty(),
                requestBody.fetchStockLocation());

    return ResponseConstructor.buildCreateResponse(
        inventoryLine, new InventoryLineResponse(inventoryLine));
  }
}
