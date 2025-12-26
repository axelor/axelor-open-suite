package com.axelor.apps.base.service.pushnotification;

import com.axelor.apps.base.db.PushToken;
import com.axelor.auth.db.User;
import java.util.List;
import java.util.Map;

public interface PushNotificationService {

  /** Register or update a push token for a user */
  PushToken registerToken(User employee, String token, String deviceId);

  /** Deactivate a push token */
  void deactivateToken(String token);

  /** Send push notification to a specific user */
  void sendNotificationToUser(User user, String title, String body, Map<String, Object> data);

  /** Send push notification to multiple users */
  void sendNotificationToUsers(
      List<User> users, String title, String body, Map<String, Object> data);

  /** Check if a user has any active push token */
  boolean hasActivePushToken(User user);
}
