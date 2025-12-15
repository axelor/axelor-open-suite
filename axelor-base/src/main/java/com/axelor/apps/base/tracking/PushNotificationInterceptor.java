package com.axelor.apps.base.tracking;

import com.axelor.apps.base.service.pushnotification.PushNotificationRule;
import com.axelor.auth.db.AuditableModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor for handling push notifications on entity changes.
 *
 * <p>Uses a registration system - notification handlers can register themselves for specific
 * entities/fields.
 */
@SuppressWarnings("serial")
public class PushNotificationInterceptor extends GlobalAuditInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(PushNotificationInterceptor.class);

  // Registry of notification handlers
  private static final Map<String, List<PushNotificationRule>> NOTIFICATION_REGISTRY =
      new ConcurrentHashMap<>();

  private static final ThreadLocal<List<DeferredPushNotification>> DEFERRED_NOTIFICATIONS =
      ThreadLocal.withInitial(ArrayList::new);

  /** Register a notification rule */
  public static void registerRule(PushNotificationRule rule) {
    String key = rule.getEntityClassName();
    NOTIFICATION_REGISTRY.computeIfAbsent(key, k -> new ArrayList<>()).add(rule);
    LOG.info(
        "Registered push notification rule for entity: {}, field: {}",
        rule.getEntityClassName(),
        rule.getFieldName());

    LOG.info("Registered {} rules for push notifications", NOTIFICATION_REGISTRY.size());
  }

  @Override
  public boolean onFlushDirty(
      Object entity,
      Serializable id,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      Type[] types) {

    boolean result =
        super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);

    if (entity instanceof AuditableModel) {
      detectAndScheduleNotifications(entity, currentState, previousState, propertyNames, false);
    }

    return result;
  }

  @Override
  public boolean onSave(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

    boolean result = super.onSave(entity, id, state, propertyNames, types);

    if (entity instanceof AuditableModel) {
      detectAndScheduleNotifications(entity, state, null, propertyNames, true);
    }

    return result;
  }

  @Override
  public void beforeTransactionCompletion(Transaction tx) {
    processDeferredNotifications();
    super.beforeTransactionCompletion(tx);
  }

  @Override
  public void afterTransactionCompletion(Transaction tx) {
    DEFERRED_NOTIFICATIONS.remove();
    super.afterTransactionCompletion(tx);
  }

  /** Detect changes and schedule notifications based on registered rules */
  private void detectAndScheduleNotifications(
      Object entity,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      boolean isNew) {

    String entityClassName = entity.getClass().getSimpleName();

    // Get registered rules for this entity
    List<PushNotificationRule> rules = NOTIFICATION_REGISTRY.get(entityClassName);
    if (rules == null || rules.isEmpty()) {
      return;
    }

    LOG.info("Found {} rules for: {}", rules.size(), entityClassName);

    for (int i = 0; i < propertyNames.length; i++) {
      String fieldName = propertyNames[i];
      Object currentValue = currentState[i];
      Object previousValue = previousState != null ? previousState[i] : null;

      // Check each rule for this entity
      for (PushNotificationRule rule : rules) {
        if (rule.matches(entity, fieldName, previousValue, currentValue, isNew)) {
          DEFERRED_NOTIFICATIONS
              .get()
              .add(
                  new DeferredPushNotification(
                      entity, fieldName, previousValue, currentValue, rule));
        }
      }
    }
  }

  /** Process all deferred notifications */
  private void processDeferredNotifications() {
    List<DeferredPushNotification> notifications = DEFERRED_NOTIFICATIONS.get();
    if (notifications.isEmpty()) {
      return;
    }

    for (DeferredPushNotification notification : notifications) {
      try {
        notification
            .getRule()
            .execute(
                notification.getEntity(),
                notification.getFieldName(),
                notification.getOldValue(),
                notification.getNewValue());
      } catch (Exception e) {
        LOG.error(
            "Error processing push notification for entity: {}, field: {}, rule: {}",
            notification.getEntity().getClass().getSimpleName(),
            notification.getFieldName(),
            notification.getRule().getClass().getSimpleName(),
            e);
      }
    }
  }
}
