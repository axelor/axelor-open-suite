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
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.rest.dto.InventoryPutRequest;
import com.axelor.apps.stock.rest.dto.InventoryResponse;
import com.axelor.apps.stock.service.InventoryUpdateService;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/inventory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InventoryRestController {

  @Operation(
      summary = "Inventory update status",
      tags = {"Inventory"})
  @Path("/update-status/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateStatus(@PathParam("id") Long inventoryId, InventoryPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Arrays.asList(Inventory.class, StockMove.class)).check();

    Inventory inventory = ObjectFinder.find(Inventory.class, inventoryId, requestBody.getVersion());

    Beans.get(InventoryUpdateService.class)
        .updateInventoryStatus(inventory, requestBody.getStatus(), requestBody.fetchUser());

    return ResponseConstructor.build(
        Response.Status.OK, "Inventory successfully updated", new InventoryResponse(inventory));
  }
}
