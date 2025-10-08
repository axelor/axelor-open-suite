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
package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.MapGroup;
import com.axelor.apps.base.rest.dto.MapGroupResponse;
import com.axelor.apps.base.service.mapConfigurator.MapGroupService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;

@Path("/aos/map-group")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MapGroupRestController {

  @Operation(
      summary = "Compute map group",
      tags = {"Map group"})
  @Path("/compute/{id}")
  @GET
  @HttpExceptionHandler
  public Response computeData(@PathParam("id") Long id)
      throws AxelorException, JSONException, ClassNotFoundException {
    new SecurityCheck().readAccess(MapGroup.class, id).check();
    MapGroup mapGroup = ObjectFinder.find(MapGroup.class, id, ObjectFinder.NO_VERSION);

    List<Map<String, Object>> data = Beans.get(MapGroupService.class).computeData(mapGroup);
    MapGroupResponse response = new MapGroupResponse(mapGroup, data);

    return ResponseConstructor.build(Response.Status.OK, response);
  }
}
