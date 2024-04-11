package com.axelor.apps.intervention.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.rest.dto.InterventionEquipmentPutRequest;
import com.axelor.apps.intervention.rest.dto.InterventionResponse;
import com.axelor.apps.intervention.rest.dto.InterventionStatusPutRequest;
import com.axelor.apps.intervention.service.InterventionRestService;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/intervention")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InterventionRestController {
  @Operation(
      summary = "Update intervention status",
      tags = {"Intervention"})
  @Path("/status/{interventionId}")
  @PUT
  @HttpExceptionHandler
  public Response updateInterventionStatus(
      @PathParam("interventionId") Long interventionId, InterventionStatusPutRequest request)
      throws AxelorException {
    new SecurityCheck().writeAccess(Intervention.class).createAccess(Intervention.class).check();
    RequestValidator.validateBody(request);

    InterventionRestService interventionRestService = Beans.get(InterventionRestService.class);
    Intervention intervention =
        ObjectFinder.find(Intervention.class, interventionId, request.getVersion());
    interventionRestService.updateStatus(request, intervention);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Intervention status successfully updated.",
        new InterventionResponse(intervention));
  }

  @Operation(
      summary = "Add equipment to intervention",
      tags = {"Intervention"})
  @Path("/add-equipment/{interventionId}")
  @PUT
  @HttpExceptionHandler
  public Response addEquipment(
      @PathParam("interventionId") Long interventionId, InterventionEquipmentPutRequest request)
      throws AxelorException {
    new SecurityCheck().writeAccess(Intervention.class).createAccess(Intervention.class).check();
    RequestValidator.validateBody(request);

    InterventionRestService interventionRestService = Beans.get(InterventionRestService.class);
    Intervention intervention =
        ObjectFinder.find(Intervention.class, interventionId, request.getVersion());
    intervention = interventionRestService.addEquipment(request, intervention);
    interventionRestService.updateSurvey(intervention);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Equipment successfully added.",
        new InterventionResponse(intervention));
  }

  @Operation(
      summary = "Remove equipment to intervention",
      tags = {"Intervention"})
  @Path("/remove-equipment/{interventionId}")
  @PUT
  @HttpExceptionHandler
  public Response removeEquipment(
      @PathParam("interventionId") Long interventionId, InterventionEquipmentPutRequest request)
      throws AxelorException {
    new SecurityCheck().writeAccess(Intervention.class).createAccess(Intervention.class).check();
    RequestValidator.validateBody(request);

    InterventionRestService interventionRestService = Beans.get(InterventionRestService.class);
    Intervention intervention =
        ObjectFinder.find(Intervention.class, interventionId, request.getVersion());
    intervention = interventionRestService.removeEquipment(request, intervention);
    interventionRestService.updateSurvey(intervention);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Equipment successfully removed.",
        new InterventionResponse(intervention));
  }
}
