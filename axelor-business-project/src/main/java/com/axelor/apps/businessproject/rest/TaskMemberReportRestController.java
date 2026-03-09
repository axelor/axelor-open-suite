package com.axelor.apps.businessproject.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.rest.dto.TaskMemberReportPostRequest;
import com.axelor.apps.businessproject.rest.dto.TaskMemberReportResponse;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportCreateService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
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

@Path("/aos/task-member-report")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TaskMemberReportRestController {

  @Operation(
      summary = "Create or update a task member report",
      tags = {"TaskMemberReport"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createOrUpdate(TaskMemberReportPostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(TaskMemberReport.class).check();

    TaskMemberReportCreateService.TaskMemberReportCreationResult result =
        Beans.get(TaskMemberReportCreateService.class)
            .createTaskMemberReport(
                requestBody.fetchTask(),
                requestBody.getStartTime(),
                requestBody.getEndTime(),
                requestBody.getBreakTimeMinutes(),
                requestBody.getDirtAllowance());

    return result.isNew
        ? ResponseConstructor.buildCreateResponse(
            result.tmr, new TaskMemberReportResponse(result.tmr, true))
        : ResponseConstructor.build(
            Response.Status.OK,
            "Task member report updated",
            new TaskMemberReportResponse(result.tmr, false));
  }
}
