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
}
