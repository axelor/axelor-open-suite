package com.axelor.apps.businessproduction.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproduction.rest.dto.TimesheetLineBusinessPostRequest;
import com.axelor.apps.businessproduction.rest.dto.TimesheetLineBusinessPutRequest;
import com.axelor.apps.businessproduction.service.TimesheetLineCreateBusinessService;
import com.axelor.apps.businessproduction.service.TimesheetLineUpdateBusinessService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLineResponse;
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
@Path("/aos/business/timesheet-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetLineBusinessRestController {

  @Operation(
      summary = "Create a timesheet line",
      tags = {"Timesheet line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createTimesheetLine(TimesheetLineBusinessPostRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    Timesheet timesheet = requestBody.fetchTimesheet();
    TimesheetLine timesheetLine =
        Beans.get(TimesheetLineCreateBusinessService.class)
            .createTimesheetLine(
                requestBody.fetchProject(),
                requestBody.fetchProjectTask(),
                null,
                requestBody.getDate(),
                requestBody.fetchTimesheet(),
                requestBody.getDuration(),
                requestBody.getComments(),
                requestBody.isToInvoice(),
                requestBody.fetchManufOrder(),
                requestBody.fetchOperationOrder());
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
      @PathParam("timesheetLineId") Long timesheetLineId,
      TimesheetLineBusinessPutRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(Timesheet.class).createAccess(Timesheet.class).check();
    RequestValidator.validateBody(requestBody);

    TimesheetLine timesheetLine =
        ObjectFinder.find(TimesheetLine.class, timesheetLineId, requestBody.getVersion());
    Beans.get(TimesheetLineUpdateBusinessService.class)
        .updateTimesheetLine(
            timesheetLine,
            requestBody.fetchProject(),
            requestBody.fetchProjectTask(),
            requestBody.getDuration(),
            requestBody.getDate(),
            requestBody.getComments(),
            requestBody.isToInvoice(),
            requestBody.fetchManufOrder(),
            requestBody.fetchOperationOrder());
    Beans.get(TimesheetPeriodComputationService.class)
        .setComputedPeriodTotal(timesheetLine.getTimesheet());
    return ResponseConstructor.build(
        Response.Status.OK,
        "Timesheet line successfully updated.",
        new TimesheetLineResponse(timesheetLine));
  }
}
