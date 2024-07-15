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
package com.axelor.apps.intervention.rest;

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
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
