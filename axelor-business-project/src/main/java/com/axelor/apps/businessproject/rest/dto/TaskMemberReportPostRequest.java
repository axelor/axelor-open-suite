package com.axelor.apps.businessproject.rest.dto;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TaskMemberReportPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long taskId;

  @NotNull
  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime startTime;

  @NotNull
  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime endTime;

  @Min(0)
  private Integer breakTimeMinutes;

  private Boolean dirtAllowance;

  public Long getTaskId() {
    return taskId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public Integer getBreakTimeMinutes() {
    return breakTimeMinutes;
  }

  public Boolean getDirtAllowance() {
    return dirtAllowance;
  }

  public ProjectTask fetchTask() {
    if (taskId == null || taskId == 0L) return null;
    return ObjectFinder.find(ProjectTask.class, taskId, ObjectFinder.NO_VERSION);
  }
}
