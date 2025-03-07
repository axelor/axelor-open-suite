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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.rest.dto.LeaveDaysToDatePostRequest;
import com.axelor.apps.hr.rest.dto.LeaveDaysToDateResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestCheckDurationPostRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreatePostRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestDurationResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestRefusalPutRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestResponse;
import com.axelor.apps.hr.service.leave.LeaveRequestCancelService;
import com.axelor.apps.hr.service.leave.LeaveRequestCheckResponseService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestRefuseService;
import com.axelor.apps.hr.service.leave.LeaveRequestSendService;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.LeaveRequestValidateService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDayDurationService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/leave-request")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaveRequestRestController {

  @Operation(
      summary = "Send leave request",
      tags = {"Leave request"})
  @Path("/send/{leaveRequestId}")
  @PUT
  @HttpExceptionHandler
  public Response sendLeaveRequest(
      @PathParam("leaveRequestId") Long leaveRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(LeaveRequest.class, leaveRequestId).check();

    LeaveRequest leaveRequest =
        ObjectFinder.find(LeaveRequest.class, leaveRequestId, requestBody.getVersion());
    Beans.get(LeaveRequestSendService.class).send(leaveRequest);

    try {
      Beans.get(LeaveRequestMailService.class).sendConfirmationEmail(leaveRequest);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED_NO_MAIL),
          new LeaveRequestResponse(leaveRequest));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED),
        new LeaveRequestResponse(leaveRequest));
  }

  @Operation(
      summary = "Validate leave request",
      tags = {"Leave request"})
  @Path("/validate/{leaveRequestId}")
  @PUT
  @HttpExceptionHandler
  public Response validateLeaveRequest(
      @PathParam("leaveRequestId") Long leaveRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(LeaveRequest.class, leaveRequestId).check();

    LeaveRequest leaveRequest =
        ObjectFinder.find(LeaveRequest.class, leaveRequestId, requestBody.getVersion());
    Beans.get(LeaveRequestValidateService.class).validate(leaveRequest);

    try {
      Beans.get(LeaveRequestMailService.class).sendValidationEmail(leaveRequest);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED_NO_MAIL),
          new LeaveRequestResponse(leaveRequest));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED),
        new LeaveRequestResponse(leaveRequest));
  }

  @Operation(
      summary = "Reject leave request",
      tags = {"Leave request"})
  @Path("/reject/{leaveRequestId}")
  @PUT
  @HttpExceptionHandler
  public Response rejectLeaveRequest(
      @PathParam("leaveRequestId") Long leaveRequestId, LeaveRequestRefusalPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(LeaveRequest.class, leaveRequestId).check();

    LeaveRequest leaveRequest =
        ObjectFinder.find(LeaveRequest.class, leaveRequestId, requestBody.getVersion());
    Beans.get(LeaveRequestRefuseService.class)
        .refuse(leaveRequest, requestBody.getGroundForRefusal());

    try {
      Beans.get(LeaveRequestMailService.class).sendRefusalEmail(leaveRequest);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED_NO_MAIL),
          new LeaveRequestResponse(leaveRequest));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED),
        new LeaveRequestResponse(leaveRequest));
  }

  @Operation(
      summary = "Cancel leave request",
      tags = {"Leave request"})
  @Path("/cancel/{leaveRequestId}")
  @PUT
  @HttpExceptionHandler
  public Response cancelLeaveRequest(
      @PathParam("leaveRequestId") Long leaveRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(LeaveRequest.class, leaveRequestId).check();

    LeaveRequest leaveRequest =
        ObjectFinder.find(LeaveRequest.class, leaveRequestId, requestBody.getVersion());
    Beans.get(LeaveRequestCancelService.class).cancel(leaveRequest);

    try {
      Beans.get(LeaveRequestMailService.class).sendCancellationEmail(leaveRequest);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED_NO_MAIL),
          new LeaveRequestResponse(leaveRequest));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_UPDATED),
        new LeaveRequestResponse(leaveRequest));
  }

  @Operation(
      summary = "Create leave request",
      tags = {"Leave request"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createLeaveRequest(LeaveRequestCreatePostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(LeaveRequest.class).readAccess(LeaveReason.class).check();
    LeaveRequestCreateRestService leaveRequestCreateRestService =
        Beans.get(LeaveRequestCreateRestService.class);
    leaveRequestCreateRestService.checkLeaveRequestCreatePostRequest(requestBody);
    List<Long> leaveRequestIds =
        leaveRequestCreateRestService.createLeaveRequests(
            requestBody.getFromDate(), requestBody.getStartOnSelect(), requestBody.getRequests());

    if (leaveRequestIds.size() != requestBody.getRequests().size()) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.API_LEAVE_REQUEST_CREATE_SUCCESS_WITH_ERRORS),
          leaveRequestCreateRestService.createLeaveRequestResponse(leaveRequestIds));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_CREATE_SUCCESS),
        leaveRequestCreateRestService.createLeaveRequestResponse(leaveRequestIds));
  }

  @Operation(
      summary = "Compute leave request duration",
      tags = {"Leave request"})
  @Path("/compute-duration")
  @POST
  @HttpExceptionHandler
  public Response computeDuration(LeaveRequestCheckDurationPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);

    User user = AuthUtils.getUser();
    Company company = Optional.ofNullable(user).map(User::getActiveCompany).orElse(null);
    Employee employee = Optional.ofNullable(user).map(User::getEmployee).orElse(null);

    BigDecimal duration =
        Beans.get(LeaveRequestComputeDayDurationService.class)
            .computeDurationInDays(
                company,
                employee,
                requestBody.getFromDate(),
                requestBody.getToDate(),
                requestBody.getStartOnSelect(),
                requestBody.getEndOnSelect());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_COMPUTE_DURATION),
        new LeaveRequestDurationResponse(duration));
  }

  @Operation(
      summary = "Compute leave available to a specific date",
      tags = {"Leave request"})
  @Path("/compute-leave-available")
  @POST
  @HttpExceptionHandler
  public Response getLeaveDaysToDate(LeaveDaysToDatePostRequest requestBody) {
    new SecurityCheck().readAccess(LeaveReason.class, requestBody.getLeaveReasonId()).check();

    Employee employee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.API_LEAVE_REQUEST_LEAVE_DAYS_TO_DATE_COMPUTATION),
        new LeaveDaysToDateResponse(
            Beans.get(LeaveRequestService.class)
                .getLeaveDaysToDate(
                    requestBody.getToDate().atStartOfDay(),
                    employee,
                    requestBody.fetchLeaveReason())));
  }

  @Operation(
      summary = "Check leave request",
      tags = {"Leave request"})
  @Path("/check/{leaveRequestId}")
  @GET
  @HttpExceptionHandler
  public Response checkLeaveRequest(@PathParam("leaveRequestId") Long leaveRequestId) {
    new SecurityCheck().readAccess(LeaveRequest.class, leaveRequestId).check();
    LeaveRequest leaveRequest =
        ObjectFinder.find(LeaveRequest.class, leaveRequestId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.CHECK_RESPONSE_RESPONSE),
        Beans.get(LeaveRequestCheckResponseService.class).createResponse(leaveRequest));
  }
}
