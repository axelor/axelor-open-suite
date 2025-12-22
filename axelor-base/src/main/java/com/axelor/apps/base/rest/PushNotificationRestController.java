package com.axelor.apps.base.rest;

import com.axelor.apps.base.db.PushToken;
import com.axelor.apps.base.rest.dto.PushTokenDeactivationRequest;
import com.axelor.apps.base.rest.dto.PushTokenRegisterRequest;
import com.axelor.apps.base.rest.dto.PushTokenRegisterResponse;
import com.axelor.apps.base.service.pushnotification.PushNotificationService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/aos/push-notification")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PushNotificationRestController {

  private static final Logger LOG = LoggerFactory.getLogger(PushNotificationRestController.class);

  @Operation(
      summary = "Register a push notification token",
      tags = {"Push Notification"})
  @Path("/register")
  @POST
  @HttpExceptionHandler
  public Response registerToken(PushTokenRegisterRequest requestBody) {
    RequestValidator.validateBody(requestBody);

    try {
      User currentUser = AuthUtils.getUser();
      if (currentUser == null) {
        return ResponseConstructor.build(
            Response.Status.UNAUTHORIZED, I18n.get("User not authenticated"));
      }

      PushToken pushToken =
          Beans.get(PushNotificationService.class)
              .registerToken(currentUser, requestBody.getToken(), requestBody.getDeviceId());

      LOG.info(
          "Push token registered for user: {}, device: {}",
          currentUser.getCode(),
          requestBody.getDeviceId());

      return ResponseConstructor.build(
          Response.Status.CREATED,
          I18n.get("Token registered successfully"),
          new PushTokenRegisterResponse(pushToken));

    } catch (Exception e) {
      LOG.error("Error registering push token", e);
      return ResponseConstructor.build(
          Response.Status.INTERNAL_SERVER_ERROR, I18n.get("Failed to register token: "));
    }
  }

  @Operation(
      summary = "Deactivate a push notification token",
      tags = {"Push Notification"})
  @Path("/deactivate")
  @POST
  @HttpExceptionHandler
  public Response deactivateToken(PushTokenDeactivationRequest requestBody) {
    RequestValidator.validateBody(requestBody);

    try {
      Beans.get(PushNotificationService.class).deactivateToken(requestBody.getToken());
      LOG.info("Push token deactivated: {}", requestBody.getToken());

      return ResponseConstructor.build(
          Response.Status.OK, I18n.get("Token deactivated successfully"));
    } catch (Exception e) {
      LOG.error("Error deactivating push token", e);
      return ResponseConstructor.build(
          Response.Status.INTERNAL_SERVER_ERROR, I18n.get("Failed to deactivate token"));
    }
  }
}
