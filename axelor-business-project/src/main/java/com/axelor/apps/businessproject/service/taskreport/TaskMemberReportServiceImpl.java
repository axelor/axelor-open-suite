package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;

public class TaskMemberReportServiceImpl implements TaskMemberReportService {

  protected final TaskReportService taskReportService = Beans.get(TaskReportService.class);

  @Override
  public TaskMemberReport getTaskMemberReport(ProjectTask task) {
    if (task == null || task.getProject() == null) return null;

    TaskReport taskReport = taskReportService.getTaskReport(task.getProject());

    if (taskReport == null || taskReport.getTaskMemberReports() == null) return null;

    return taskReport.getTaskMemberReports().stream()
        .filter(tmr -> tmr.getTask() != null && tmr.getTask().getId().equals(task.getId()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public boolean hasTaskMemberReport(ProjectTask task) {
    if (task == null || task.getProject() == null) return false;

    TaskReport taskReport = taskReportService.getTaskReport(task.getProject());

    if (taskReport == null || taskReport.getTaskMemberReports() == null) return false;

    return taskReport.getTaskMemberReports().stream()
        .anyMatch(tmr -> tmr.getTask() != null && tmr.getTask().getId().equals(task.getId()));
  }
}
