package com.axelor.apps.businessproject.service.statuschange;

import static com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage.PROJECT_TASK_BUSINESS_PROJECT_TASK_STATUS_NOT_FOUND;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskStatusBusinessProjectRepository;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportService;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskStatusChangeServiceImpl implements TaskStatusChangeService {

  protected final Logger log = LoggerFactory.getLogger(TaskStatusChangeServiceImpl.class);

  // Task Statuses
  public static final String TASK_STATUS_NEW = "New";
  public static final String TASK_STATUS_IN_PROGRESS = "In Progress";
  public static final String TASK_STATUS_FEEDBACK = "Feedback";
  public static final String TASK_STATUS_DONE = "Done";

  protected TaskStatusBusinessProjectRepository taskStatusRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected TaskMemberReportService taskMemberReportRepo =
      Beans.get(TaskMemberReportServiceImpl.class);

  @Inject
  public TaskStatusChangeServiceImpl(
      TaskStatusBusinessProjectRepository taskStatusRepo, ProjectTaskRepository projectTaskRepo) {
    this.taskStatusRepo = taskStatusRepo;
    this.projectTaskRepo = projectTaskRepo;
  }

  @Override
  public void handleTaskReportSaved(TaskReport taskReport) throws AxelorAlertException {
    List<TaskMemberReport> memberReports = getTaskMemberReports(taskReport);
    TaskStatus feedbackStatus = getTaskStatus(TASK_STATUS_FEEDBACK);

    for (TaskMemberReport taskMemberReport : memberReports) {
      if (taskMemberReport.getTask() == null) {
        continue;
      }

      ProjectTask task = taskMemberReport.getTask();
      TaskStatus currentStatus = task.getStatus();

      // Only change status if it is not already in Feedback or Done
      if (currentStatus != null && !currentStatus.getIsCompleted()) {

        task.setStatus(feedbackStatus);
        projectTaskRepo.save(task);
        log.info("Task {} status changed to Feedback", task.getId());
      }
    }
    handleUnreportedTasks(taskReport);
  }

  @Override
  public void handleTaskReportDeleted(TaskReport taskReport) throws AxelorAlertException {
    List<TaskMemberReport> memberReports = getTaskMemberReports(taskReport);
    TaskStatus inProgressStatus = getTaskStatus(TASK_STATUS_IN_PROGRESS);

    for (TaskMemberReport taskMemberReport : memberReports) {
      ProjectTask task = taskMemberReport.getTask();

      revertUnreportedTaskStatus(task, inProgressStatus);
    }
  }

  private void handleUnreportedTasks(TaskReport taskReport) throws AxelorAlertException {
    if (taskReport == null
        || taskReport.getProject() == null
        || taskReport.getProject().getProjectTaskList() == null) {
      return;
    }
    TaskStatus inProgressStatus = getTaskStatus(TASK_STATUS_IN_PROGRESS);

    List<ProjectTask> unReportedTasks =
        taskReport.getProject().getProjectTaskList().stream()
            .filter(
                task -> !task.getIsTemplate() && !taskMemberReportRepo.hasTaskMemberReport(task))
            .collect(Collectors.toList());

    for (ProjectTask task : unReportedTasks) {
      revertUnreportedTaskStatus(task, inProgressStatus);
    }
  }

  private void revertUnreportedTaskStatus(ProjectTask task, TaskStatus inProgressStatus) {
    task.setStatus(inProgressStatus);
    projectTaskRepo.save(task);
    log.info("Task {} status reverted to In Progress", task.getId());
  }

  @Override
  public void changeTaskStatusToDone(ProjectTask task) throws AxelorAlertException {
    TaskStatus doneStatus = getTaskStatus(TASK_STATUS_DONE);

    task.setStatus(doneStatus);
    projectTaskRepo.save(task);
    log.info("Task {} status changed to Done", task.getId());
  }

  @Override
  public void revertTaskStatusOnTimesheetLineCancel(ProjectTask task) throws AxelorAlertException {
    if (task == null) return;

    // If the task has no task member report it's state should be reverted to in progress
    if (!Beans.get(TaskMemberReportServiceImpl.class).hasTaskMemberReport(task)) {
      TaskStatus inProgressStatus = getTaskStatus(TASK_STATUS_IN_PROGRESS);
      task.setStatus(inProgressStatus);
      projectTaskRepo.save(task);
      log.debug("Task {} staus changed to In Progress", task.getId());
    } else {
      TaskStatus feedbackStatus = getTaskStatus(TASK_STATUS_FEEDBACK);
      task.setStatus(feedbackStatus);
      projectTaskRepo.save(task);
      log.debug("Task {} status changed to Feedback", task.getId());
    }
  }

  private List<TaskMemberReport> getTaskMemberReports(TaskReport taskReport) {
    if (taskReport == null || taskReport.getTaskMemberReports() == null) {
      return Collections.emptyList();
    }

    return taskReport.getTaskMemberReports();
  }

  private TaskStatus getTaskStatus(String statusName) throws AxelorAlertException {
    TaskStatus taskStatus = taskStatusRepo.findByNameIgnoreCase(statusName);
    if (taskStatus == null) {
      throw new AxelorAlertException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PROJECT_TASK_BUSINESS_PROJECT_TASK_STATUS_NOT_FOUND),
          statusName);
    }
    return taskStatus;
  }
}
