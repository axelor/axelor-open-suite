package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportServiceImpl implements TaskReportService {

  private static final Logger log = LoggerFactory.getLogger(TaskReportServiceImpl.class);
  private static final String CUSTOM_PROJECT_TECHNICIAN_GROUP_CODE = "CUSTOM-PT";

  @Override
  public boolean checkIfAllTasksReported(TaskReport report) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      return false;
    }

    // Get unique reported task IDs, skip template tasks
    Set<Long> reportedTaskIds = getReportedTaskIds(taskReport);
    if (reportedTaskIds.isEmpty()) {
      return false;
    }

    // Get all task Ids for project to use in comparision. skip template taks
    Set<Long> allProjectTaskIds = getAllProjectTaskIds(taskReport);
    if (allProjectTaskIds.isEmpty()) {
      return false;
    }

    // Return true only if all project tasks have been reported
    boolean reportedallTasks = reportedTaskIds.containsAll(allProjectTaskIds);
    log.info("all task reported ? {}", reportedallTasks);
    return reportedallTasks;
  }

  @Override
  public String getReportedTaskCount(TaskReport report) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      return "0/0";
    }

    return getReportedTaskIds(taskReport).size() + "/" + getAllProjectTaskIds(taskReport).size();
  }

  @Override
  public String buildTaskDomainFilter(TaskReport report, Long currentTaskId, User user) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      log.warn("TaskReport or Project is null, returning empty domain");
      return "self.id IN (0)";
    }

    Long projectId = taskReport.getProject().getId();
    String userGroupCode = user.getGroup() != null ? user.getGroup().getCode() : null;

    Set<Long> reportedTaskIds = getReportedTaskIds(taskReport);

    // when editng a task, the task being edited should not be filtered out
    if (currentTaskId != null) {
      reportedTaskIds.remove(currentTaskId);
    }

    // Build domain
    StringBuilder domain = new StringBuilder("self.project.id = ");
    domain.append(projectId);
    domain.append(" AND (self.isTemplate = false OR self.isTemplate IS NULL)");

    // Exclude already reported tasks
    if (!reportedTaskIds.isEmpty()) {
      String excludedIds =
          reportedTaskIds.stream().map(String::valueOf).collect(Collectors.joining(","));
      domain.append(" AND self.id NOT IN (").append(excludedIds).append(")");
    }

    // Filter by assigned user for project technician group
    if (CUSTOM_PROJECT_TECHNICIAN_GROUP_CODE.equals(userGroupCode)) {
      domain.append(" AND self.assignedTo.id = ").append(user.getId());
    }

    log.debug("Built task domain: {}", domain);
    return domain.toString();
  }

  /** Validates report and fetch entity from repository */
  private TaskReport fetchTaskReport(TaskReport report) {
    if (report == null || report.getId() == null) {
      return null;
    }

    TaskReport taskReport = Beans.get(TaskReportRepository.class).find(report.getId());
    if (taskReport == null || taskReport.getProject() == null) {
      return null;
    }

    return taskReport;
  }

  /** Get the id of all reported task excluding template tasks */
  private Set<Long> getReportedTaskIds(TaskReport report) {
    List<TaskMemberReport> taskMemberReports =
        report != null ? report.getTaskMemberReports() : null;

    if (taskMemberReports == null || taskMemberReports.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Long> reportedTaskIds =
        taskMemberReports.stream()
            .map(TaskMemberReport::getTask)
            .filter(Objects::nonNull)
            .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
            .map(ProjectTask::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    log.info("Found {} reported tasks", reportedTaskIds.size());
    return reportedTaskIds;
  }

  /** Get the Id of all project tasks without template tasks */
  private Set<Long> getAllProjectTaskIds(TaskReport report) {
    List<ProjectTask> projectTasks =
        report != null
            ? (report.getProject() != null ? report.getProject().getProjectTaskList() : null)
            : null;

    if (projectTasks == null || projectTasks.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Long> allProjectTaskIds =
        projectTasks.stream()
            .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
            .map(ProjectTask::getId)
            .collect(Collectors.toSet());

    log.info("Found {} project tasks", allProjectTaskIds.size());

    return allProjectTaskIds;
  }

  @Override
  public void createTaskReport(Project project) {
    TaskReport report = new TaskReport();
    report.setProject(project);
    report.setCustomer(project.getClientPartner());
    report.setLocation(project.getCustomerAddress());
    Beans.get(TaskReportRepository.class).save(report);
  }
}
