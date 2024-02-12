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
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.rest.dto.TimesheetPostRequest;
import com.axelor.apps.hr.rest.dto.TimesheetPutRequest;
import com.axelor.apps.hr.rest.dto.TimesheetResponse;
import com.axelor.apps.hr.service.timesheet.TimesheetCheckResponseService;
import com.axelor.apps.hr.service.timesheet.TimesheetCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowService;
import com.axelor.apps.hr.service.timesheet.timer.TimerTimesheetGenerationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.axelor.web.ITranslation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/timesheet")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetRestController {
  @Operation(
      summary = "Create a timesheet",
      tags = {"Timesheet"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createTimesheet(TimesheetPostRequest requestBody) throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    Timesheet timesheet =
        Beans.get(TimesheetCreateService.class)
            .createTimesheet(requestBody.getFromDate(), requestBody.getToDate());
    Beans.get(TimerTimesheetGenerationService.class)
        .addTimersToTimesheet(requestBody.fetchTSTimers(), timesheet);
    Beans.get(TimesheetPeriodComputationService.class).setComputedPeriodTotal(timesheet);

    return ResponseConstructor.buildCreateResponse(timesheet, new TimesheetResponse(timesheet));
  }

  @Operation(
      summary = "Add timers to timesheet",
      tags = {"Timesheet"})
  @Path("/add-timer/{timesheetId}")
  @PUT
  @HttpExceptionHandler
  public Response addTimersToTimesheet(
      @PathParam("timesheetId") Long timesheetId, TimesheetPutRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    Timesheet timesheet = ObjectFinder.find(Timesheet.class, timesheetId, requestBody.getVersion());
    Beans.get(TimerTimesheetGenerationService.class)
        .addTimersToTimesheet(requestBody.fetchTSTimers(), timesheet);
    Beans.get(TimesheetPeriodComputationService.class).setComputedPeriodTotal(timesheet);

    return ResponseConstructor.build(
        Response.Status.OK, "Timesheet successfully updated.", new TimesheetResponse(timesheet));
  }

  @Operation(
      summary = "Update timesheet status",
      tags = {"Timesheet"})
  @Path("/status/{timesheetId}")
  @PUT
  @HttpExceptionHandler
  public Response updateTimesheetStatus(
      @PathParam("timesheetId") Long timesheetId, TimesheetPutRequest requestBody)
      throws AxelorException, JSONException, IOException, ClassNotFoundException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    TimesheetWorkflowService timesheetWorkflowService = Beans.get(TimesheetWorkflowService.class);
    Timesheet timesheet = ObjectFinder.find(Timesheet.class, timesheetId, requestBody.getVersion());

    switch (requestBody.getToStatus()) {
      case TimesheetPutRequest.TIMESHEET_UPDATE_CONFIRM:
        timesheetWorkflowService.completeOrConfirm(timesheet);
        break;
      case TimesheetPutRequest.TIMESHEET_UPDATE_VALIDATE:
        timesheetWorkflowService.validateAndSendValidationEmail(timesheet);
        break;
      case TimesheetPutRequest.TIMESHEET_UPDATE_REFUSE:
        timesheetWorkflowService.refuseAndSendRefusalEmail(
            timesheet, requestBody.getGroundForRefusal());
        break;
      case TimesheetPutRequest.TIMESHEET_UPDATE_CANCEL:
        timesheetWorkflowService.cancelAndSendCancellationEmail(timesheet);
        break;
      default:
        break;
    }

    return ResponseConstructor.build(
        Response.Status.OK, "Timesheet successfully updated.", new TimesheetResponse(timesheet));
  }

  @Operation(
      summary = "Check timesheet",
      tags = {"Timesheet"})
  @Path("/check/{timesheetId}")
  @GET
  @HttpExceptionHandler
  public Response checkExpense(@PathParam("timesheetId") Long timesheetId) throws AxelorException {
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();
    Timesheet timesheet = ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.CHECK_RESPONSE_RESPONSE),
        Beans.get(TimesheetCheckResponseService.class).createResponse(timesheet));
  }

  @Operation(
      summary = "Convert timesheet period total",
      tags = {"Timesheet"})
  @Path("/convertPeriod/{timesheetId}")
  @GET
  @HttpExceptionHandler
  public Response convertPeriodTotal(@PathParam("timesheetId") Long timesheetId)
      throws AxelorException {
    new SecurityCheck().readAccess(Expense.class).check();
    Timesheet timesheet = ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        "Timesheet converted period total.",
        Map.of(
            "periodTotalConvertTitle",
            Beans.get(TimesheetService.class).getPeriodTotalConvertTitle(timesheet),
            "periodTotalConvert",
            Beans.get(TimesheetLineService.class)
                .computeHoursDuration(timesheet, timesheet.getPeriodTotal(), false)));
  }
}
