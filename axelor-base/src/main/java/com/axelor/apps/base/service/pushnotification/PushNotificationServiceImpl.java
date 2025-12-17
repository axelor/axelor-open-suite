package com.axelor.apps.base.service.pushnotification;

import com.axelor.apps.base.db.PushToken;
import com.axelor.apps.base.db.repo.pushtoken.PushTokenBaseRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushNotificationServiceImpl implements PushNotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(PushNotificationServiceImpl.class);

  private final PushTokenBaseRepository pushTokenRepo = Beans.get(PushTokenBaseRepository.class);

  private final FirebaseInitializer firebaseInitializer = Beans.get(FirebaseInitializer.class);

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

    LOG.info("Registered token for user {}", pushToken.getEmployee());
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
      return;
    }

    if (!firebaseInitializer.isInitialized()) {
      LOG.debug("Push skipped: Firebase not initialized");
      return;
    }

    List<PushToken> tokens = pushTokenRepo.findByUser(user).fetch();
    List<String> activeTokens = new ArrayList<>();

    for (PushToken pt : tokens) {
      if (Boolean.TRUE.equals(pt.getIsActive())) {
        activeTokens.add(pt.getToken());
      }
    }

    if (activeTokens.isEmpty()) {
      LOG.debug("No active push tokens for user {}", user.getFullName());
      return;
    }

    // Send notification to all active devices for this user
    for (String token : activeTokens) {
      sendToSingleToken(token, title, body, data, user);
    }
  }

  @Override
  public void sendNotificationToUsers(
      List<User> users, String title, String body, Map<String, Object> data) {

    if (users == null || users.isEmpty()) {
      return;
    }

    for (User user : users) {
      sendNotificationToUser(user, title, body, data);
    }
  }

  private void sendToSingleToken(
      String token, String title, String body, Map<String, Object> data, User user) {

    // Using this to log a clear error message of the faulty token
    Long pushTokenId = pushTokenRepo.findByToken(token).getId();

    try {

      Message.Builder builder =
          Message.builder()
              .setToken(token)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build());

      if (data != null && !data.isEmpty()) {
        data.forEach((k, v) -> builder.putData(k, String.valueOf(v)));
      }

      FirebaseMessaging.getInstance().send(builder.build());

      LOG.debug("Push sent successfully to token {}", token);

      // Update lastUsedOn for provenance
      updateLastUsed(token);

    } catch (Exception e) {
      LOG.warn("Failed to send push notification to user {} " +
              "for push token with id {}: {}",
              user.getFullName(), pushTokenId, formatErrorMessage(e));

      LOG.debug("Full stack trace: ", e);
      handleSendError(token, e);
    }
  }

  @Transactional
  private void handleSendError(String token, Exception e) {
    String msg = e.getMessage();
    if (msg == null) {
      return;
    }

    if (msg.contains("registration-token-not-registered")
        || msg.contains("InvalidRegistration")
        || msg.contains("NotRegistered")) {

      deactivateToken(token);
      LOG.info("Deactivated invalid FCM token {}", token);
    }
  }

  @Override
  public boolean hasActivePushToken(User user) {
    return user != null
        && pushTokenRepo.findByUser(user).fetch().stream().anyMatch(PushToken::getIsActive);
  }

  @Transactional
  protected void updateLastUsed(String token) {
    PushToken pushToken = pushTokenRepo.findByToken(token);

    if (pushToken != null && Boolean.TRUE.equals(pushToken.getIsActive())) {
      pushToken.setLastUsedOn(LocalDateTime.now());
      pushTokenRepo.save(pushToken);
    }
  }

  private String formatErrorMessage(Exception e) {
    if (e instanceof FirebaseMessagingException) {
      FirebaseMessagingException fbme = (FirebaseMessagingException) e;
      return fbme.getMessagingErrorCode() + "- " + fbme.getMessage();
    }

    return e.getClass().getSimpleName() + "- " + e.getMessage();
  }
}
