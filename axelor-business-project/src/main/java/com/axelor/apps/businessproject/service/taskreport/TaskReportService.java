package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;

public interface TaskReportService {

  /** Check if all tasks for a project habe been reported in this task report. */
  boolean checkIfAllTasksReported(TaskReport report);

  void createTaskReport(Project project);

  /** Get reported task count per total project task */
  String getReportedTaskCount(TaskReport report);

  /** Build domain filter for tasks in task member report */
  String buildTaskDomainFilter(TaskReport taskReport, Long currentTaskId, User user);
}
