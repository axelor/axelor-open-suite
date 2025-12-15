package com.axelor.apps.base.service.pushnotification;

import com.axelor.apps.base.db.PushToken;
import com.axelor.apps.base.db.repo.pushtoken.PushTokenBaseRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushNotificationServiceImpl implements PushNotificationService {
  private static final Logger LOG = LoggerFactory.getLogger(PushNotificationServiceImpl.class);
  private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

  private PushTokenBaseRepository pushTokenRepo = Beans.get(PushTokenBaseRepository.class);

  @Override
  @Transactional
  public PushToken registerToken(User user, String token, String deviceId) {
    PushToken pushToken = pushTokenRepo.findByToken(token);

    if (pushToken == null) {
      pushToken = new PushToken();
      pushToken.setToken(token);
    }

    pushToken.setEmployee(user);
    pushToken.setDeviceId(deviceId);
    pushToken.setIsActive(true);
    pushToken.setLastUsedOn(LocalDateTime.now());

    return pushTokenRepo.save(pushToken);
  }

  @Override
  @Transactional
  public void deactivateToken(String token) {
    PushToken pushToken = pushTokenRepo.findByToken(token);
    if (pushToken != null) {
      pushToken.setIsActive(false);
      pushTokenRepo.save(pushToken);
    }
  }

  @Override
  public void sendNotificationToUser(
      User user, String title, String body, Map<String, Object> data) {
    if (user == null) {
      LOG.warn("Cannot send notification: user is null");
      return;
    }

    List<PushToken> tokens = pushTokenRepo.findByUser(user).fetch();
    List<String> activeTokens = new ArrayList<>();

    // Send push notification to all user's devices
    for (PushToken pushToken : tokens) {
      if (pushToken.getIsActive()) {
        activeTokens.add(pushToken.getToken());
      }
    }

    if (activeTokens.isEmpty()) {
      LOG.info("No active push tokens found for user: {}", user.getFullName());
      return;
    }

    LOG.info("{} active tokens found for user: {}", activeTokens.size(), user.getFullName());

    sendPushNotifications(activeTokens, title, body, data);
  }

  @Override
  public void sendNotificationToUsers(
      List<User> users, String title, String body, Map<String, Object> data) {
    if (users == null || users.isEmpty()) {
      LOG.warn("Cannot send notification: users list is empty");
      return;
    }

    LOG.info("Sending notifications to {} users", users.size());

    for (User user : users) {
      sendNotificationToUser(user, title, body, data);
    }
  }

  @Override
  public boolean hasActivePushToken(User user) {
    if (user == null) return false;

    List<PushToken> tokens = pushTokenRepo.findByUser(user).fetch();

    // Check if user has any active token
    for (PushToken token : tokens) {
      if (token.getIsActive()) {
        return true;
      }
    }

    return false;
  }

  /** Core method to send push notifications via Expo API */
  private void sendPushNotifications(
      List<String> tokens, String title, String body, Map<String, Object> data) {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      List<Map<String, Object>> messages = new ArrayList<>();

      for (String token : tokens) {
        Map<String, Object> message = new HashMap<>();
        message.put("to", token);
        message.put("title", title);
        message.put("body", body);

        if (data != null && !data.isEmpty()) {
          message.put("data", data);
        }

        messages.add(message);
      }

      ObjectMapper mapper = new ObjectMapper();
      String jsonPayload = mapper.writeValueAsString(messages);

      HttpPost httpPost = new HttpPost(EXPO_PUSH_URL);
      httpPost.setHeader("Content-Type", "application/json");
      httpPost.setHeader("Accept", "application/json");
      httpPost.setEntity(new StringEntity(jsonPayload, "UTF-8"));

      HttpResponse response = httpClient.execute(httpPost);
      String responseBody = EntityUtils.toString(response.getEntity());

      int statusCode = response.getStatusLine().getStatusCode();

      // Cover all successful status codes.
      if (statusCode >= HttpStatusCodes.STATUS_CODE_OK
          && statusCode < HttpStatusCodes.STATUS_CODE_MULTIPLE_CHOICES) {
        LOG.info("Push notifications sent successfully: {}", responseBody);
        handleExpoResponse(responseBody);
      } else {
        LOG.error(
            "Failed to send push notifications. Status: {}, Response: {}",
            statusCode,
            responseBody);
      }

    } catch (Exception e) {
      LOG.error("Error sending push notifications", e);
    }
  }

  /** Handle Expo API response and deactivate invalid tokens */
  @Transactional
  private void handleExpoResponse(String responseBody) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> response = mapper.readValue(responseBody, Map.class);

      if (!response.containsKey("data")) {
        return;
      }

      List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

      for (Map<String, Object> item : dataList) {
        processResponseItem(item);
      }
    } catch (Exception e) {
      LOG.error("Error handling Expo response", e);
    }
  }

  private void processResponseItem(Map<String, Object> item) {
    String status = (String) item.get("status");

    if (!"error".equals(status)) {
      return;
    }

    Map<String, Object> details = (Map<String, Object>) item.get("details");
    if (details == null) {
      return;
    }

    String error = (String) details.get("error");
    if (!"DeviceNotRegistered".equals(error)) {
      return;
    }

    String token = (String) item.get("to");
    if (token != null) {
      deactivateToken(token);
      LOG.info("Deactivated invalid token: {}", token);
    }
  }
}
