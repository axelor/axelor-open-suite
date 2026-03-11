package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.service.extraexpense.ExtraExpenseLineService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.time.LocalDate;

public class ExtraExpenseLineController {

  @Inject protected ExtraExpenseLineService extraExpenseLineService;

  public void setExtraExpenseLineTypeSelect(ActionRequest request, ActionResponse response) {
    ExtraExpenseLine extraExpenseLine = request.getContext().asType(ExtraExpenseLine.class);

    Integer extraExpenseLineTypeSelect =
        extraExpenseLineService.getTypeSelectFromCode(
            extraExpenseLine.getExpenseProduct().getCode());

    response.setValue("extraExpenseLineTypeSelect", extraExpenseLineTypeSelect);
  }

  public void setExtraExpenseLineDefaults(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().getParent().asType(Project.class);

    project = Beans.get(ProjectRepository.class).find(project.getId());
    TaskReport taskReport = Beans.get(TaskReportService.class).getTaskReport(project);

    response.setValue("project", project);
    response.setValue("taskReport", taskReport);
    response.setValue("expenseDate", LocalDate.now());
    response.setValue("toInvoice", true);
  }
}
