package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import java.time.LocalDate;

public interface TaskReportService {

  /** Check if all tasks for a project habe been reported in this task report. */
  boolean allTasksReported(TaskReport report);

  void createTaskReport(Project project);

  /** Get reported task count per total project task */
  String getReportedTaskCount(TaskReport report);

  /** Build domain filter for tasks in task member report */
  String buildTaskDomainFilter(TaskReport taskReport, Long currentTaskId, User user);

  /**
   * Creates or updates a timesheet line for the given task member report when a task report is
   * saved.
   */
  void createTimesheetLineFromTMR(TaskMemberReport report);

  /** create a timesheet for the employee if no timesheet is found */
  Timesheet findOrCreateMonthlyTimesheet(Employee employee, LocalDate date);

  TaskReport getTaskReport(Project project);

  void updateAllTaskReported(ProjectTask task);
}
