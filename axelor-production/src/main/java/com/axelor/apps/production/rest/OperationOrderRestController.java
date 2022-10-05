package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.rest.dto.OperationOrderPutRequest;
import com.axelor.apps.production.rest.dto.OperationOrderResponse;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/operation-order")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OperationOrderRestController {

  @Path("/{operationOrderId}")
  @PUT
  @HttpExceptionHandler
  public Response updateOperationOrderStatus(
      @PathParam("operationOrderId") Long operationOrderId, OperationOrderPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(OperationOrder.class).check();

    OperationOrder operationOrder =
        ObjectFinder.find(OperationOrder.class, operationOrderId, requestBody.getVersion());

    Beans.get(OperationOrderRestService.class)
        .updateStatusOfOperationOrder(operationOrder, requestBody.getStatus());

    return ResponseConstructor.build(
        Response.Status.OK,
        "Operation order status successfully updated.",
        new OperationOrderResponse((operationOrder)));
  }
}
