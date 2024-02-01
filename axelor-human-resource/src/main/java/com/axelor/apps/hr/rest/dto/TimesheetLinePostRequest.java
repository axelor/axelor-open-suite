package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TimesheetLinePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long timesheetId;

  @Min(0)
  private Long projectId;

  @Min(0)
  private Long projectTaskId;

  @NotNull private LocalDate date;

  @NotNull
  @Min(0)
  private BigDecimal duration;

  private String comments;

  private boolean toInvoice;

  public Long getTimesheetId() {
    return timesheetId;
  }

  public void setTimesheetId(Long timesheetId) {
    this.timesheetId = timesheetId;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getProjectTaskId() {
    return projectTaskId;
  }

  public void setProjectTaskId(Long projectTaskId) {
    this.projectTaskId = projectTaskId;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public boolean isToInvoice() {
    return toInvoice;
  }

  public void setToInvoice(boolean toInvoice) {
    this.toInvoice = toInvoice;
  }

  public Timesheet fetchTimesheet() {
    return ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);
  }

  public Project fetchProject() {
    if (projectId == null || projectId == 0L) {
      return null;
    }
    return ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);
  }

  public ProjectTask fetchProjectTask() {
    if (projectTaskId == null || projectTaskId == 0L) {
      return null;
    }
    return ObjectFinder.find(ProjectTask.class, projectTaskId, ObjectFinder.NO_VERSION);
  }
}
