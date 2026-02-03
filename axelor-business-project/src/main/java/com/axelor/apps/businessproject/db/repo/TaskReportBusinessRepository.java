package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.businessproject.service.statuschange.TaskStatusChangeService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportExpenseService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.project.db.Project;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportBusinessRepository extends TaskReportRepository {
  protected final Logger log = LoggerFactory.getLogger(TaskReportBusinessRepository.class);
  protected TaskReportExpenseService taskReportExpenseService;
  protected TaskReportService taskReportService;
  protected TaskStatusChangeService taskStatusChangeService;
  protected ProjectStatusChangeService projectStatusChangeService;

  @Inject
  public TaskReportBusinessRepository(
      TaskReportExpenseService taskReportExpenseService,
      TaskReportService taskReportService,
      TaskStatusChangeService taskStatusChangeService,
      ProjectStatusChangeService projectStatusChangeService) {
    this.taskReportExpenseService = taskReportExpenseService;
    this.taskReportService = taskReportService;
    this.taskStatusChangeService = taskStatusChangeService;
    this.projectStatusChangeService = projectStatusChangeService;
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

    try {
      taskStatusChangeService.handleTaskReportSaved(taskReport);
      projectStatusChangeService.updateProjectStatus(taskReport.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }

    // determine if the project's status should move to "To Validate" status
    return savedTaskReport;
  }

  @Override
  public void remove(TaskReport taskReport) {
    try {
      taskStatusChangeService.handleTaskReportDeleted(taskReport);
      Project project = taskReport.getProject();
      super.remove(taskReport);
      // When a task gets un reported determine which status the project should get
      projectStatusChangeService.updateProjectStatus(project);
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
