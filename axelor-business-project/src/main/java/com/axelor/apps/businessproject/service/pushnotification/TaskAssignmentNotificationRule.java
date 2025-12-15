package com.axelor.apps.businessproject.service.pushnotification;

import com.axelor.apps.base.service.pushnotification.PushNotificationRule;
import com.axelor.apps.base.service.pushnotification.PushNotificationService;
import com.axelor.apps.base.tracking.PushNotificationInterceptor;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notification rule for task assignment changes. Sends push notification when a task is assigned to
 * a user.
 */
public class TaskAssignmentNotificationRule extends PushNotificationRule {

  private static final Logger LOG = LoggerFactory.getLogger(TaskAssignmentNotificationRule.class);

  @Inject private PushNotificationService pushNotificationService;

  public TaskAssignmentNotificationRule() {
    super("ProjectTask", "assignedTo");

    PushNotificationInterceptor.registerRule(this); // Register this notification rule.
    LOG.debug("Task AssignemntNotificationRule registered");
  }

  @Override
  public void execute(Object entity, String fieldName, Object oldValue, Object newValue) {
    if (!(entity instanceof ProjectTask) || !(newValue instanceof User)) {
      return;
    }

    ProjectTask task = (ProjectTask) entity;
    User assignee = (User) newValue;

    if (!pushNotificationService.hasActivePushToken(assignee)) return;

    try {
      String title = I18n.get("New Task Assigned");
      String body = String.format(I18n.get("You have been assigned to: %s"), task.getFullName());

      Map<String, Object> data = buildTaskData(task);
      data.put("type", I18n.get("TASK_ASSIGNMENT"));

      pushNotificationService.sendNotificationToUser(assignee, title, body, data);

      LOG.info(
          "Push notification sent for task assignment - Task: {}, User: {}",
          task.getName(),
          assignee.getCode());

    } catch (Exception e) {
      LOG.error("Error sending task assignment notification", e);
    }
  }

  private Map<String, Object> buildTaskData(ProjectTask task) {
    Map<String, Object> data = new HashMap<>();

    data.put("taskName", task.getFullName());

    if (task.getDescription() != null) {
      data.put("description", task.getDescription());
    }

    if (task.getTaskDate() != null) {
      data.put("taskDate", task.getTaskDate().toString());
    }

    if (task.getTaskEndDate() != null) {
      data.put("taskEndDate", task.getTaskEndDate().toString());
    }

    if (task.getPriority() != null) {
      data.put("priority", task.getPriority());
    }

    if (task.getStatus() != null) {
      data.put("status", task.getStatus().getName());
    }

    if (task.getProject() != null) {
      data.put("projectName", task.getProject().getName());
    }

    if (task.getProjectTaskCategory() != null) {
      data.put("category", task.getProjectTaskCategory());
    }

    return data;
  }
}
