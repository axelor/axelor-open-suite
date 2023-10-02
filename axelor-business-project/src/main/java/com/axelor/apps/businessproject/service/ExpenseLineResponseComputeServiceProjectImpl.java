package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.translation.ITranslation;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLinePostRequest;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import java.math.BigDecimal;
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
      ExpenseLinePostRequest requestBody,
      ExpenseLineResponse expenseLineResponse) {
    Project project = requestBody.fetchProject();
    Boolean toInvoice = requestBody.getToInvoice();
    BigDecimal totalTax = requestBody.getTotalTax();
    Product expenseProduct = requestBody.fetchExpenseProduct();
    if (appBusinessProjectService.isApp("business-project")
        && project != null
        && project.getIsInvoicingExpenses()
        && toInvoice == null) {
      StringBuilder response =
          new StringBuilder(I18n.get(ITranslation.EXPENSE_LINE_CREATION_WITH_PROJECT));
      if (expenseProduct != null
          && expenseProduct.getBlockExpenseTax()
          && totalTax != null
          && totalTax.compareTo(BigDecimal.ZERO) != 0) {
        response.append(" ");
        response.append(I18n.get(com.axelor.apps.hr.translation.ITranslation.SET_TOTAL_TAX_ZERO));
      }

      return ResponseConstructor.build(
          Response.Status.CREATED, response.toString(), new ExpenseLineResponse(expenseLine));
    }
    return super.computeCreateResponse(expenseLine, requestBody, expenseLineResponse);
  }
}
