package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.translation.ITranslation;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import javax.ws.rs.core.Response;

public class ExpenseLineResponseComputeServiceProjectImpl
    extends ExpenseLineResponseComputeServiceImpl {
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ExpenseLineResponseComputeServiceProjectImpl(
      AppBusinessProjectService appBusinessProjectService) {
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public Response computeCreateResponse(
      ExpenseLine expenseLine,
      Project project,
      Boolean toInvoice,
      ExpenseLineResponse expenseLineResponse) {
    if (appBusinessProjectService.isApp("business-project")
        && project != null
        && project.getIsInvoicingExpenses()
        && toInvoice == null) {
      return ResponseConstructor.build(
          Response.Status.CREATED,
          I18n.get(ITranslation.EXPENSE_LINE_CREATION_WITH_PROJECT),
          new ExpenseLineResponse(expenseLine));
    }
    return super.computeCreateResponse(expenseLine, project, toInvoice, expenseLineResponse);
  }
}
