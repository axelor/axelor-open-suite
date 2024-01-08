package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.rest.dto.TSTimerPostRequest;
import com.axelor.apps.hr.rest.dto.TSTimerPutRequest;
import com.axelor.apps.hr.rest.dto.TSTimerResponse;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerCreateService;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
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
@Path("/aos/timesheet")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetRestController {
  @Operation(
      summary = "Create TSTimer",
      tags = {"TSTimer"})
  @Path("/timer")
  @POST
  @HttpExceptionHandler
  public Response createTSTimer(TSTimerPostRequest requestBody) throws AxelorException {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer =
        Beans.get(TimesheetTimerCreateService.class)
            .createOrUpdateTimer(
                requestBody.fetchEmployee(),
                requestBody.fetchProject(),
                requestBody.fetchProjectTask(),
                requestBody.fetchProduct());
    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully created.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Update TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/update/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response updateTSTimer(@PathParam("timerId") Long timerId, TSTimerPutRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);

    Beans.get(TimesheetTimerCreateService.class)
        .updateTimer(
            timer,
            requestBody.fetchEmployee(),
            requestBody.fetchProject(),
            requestBody.fetchProjectTask(),
            requestBody.fetchProduct());

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Reset TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/reset/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response resetTSTimer(@PathParam("timerId") Long timerId) {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);
    Beans.get(TimesheetTimerCreateService.class).resetTimer(timer);

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Start TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/start/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response startTSTimer(@PathParam("timerId") Long timerId) {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);
    Beans.get(TimesheetTimerService.class).start(timer);

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Pause TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/pause/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response pauseTSTimer(@PathParam("timerId") Long timerId) {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);
    Beans.get(TimesheetTimerService.class).pause(timer);
    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Stop TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/stop/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response stopTSTimer(@PathParam("timerId") Long timerId) throws AxelorException {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);
    Beans.get(TimesheetTimerService.class).stop(timer);

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Update TSTimer duration",
      tags = {"TSTimer"})
  @Path("/timer/duration/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response updateDurationTSTimer(
      @PathParam("timerId") Long timerId, TSTimerPutRequest requestBody) {
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, ObjectFinder.NO_VERSION);
    Beans.get(TimesheetTimerService.class).setUpdatedDuration(timer, requestBody.getDuration());

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }
}
