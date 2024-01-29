package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.rest.dto.TimesheetLinePostRequest;
import com.axelor.apps.hr.rest.dto.TimesheetResponse;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
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
    Beans.get(TimesheetLineCreateService.class)
        .createTimesheetLine(
            requestBody.fetchProject(),
            requestBody.fetchProjectTask(),
            null,
            requestBody.getDate(),
            requestBody.fetchTimesheet(),
            requestBody.getDuration(),
            requestBody.getComments());
    Beans.get(TimesheetPeriodComputationService.class).computePeriodTotal(timesheet);

    return ResponseConstructor.buildCreateResponse(timesheet, new TimesheetResponse(timesheet));
  }
}
