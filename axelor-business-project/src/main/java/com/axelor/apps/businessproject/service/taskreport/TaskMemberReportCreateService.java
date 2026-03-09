package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.project.db.ProjectTask;
import java.time.LocalDateTime;

public interface TaskMemberReportCreateService {

  class TaskMemberReportCreationResult {
    public final TaskMemberReport tmr;
    public final boolean isNew;

    public TaskMemberReportCreationResult(TaskMemberReport tmr, boolean isNew) {
      this.tmr = tmr;
      this.isNew = isNew;
    }
  }

  TaskMemberReportCreationResult createTaskMemberReport(
      ProjectTask task,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Integer breakTimeMinutes,
      Boolean dirtAllowance)
      throws AxelorException;
}
