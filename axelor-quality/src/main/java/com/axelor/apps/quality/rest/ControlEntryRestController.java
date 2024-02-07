package com.axelor.apps.quality.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.rest.dto.ControlEntryPostRequest;
import com.axelor.apps.quality.service.ControlEntryProgressValuesComputeService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/controlentry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ControlEntryRestController {
  @Operation(
      summary = "Get control entry progress values",
      tags = {"Control entry"})
  @Path("/progressValues")
  @POST
  @HttpExceptionHandler
  public Response getProgressValues(ControlEntryPostRequest requestBody) throws AxelorException {
    new SecurityCheck().readAccess(ControlEntry.class).check();
    RequestValidator.validateBody(requestBody);

    ControlEntry controlEntry =
        ObjectFinder.find(
            ControlEntry.class, requestBody.getControlEntryId(), ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        Beans.get(ControlEntryProgressValuesComputeService.class)
            .getProgressValues(
                controlEntry, requestBody.getCharacteristicId(), requestBody.getSampleId()));
  }
}
