package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.ExtraExpenseLineRepository;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.businessproject.service.taskreport.TaskReportExpenseService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class TaskReportController {

  @Inject private ExtraExpenseLineRepository extraExpenseLineRepo;

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

  /** Called on save of TaskReport to create/update extra expense */
  public void createOrUpdateExtraExpenses(ActionRequest request, ActionResponse response) {
    try {
      TaskReport taskReport = request.getContext().asType(TaskReport.class);

      TaskReportExpenseService taskReportExpenseService = Beans.get(TaskReportExpenseService.class);

      List<ExtraExpenseLine> lines =
          taskReportExpenseService.createOrUpdateExtraExpenseLinesFromTaskReport(taskReport);

      if (lines != null && !lines.isEmpty()) {
        response.setValue("extraExpenseLineList", lines);
      }

    } catch (Exception e) {
      response.setError(e.getMessage());
    }
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
}
