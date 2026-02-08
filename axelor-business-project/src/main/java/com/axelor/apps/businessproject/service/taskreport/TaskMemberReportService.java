package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.project.db.ProjectTask;

public interface TaskMemberReportService {

  TaskMemberReport getTaskMemberReport(ProjectTask task);

  boolean hasTaskMemberReport(ProjectTask task);
}
