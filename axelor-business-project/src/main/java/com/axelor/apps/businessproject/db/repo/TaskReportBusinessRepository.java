package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.service.taskreport.TaskReportExpenseService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportBusinessRepository extends TaskReportRepository {
  protected final Logger log = LoggerFactory.getLogger(TaskReportBusinessRepository.class);
  protected TaskReportExpenseService taskReportExpenseService;

  @Inject
  public TaskReportBusinessRepository(TaskReportExpenseService taskReportExpenseService) {
    this.taskReportExpenseService = taskReportExpenseService;
  }

  @Override
  public TaskReport save(TaskReport taskReport) {
    if (taskReport.getProject() != null) {
      log.debug("Creating Extra Expenses from task report");
      taskReportExpenseService.createOrUpdateExtraExpenseLinesFromTaskReport(taskReport);
    }

    return super.save(taskReport);
  }
}
