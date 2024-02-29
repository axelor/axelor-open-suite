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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLinePostRequest;
import com.axelor.apps.hr.rest.dto.TimesheetLinePutRequest;
import com.axelor.apps.hr.rest.dto.TimesheetLineResponse;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/timesheet-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetLineRestController {
  @Operation(
      summary = "Create a timesheet line",
      tags = {"Timesheet line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createTimesheetLine(TimesheetLinePostRequest requestBody) throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    Timesheet timesheet = requestBody.fetchTimesheet();
    TimesheetLine timesheetLine =
        Beans.get(TimesheetLineCreateService.class)
            .createTimesheetLine(
                requestBody.fetchProject(),
                requestBody.fetchProjectTask(),
                requestBody.fetchProduct(),
                requestBody.getDate(),
                requestBody.fetchTimesheet(),
                requestBody.getDuration(),
                requestBody.getComments(),
                requestBody.isToInvoice());
    Beans.get(TimesheetPeriodComputationService.class).setComputedPeriodTotal(timesheet);

    return ResponseConstructor.buildCreateResponse(
        timesheetLine, new TimesheetLineResponse(timesheetLine));
  }

  @Operation(
      summary = "Update a timesheet line",
      tags = {"Timesheet line"})
  @Path("/update/{timesheetLineId}")
  @PUT
  @HttpExceptionHandler
  public Response updateTimesheetLine(
      @PathParam("timesheetLineId") Long timesheetLineId, TimesheetLinePutRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    TimesheetLine timesheetLine =
        ObjectFinder.find(TimesheetLine.class, timesheetLineId, requestBody.getVersion());
    Beans.get(TimesheetLineUpdateService.class)
        .updateTimesheetLine(
            timesheetLine,
            requestBody.fetchProject(),
            requestBody.fetchProjectTask(),
            requestBody.fetchProduct(),
            requestBody.getDuration(),
            requestBody.getDate(),
            requestBody.getComments(),
            requestBody.isToInvoice());
    Beans.get(TimesheetPeriodComputationService.class)
        .setComputedPeriodTotal(timesheetLine.getTimesheet());
    return ResponseConstructor.build(
        Response.Status.OK,
        "Timesheet line successfully updated.",
        new TimesheetLineResponse(timesheetLine));
  }
}
