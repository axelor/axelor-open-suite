package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportServiceImpl implements TaskReportService {

  private static final Logger log = LoggerFactory.getLogger(TaskReportServiceImpl.class);

  @Override
  public boolean checkIfAllTasksReported(TaskReport report) {

    if (report == null || report.getId() == null) {
      return false;
    }

    TaskReport taskReport = Beans.get(TaskReportRepository.class).find(report.getId());

    if (taskReport == null || taskReport.getProject() == null) {
      return false;
    }

    // Get all task member reports for this task report
    List<TaskMemberReport> taskMemberReports = taskReport.getTaskMemberReports();
    log.info("Found {} task member reports", taskMemberReports.size());

    // If no task member reports exist, no tasks reported
    if (taskMemberReports == null || taskMemberReports.isEmpty()) {
      return false;
    }

    // Get unique reported task IDs, skip template tasks
    Set<Long> reportedTaskIds =
        taskMemberReports.stream()
            .map(TaskMemberReport::getTask)
            .filter(Objects::nonNull)
                .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
            .map(ProjectTask::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    log.info("found {} reported tasks", reportedTaskIds.size());

    // If found no valid tasks in reports, not all tasks reported
    if (reportedTaskIds == null || reportedTaskIds.isEmpty()) {
      return false;
    }

    // Get all project task IDs
    List<ProjectTask> projectTasks = taskReport.getProject().getProjectTaskList();
    log.info(
        "Found {} tasks for this project {}",
        projectTasks.size(),
        taskReport.getProject().getFullName());

    if (projectTasks == null || projectTasks.isEmpty()) {
      return false;
    }

    // Get all task Ids for project to use in comparision. skip template taks
    Set<Long> allProjectTaskIds =
        projectTasks.stream()
                .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
                .map(ProjectTask::getId).collect(Collectors.toSet());

    if (allProjectTaskIds == null || allProjectTaskIds.isEmpty()) {
      return false;
    }

    // Return true only if all project tasks have been reported
    boolean reportedallTasks = reportedTaskIds.containsAll(allProjectTaskIds);
    log.info("all task reported ? {}", reportedallTasks);
    return reportedallTasks;
  }
}
