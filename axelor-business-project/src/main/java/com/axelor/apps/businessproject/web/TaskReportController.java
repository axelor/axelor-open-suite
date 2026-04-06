package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.utils.TimeOverlapValidator;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.ExtraExpenseLineRepository;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskReportController {

  private static final Logger log = LoggerFactory.getLogger(TaskReportController.class);
  @Inject private ExtraExpenseLineRepository extraExpenseLineRepo;
  private final TaskReportService taskReportService = Beans.get(TaskReportService.class);

  @Inject protected TaskMemberReportService taskMemberReportService;

  @Inject protected TimeOverlapValidator timeOverlapValidator;

  public void previewTaskReport(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    TaskReport taskReport = context.asType(TaskReport.class);

    if (taskReport == null || taskReport.getId() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get("No Task Report found to preview"));
    }

    taskReport = Beans.get(TaskReportRepository.class).find(taskReport.getId());
    response.setView(
        ActionView.define("MGM TASK REPORT PREVIEW")
            .model(TaskReport.class.getName())
            .add("form", "task-report-preview-form")
            .param("popup", "true")
            .context("_showRecord", taskReport.getId())
            .context("_previewReadonly", true)
            .context("_canSign", true)
            .map());
  }

  /** Determine the visibilty of extra expenses panel. */
  public void checkHasInvoicableExpenses(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Project project = (Project) context.get("project");

    if (project == null || project.getId() == null) {
      response.setAttr("extraExpenseLineSetDashlet", "hidden", true);
      return;
    }

    long count =
        extraExpenseLineRepo
            .all()
            .filter(
                "self.project.id = ?1 AND self.toInvoice = true AND self.invoiced = false",
                project.getId())
            .count();

    // Hide extra expense panel if count is 0, show if count > 0
    response.setAttr("extraExpenseLineSetDashlet", "hidden", count == 0);
  }

  public void validateTaskReport(ActionRequest request, ActionResponse response) {
    TaskReport taskReport = request.getContext().asType(TaskReport.class);

    if (taskReport.getProject() == null) {
      response.setError("Project must be selected before saving the Task Report.");
      return;
    }

    TaskReport existingReport =
        Beans.get(TaskReportRepository.class)
            .all()
            .filter("self.project.id = ?1", taskReport.getProject().getId())
            .fetchOne();

    if (existingReport != null
        && (taskReport.getId() == null || !existingReport.getId().equals(taskReport.getId()))) {
      response.setError("A Task Report already exists for this project.");
      return;
    }
  }

  /** Updates the reportedAllTask field to the current state */
  public void setReportedAllProjectTaskFlag(ActionRequest request, ActionResponse response) {
    TaskReport taskReport = request.getContext().asType(TaskReport.class);

    taskReport = Beans.get(TaskReportRepository.class).find(taskReport.getId());
    boolean allTasksReported = taskReportService.allTasksReported(taskReport);

    response.setValue("reportedAllTasks", allTasksReported);
  }

  /** Update reported task count per total project task count */
  public void updateReportedTaskCount(ActionRequest request, ActionResponse response) {
    TaskReport taskReport = request.getContext().asType(TaskReport.class);

    String reportedTaskCount = taskReportService.getReportedTaskCount(taskReport);

    response.setValue("reportedTaskCount", reportedTaskCount);
  }

  /** Filter tasks for task member report dropdown */
  public void filterTasksForMemberReport(ActionRequest request, ActionResponse response) {
    User currentUser = AuthUtils.getUser();

    TaskReport taskReport = request.getContext().getParent().asType(TaskReport.class);

    TaskMemberReport currentReport = request.getContext().asType(TaskMemberReport.class);
    Long currentTaskId = currentReport.getTask() != null ? currentReport.getTask().getId() : null;

    String domain = taskReportService.buildTaskDomainFilter(taskReport, currentTaskId, currentUser);

    response.setAttr("task", "domain", domain);
  }

  public void filterTaskMemberReports(ActionRequest request, ActionResponse response) {
    TaskReport report = request.getContext().asType(TaskReport.class);

    User user = AuthUtils.getUser();

    List<TaskMemberReport> taskMemberReports = report.getTaskMemberReports();

    if (user.getGroup() != null && !"CUSTOM-PT".equals(user.getGroup().getCode())) {
      response.setValue("taskMemberReports", taskMemberReports);
      return;
    }
    List<TaskMemberReport> filtered =
        taskMemberReports.stream()
            .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(user.getId()))
            .collect(Collectors.toList());

    response.setValue("taskMemberReports", filtered);
  }

  public void checkTimeOverlap(ActionRequest request, ActionResponse response) {
    TaskMemberReport report = request.getContext().asType(TaskMemberReport.class);

    if (report.getEmployee() == null
        || report.getStartTime() == null
        || report.getEndTime() == null) {
      return;
    }

    TaskMemberReport conflict =
        timeOverlapValidator.findConflictingRecord(
            TaskMemberReport.class,
            report.getStartTime(),
            report.getEndTime(),
            report.getEmployee().getId(),
            report.getId(),
            "startTime",
            "endTime",
            "employee");

    if (conflict != null) {
      response.setError(
          String.format(
              I18n.get(
                  "This time range overlaps with an existing task report for the employee: %s for task: %s (time: %s - %s) on project: '%s'"),
              conflict.getEmployee().getName(),
              conflict.getTask() != null ? conflict.getTask().getName() : "N/A",
              conflict.getStartTime().toLocalTime(),
              conflict.getEndTime().toLocalTime(),
              (conflict.getTask() != null && conflict.getTask().getProject() != null)
                  ? conflict.getTask().getProject().getFullName()
                  : "N/A"));

      response.setValue("startTime", null);
      response.setValue("endTime", null);
    }
  }

  public void checkDeletedTMRTimesheetLineState(ActionRequest request, ActionResponse response) {
    TaskReport taskReport = request.getContext().asType(TaskReport.class);

    if (taskReport.getId() == null) {
      response.setValue("$tslDeleteWarningState", "none");
      return;
    }

    TaskReport existingTaskReport = Beans.get(TaskReportRepository.class).find(taskReport.getId());
    if (existingTaskReport == null) {
      response.setValue("$tslDeleteWarningState", "none");
      return;
    }

    List<TaskMemberReport> contextTMRs =
        Optional.ofNullable(taskReport.getTaskMemberReports()).orElse(Collections.emptyList());
    List<TaskMemberReport> existingTMRs =
        Optional.ofNullable(existingTaskReport.getTaskMemberReports())
            .orElse(Collections.emptyList());

    Set<Long> contextIds =
        contextTMRs.stream()
            .map(TaskMemberReport::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<TaskMemberReport> deletedTaskMemberReports =
        existingTMRs.stream()
            .filter(tmr -> !contextIds.contains(tmr.getId()))
            .collect(Collectors.toList());

    if (deletedTaskMemberReports.isEmpty()) {
      response.setValue("$tslDeleteWarningState", "none");
      return;
    }

    String worstState = "none";
    for (TaskMemberReport tmr : deletedTaskMemberReports) {
      TimesheetLine tsl = taskMemberReportService.getTimesheetLine(tmr);
      if (tsl == null) continue;

      if (Boolean.TRUE.equals(tsl.getInvoiced())) {
        response.setValue("$tslDeleteWarningState", "invoiced");
        log.debug("Deleted TMR id={} has invoiced TSL id={}", tmr.getId(), tsl.getId());
        return;
      }

      if (Boolean.TRUE.equals(tsl.getIsValidated())) {
        worstState = "validated";
        log.debug("Deleted TMR id={} has validated TSL id={}", tmr.getId(), tsl.getId());
        continue;
      }

      if (!"validated".equals(worstState)
          && Beans.get(TimesheetLineBusinessService.class).isReferencedInInvoicingProject(tsl)) {
        worstState = "referenced_in_invoicing_project";
        log.debug(
            "Deleted TMR id={} has TSL id={} referenced in an invoicing project",
            tmr.getId(),
            tsl.getId());
        continue;
      }

      if ("none".equals(worstState)) {
        worstState = "deletable";
        log.debug("Deleted TMR id={} has deletable TSL id={}", tmr.getId(), tsl.getId());
      }
    }

    response.setValue("$tslDeleteWarningState", worstState);
    log.debug("Resolved worst TSL delete state: {}", worstState);
  }

  /** Deletes the timesheet line associated to the deleted task member report */
  public void deleteRemovedTMRTimesheetLines(ActionRequest request, ActionResponse response) {
    TaskReport taskReport = request.getContext().asType(TaskReport.class);

    if (taskReport.getId() == null) return;

    TaskReport existingTaskReport = Beans.get(TaskReportRepository.class).find(taskReport.getId());
    if (existingTaskReport == null) return;

    List<TaskMemberReport> contextTMRs =
        Optional.ofNullable(taskReport.getTaskMemberReports()).orElse(Collections.emptyList());
    List<TaskMemberReport> existingTMRs =
        Optional.ofNullable(existingTaskReport.getTaskMemberReports())
            .orElse(Collections.emptyList());

    Set<Long> contextIds =
        contextTMRs.stream()
            .map(TaskMemberReport::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<TaskMemberReport> deletedTaskMemberReports =
        existingTMRs.stream()
            .filter(tmr -> !contextIds.contains(tmr.getId()))
            .collect(Collectors.toList());

    if (deletedTaskMemberReports.isEmpty()) return;

    for (TaskMemberReport tmr : deletedTaskMemberReports) {
      TimesheetLine tsl = taskMemberReportService.getTimesheetLine(tmr);
      if (tsl == null) {
        log.debug("No TSL found for deleted TMR id={}, skipping", tmr.getId());
        continue;
      }
      log.debug("Deleting TSL id={} for deleted TMR id={}", tsl.getId(), tmr.getId());
      Beans.get(TimesheetLineRemoveService.class).removeTimesheetLine(tsl);
    }
  }

  public void cancelDelete(ActionRequest request, ActionResponse response) {
    response.setReload(true);
  }
}
