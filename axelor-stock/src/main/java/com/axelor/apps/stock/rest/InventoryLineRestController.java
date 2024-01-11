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
package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.api.ResponseComputeService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.rest.dto.InventoryLinePostRequest;
import com.axelor.apps.stock.rest.dto.InventoryLinePutRequest;
import com.axelor.apps.stock.rest.dto.InventoryLineResponse;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
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
    new SecurityCheck().writeAccess(Arrays.asList(Inventory.class, InventoryLine.class)).check();

    InventoryLine inventoryLine =
        ObjectFinder.find(InventoryLine.class, inventoryLineId, requestBody.getVersion());

    Beans.get(InventoryLineService.class)
        .updateInventoryLine(inventoryLine, requestBody.getRealQty(), requestBody.getDescription());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Inventory line successfully updated",
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
    new SecurityCheck().writeAccess(Inventory.class).createAccess(InventoryLine.class).check();

    InventoryLine inventoryLine =
        Beans.get(InventoryLineService.class)
            .addLine(
                requestBody.fetchInventory(),
                requestBody.fetchProduct(),
                requestBody.fetchTrackingNumber(),
                requestBody.getRack(),
                requestBody.getRealQty());

    return ResponseConstructor.build(
        Response.Status.CREATED,
        Beans.get(ResponseComputeService.class).compute(inventoryLine),
        new InventoryLineResponse(inventoryLine));
  }
}
