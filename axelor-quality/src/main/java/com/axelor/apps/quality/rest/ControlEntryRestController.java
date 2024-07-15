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
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    RequestValidator.validateBody(requestBody);
    Long controlEntryId = requestBody.getControlEntryId();
    new SecurityCheck().readAccess(ControlEntry.class, controlEntryId).check();

    ControlEntry controlEntry =
        ObjectFinder.find(ControlEntry.class, controlEntryId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        Beans.get(ControlEntryProgressValuesComputeService.class)
            .getProgressValues(
                controlEntry, requestBody.getCharacteristicId(), requestBody.getSampleId()));
  }
}
