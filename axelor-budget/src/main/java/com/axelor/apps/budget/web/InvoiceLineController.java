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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class InvoiceLineController {

  public void validateBudgetLinesAmount(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      Beans.get(BudgetInvoiceLineService.class).checkAmountForInvoiceLine(invoiceLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void checkBudget(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();

      if (parentContext != null
          && parentContext.getContextClass().toString().equals(Invoice.class.toString())) {

        Invoice invoice = parentContext.asType(Invoice.class);
        if (invoice != null && invoice.getCompany() != null) {
          response.setAttr(
              "budgetPanel",
              "readonly",
              !(Beans.get(BudgetToolsService.class)
                      .checkBudgetKeyAndRole(invoice.getCompany(), AuthUtils.getUser()))
                  || invoice.getStatusSelect() > InvoiceRepository.STATUS_VALIDATED);
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void computeBudgetDistributionSumAmount(ActionRequest request, ActionResponse response) {
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
    Invoice invoice = request.getContext().getParent().asType(Invoice.class);

    Beans.get(BudgetInvoiceLineService.class)
        .computeBudgetDistributionSumAmount(invoiceLine, invoice);

    response.setValue("budgetDistributionSumAmount", invoiceLine.getBudgetDistributionSumAmount());
    response.setValue("budgetDistributionList", invoiceLine.getBudgetDistributionList());
  }
}
