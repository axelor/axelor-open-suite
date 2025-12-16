package com.axelor.apps.base.web.pushnotification;

import com.axelor.apps.base.db.PushToken;
import com.axelor.apps.base.service.pushnotification.PushNotificationServiceImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/rest/push-notification")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PushNotificationController {

  private static final Logger LOG = LoggerFactory.getLogger(PushNotificationController.class);

  @Inject
  private PushNotificationServiceImpl pushNotificationService =
      Beans.get(PushNotificationServiceImpl.class);

  /** Register a push token for the current user Expected payload: token, deviceId */
  @POST
  @Path("/register")
  public Response registerToken(Map<String, Object> payload) {
    try {
      User currentUser = AuthUtils.getUser();
      if (currentUser == null) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", "User not authenticated"))
            .build();
      }

      String token = (String) payload.get("token");
      String deviceId = (String) payload.get("deviceId");

      if (token == null || token.trim().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", "Token is required"))
            .build();
      }

      PushToken pushToken = pushNotificationService.registerToken(currentUser, token, deviceId);

      LOG.info("Push token registered for user: {}, device: {}", currentUser.getCode(), deviceId);

      return Response.ok()
          .entity(
              Map.of(
                  "success",
                  true,
                  "message",
                  "Token registered successfully",
                  "tokenId",
                  pushToken.getId()))
          .build();

    } catch (Exception e) {
      LOG.error("Error registering push token", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Failed to register token"))
          .build();
    }
  }

  /** Deactivate a push token Expected payload: { "token": "ExponentPushToken[xxx]" } */
  @POST
  @Path("/deactivate")
  public Response deactivateToken(Map<String, Object> payload) {
    try {
      String token = (String) payload.get("token");

      if (token == null || token.trim().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", "Token is required"))
            .build();
      }

      pushNotificationService.deactivateToken(token);

      LOG.info("Push token deactivated: {}", token);

      return Response.ok()
          .entity(Map.of("success", true, "message", "Token deactivated successfully"))
          .build();

    } catch (Exception e) {
      LOG.error("Error deactivating push token", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Failed to deactivate token"))
          .build();
    }
  }
}
