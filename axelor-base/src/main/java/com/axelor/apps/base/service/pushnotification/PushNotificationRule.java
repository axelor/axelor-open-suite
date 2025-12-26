package com.axelor.apps.base.service.pushnotification;

import java.util.Objects;

/**
 * Abstract base class for push notification rules. Extend this class to create notification rules.
 *
 * <p>Each rule defines: 1. Which entity it applies to 2. Which field changes trigger it 3. What
 * conditions must be met 4. What to do when triggered
 */
public abstract class PushNotificationRule {

  private final String entityClassName;
  private final String fieldName;

  protected PushNotificationRule(String entityClassName, String fieldName) {
    this.entityClassName = entityClassName;
    this.fieldName = fieldName;
  }

  public String getEntityClassName() {
    return entityClassName;
  }

  public String getFieldName() {
    return fieldName;
  }

  /**
   * Check if this rule matches the given change. Override this for custom matching logic.
   *
   * @param entity The entity being changed
   * @param fieldname The field that changed
   * @param oldValue The old value
   * @param newValue The new value
   * @param isNew True if this is a new entity being created
   * @return true if this rule should trigger
   */
  public boolean matches(
      Object entity, String fieldname, Object oldValue, Object newValue, boolean isNew) {

    // Field must match
    if (!this.fieldName.equals(fieldname)) {
      return false;
    }

    // If it's a new entity, check if we should trigger on creation
    if (isNew) {
      return shouldTriggerOnCreate(entity, newValue);
    }

    // For updates, check if value actually changed
    if (Objects.equals(oldValue, newValue)) {
      return false;
    }

    // Check custom conditions
    return shouldTrigger(entity, oldValue, newValue);
  }

  /**
   * Override this to define conditions for triggering on entity creation. Default: trigger if new
   * value is not null.
   */
  protected boolean shouldTriggerOnCreate(Object entity, Object newValue) {
    return newValue != null;
  }

  /**
   * Override this to define custom conditions for triggering on updates. Default: trigger if new
   * value is not null.
   */
  protected boolean shouldTrigger(Object entity, Object oldValue, Object newValue) {
    return newValue != null;
  }

  /**
   * Execute the notification logic. This is called when the rule matches and transaction is about
   * to commit.
   *
   * @param entity The entity that changed
   * @param fieldName The field that changed
   * @param oldValue The old value (null for new entities)
   * @param newValue The new value
   */
  public abstract void execute(Object entity, String fieldName, Object oldValue, Object newValue);
}
