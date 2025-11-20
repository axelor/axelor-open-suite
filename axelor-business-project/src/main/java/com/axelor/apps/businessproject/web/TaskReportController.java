package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class TaskReportController {

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
