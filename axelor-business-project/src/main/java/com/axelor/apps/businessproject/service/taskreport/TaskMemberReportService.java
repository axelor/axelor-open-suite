package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TaskMemberReportService {
  TaskMemberReport getTaskMemberReport(ProjectTask task);

  boolean hasTaskMemberReport(ProjectTask task);

  BigDecimal computeWorkHours(
      LocalDateTime startTime, LocalDateTime endTime, BigDecimal breakTimeInHours);

  TimesheetLine getTimesheetLine(TaskMemberReport report);
}
