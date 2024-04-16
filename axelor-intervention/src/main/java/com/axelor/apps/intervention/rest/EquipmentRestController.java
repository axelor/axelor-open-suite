package com.axelor.apps.equipment.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Picture;
import com.axelor.apps.intervention.rest.dto.EquipmentPicturePutRequest;
import com.axelor.apps.intervention.rest.dto.EquipmentResponse;
import com.axelor.apps.intervention.service.EquipmentRestService;
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
@Path("/aos/equipment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EquipmentRestController {
  @Operation(
      summary = "Add picture to equipment",
      tags = {"Equipment"})
  @Path("/add-picture/{equipmentId}")
  @PUT
  @HttpExceptionHandler
  public Response addPicture(
      @PathParam("equipmentId") Long equipmentId, EquipmentPicturePutRequest request)
      throws AxelorException {
    new SecurityCheck()
        .writeAccess(Equipment.class, equipmentId)
        .writeAccess(Picture.class)
        .createAccess(Picture.class)
        .check();
    RequestValidator.validateBody(request);

    EquipmentRestService equipmentRestService = Beans.get(EquipmentRestService.class);
    Equipment equipment = ObjectFinder.find(Equipment.class, equipmentId, request.getVersion());
    equipmentRestService.addPicture(request, equipment);

    return ResponseConstructor.build(
        Response.Status.OK, "Picture successfully added.", new EquipmentResponse(equipment));
  }

  @Operation(
      summary = "Remove picture to equipment",
      tags = {"Equipment"})
  @Path("/remove-picture/{equipmentId}")
  @PUT
  @HttpExceptionHandler
  public Response removeEquipmentPicture(
      @PathParam("equipmentId") Long equipmentId, EquipmentPicturePutRequest request)
      throws AxelorException {
    new SecurityCheck()
        .writeAccess(Equipment.class, equipmentId)
        .removeAccess(Picture.class, request.getPictureId())
        .check();
    RequestValidator.validateBody(request);

    EquipmentRestService equipmentRestService = Beans.get(EquipmentRestService.class);
    Equipment equipment = ObjectFinder.find(Equipment.class, equipmentId, request.getVersion());
    equipmentRestService.removePicture(request, equipment);

    return ResponseConstructor.build(
        Response.Status.OK, "Picture successfully removed.", new EquipmentResponse(equipment));
  }
}
