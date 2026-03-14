package com.axelor.apps.businessproject.rest.dto;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class TaskMemberReportResponse extends ResponseStructure {

  private final Long id;
  private final BigDecimal workHours;
  private final Long taskReportId;
  private final Long projectId;
  private final boolean taskReportToolUsage;
  private final boolean taskReportTravelExpense;
  private final boolean taskReportDirtAllowance;
  private final boolean reportedAllTask;
  private final boolean created; // indicates if the task member report was created or updated

  public TaskMemberReportResponse(TaskMemberReport tmr, boolean created) {
    super(tmr.getVersion());
    this.id = tmr.getId();
    this.workHours = tmr.getWorkHours();

    TaskReport taskReport = tmr.getTaskReport();

    this.taskReportId = taskReport.getId();
    this.projectId = taskReport.getProject().getId();
    this.taskReportToolUsage = taskReport.getToolsUsage();
    this.taskReportTravelExpense = taskReport.getTravelExpenses();
    this.taskReportDirtAllowance = taskReport.getDirtAllowance();
    this.reportedAllTask = taskReport.getReportedAllTasks();
    this.created = created;
  }

  public Long getId() {
    return id;
  }

  public BigDecimal getWorkHours() {
    return workHours;
  }

  public Long getTaskReportId() {
    return taskReportId;
  }

  public boolean isCreated() {
    return created;
  }

  public Long getProjectId() {
    return projectId;
  }

  public boolean isTaskReportToolUsage() {
    return taskReportToolUsage;
  }

  public boolean isTaskReportTravelExpense() {
    return taskReportTravelExpense;
  }

  public boolean isTaskReportDirtAllowance() {
    return taskReportDirtAllowance;
  }

  public boolean isReportedAllTask() {
    return reportedAllTask;
  }
}
