/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
