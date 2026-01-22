package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.service.taskreport.TaskReportExpenseService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportBusinessRepository extends TaskReportRepository {
  protected final Logger log = LoggerFactory.getLogger(TaskReportBusinessRepository.class);
  protected TaskReportExpenseService taskReportExpenseService;
  protected TaskReportService taskReportService;

  @Inject
  public TaskReportBusinessRepository(
      TaskReportExpenseService taskReportExpenseService, TaskReportService taskReportService) {
    this.taskReportExpenseService = taskReportExpenseService;
    this.taskReportService = taskReportService;
  }

  @Override
  @Transactional
  public TaskReport save(TaskReport taskReport) {

    if (taskReport.getProject() != null) {
      log.debug("Creating Extra Expenses from task report");
      taskReportExpenseService.createOrUpdateExtraExpenseLinesFromTaskReport(taskReport);
    }

    TaskReport savedTaskReport = super.save(taskReport);
    JPA.em().flush();
    List<TaskMemberReport> memberReports = savedTaskReport.getTaskMemberReports();
    if (memberReports != null) {
      for (TaskMemberReport memberReport : memberReports) {
        log.debug(
            "Creating timesheet lines from task member reports {}",
            memberReport.getId().toString());
        taskReportService.createTimesheetLineFromTMR(memberReport);
      }
    }

    return savedTaskReport;
  }
}
