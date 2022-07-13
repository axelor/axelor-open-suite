package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.rest.dto.InventoryPutRequest;
import com.axelor.apps.stock.rest.dto.InventoryResponse;
import com.axelor.apps.stock.service.InventoryUpdateService;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/inventory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InventoryRestController {

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
