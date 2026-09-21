/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.ControlEntrySampleLinePutRequest;
import com.axelor.apps.quality.rest.dto.ControlEntrySampleLineResponse;
import com.axelor.apps.quality.rest.service.ControlEntrySampleLineUpdateAPIService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A control entry sample line is a {@link ControlEntryPlanLine} of type {@link
 * com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository#TYPE_ENTRY_SAMPLE_LINE}: the line
 * of a control entry sample on which the values are measured.
 */
@Path("/aos/control-entry-sample-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ControlEntrySampleLineRestController {

  @Operation(
      summary = "Save the measured values of a control entry sample line and check its conformity",
      tags = {"Control entry sample line"})
  @Path("/{controlEntrySampleLineId}")
  @PUT
  @HttpExceptionHandler
  public Response updateValuesAndCheckConformity(
      @PathParam("controlEntrySampleLineId") Long controlEntrySampleLineId,
      ControlEntrySampleLinePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .writeAccess(ControlEntryPlanLine.class, controlEntrySampleLineId)
        .writeAccess(ControlTypeFieldValue.class, requestBody.getEntryValueIds())
        .check();

    ControlEntryPlanLine controlEntrySampleLine =
        ObjectFinder.find(
            ControlEntryPlanLine.class, controlEntrySampleLineId, requestBody.getVersion());

    // the result of the sample follows the result of its lines
    ControlEntrySample controlEntrySample = controlEntrySampleLine.getControlEntrySample();
    if (controlEntrySample != null) {
      new SecurityCheck().writeAccess(ControlEntrySample.class, controlEntrySample.getId()).check();
    }

    controlEntrySampleLine =
        Beans.get(ControlEntrySampleLineUpdateAPIService.class)
            .updateValuesAndCheckConformity(controlEntrySampleLine, requestBody);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(QualityExceptionMessage.API_CONTROL_ENTRY_SAMPLE_LINE_UPDATED),
        new ControlEntrySampleLineResponse(controlEntrySampleLine));
  }
}
