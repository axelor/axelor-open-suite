package com.axelor.apps.base.tracking;

import com.axelor.apps.base.service.pushnotification.PushNotificationRule;

/** Container for deferred push notifications */
class DeferredPushNotification {
  private final Object entity;
  private final String fieldName;
  private final Object oldValue;
  private final Object newValue;
  private final PushNotificationRule rule;

  DeferredPushNotification(
      Object entity,
      String fieldName,
      Object oldValue,
      Object newValue,
      PushNotificationRule rule) {
    this.entity = entity;
    this.fieldName = fieldName;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.rule = rule;
  }

  public PushNotificationRule getRule() {
    return rule;
  }

  public Object getEntity() {
    return entity;
  }

  public String getFieldName() {
    return fieldName;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
}
