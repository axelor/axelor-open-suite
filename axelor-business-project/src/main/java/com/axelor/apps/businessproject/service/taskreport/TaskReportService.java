package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskReport;

public interface TaskReportService {

  /** Check if all tasks for a project habe been reported in this task report. */
  boolean checkIfAllTasksReported(TaskReport report);
}
