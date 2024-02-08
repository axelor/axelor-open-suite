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
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.rest.dto.TSTimerPostRequest;
import com.axelor.apps.hr.rest.dto.TSTimerPutRequest;
import com.axelor.apps.hr.rest.dto.TSTimerResponse;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerCreateService;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerService;
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
@Path("/aos/timesheet")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetTimerRestController {
  @Operation(
      summary = "Create TSTimer",
      tags = {"TSTimer"})
  @Path("/timer")
  @POST
  @HttpExceptionHandler
  public Response createTSTimer(TSTimerPostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer =
        Beans.get(TimesheetTimerCreateService.class)
            .createOrUpdateTimer(
                requestBody.fetchProject(),
                requestBody.fetchProjectTask(),
                requestBody.fetchProduct(),
                requestBody.getDuration(),
                requestBody.getComments(),
                requestBody.getStartDateTime());
    return ResponseConstructor.buildCreateResponse(timer, new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Update TSTimer",
      tags = {"TSTimer"})
  @Path("/timer/update/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response updateTSTimer(@PathParam("timerId") Long timerId, TSTimerPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();

    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, requestBody.getVersion());

    Beans.get(TimesheetTimerCreateService.class)
        .updateTimer(
            timer,
            null,
            requestBody.fetchProject(),
            requestBody.fetchProjectTask(),
            requestBody.fetchProduct(),
            requestBody.getDuration(),
            requestBody.getComments(),
            requestBody.getStartDateTime());

    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }

  @Operation(
      summary = "Update TSTimer status",
      tags = {"TSTimer"})
  @Path("/timer/status/{timerId}")
  @PUT
  @HttpExceptionHandler
  public Response updateStatusTSTimer(
      @PathParam("timerId") Long timerId, TSTimerPutRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(TSTimer.class).createAccess(TSTimer.class).check();
    TSTimer timer = ObjectFinder.find(TSTimer.class, timerId, requestBody.getVersion());
    TimesheetTimerService timesheetTimerService = Beans.get(TimesheetTimerService.class);

    switch (requestBody.getToStatus()) {
      case TimesheetTimerService.TS_TIMER_UPDATE_START:
        timesheetTimerService.start(timer);
        break;
      case TimesheetTimerService.TS_TIMER_UPDATE_PAUSE:
        timesheetTimerService.pause(timer);
        break;
      case TimesheetTimerService.TS_TIMER_UPDATE_STOP:
        timesheetTimerService.stop(timer);
        break;
      case TimesheetTimerService.TS_TIMER_UPDATE_RESET:
        timesheetTimerService.resetTimer(timer);
        break;
      default:
        break;
    }
    return ResponseConstructor.build(
        Response.Status.OK, "Timer successfully updated.", new TSTimerResponse(timer));
  }
}
