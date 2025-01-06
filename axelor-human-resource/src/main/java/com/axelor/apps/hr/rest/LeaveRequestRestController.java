package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestCheckDurationGetRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestDurationResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestRefusalPutRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestResponse;
import com.axelor.apps.hr.service.leave.LeaveRequestCancelService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestRefuseService;
import com.axelor.apps.hr.service.leave.LeaveRequestSendService;
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
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
      summary = "Compute leave request duration",
      tags = {"Leave request"})
  @Path("/compute-duration")
  @GET
  @HttpExceptionHandler
  public Response computeDuration(LeaveRequestCheckDurationGetRequest requestBody)
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
}
