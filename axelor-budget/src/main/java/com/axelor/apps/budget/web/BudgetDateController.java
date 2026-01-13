/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.date.BudgetDateService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDateController {

  @ErrorException
  public void manageBudgetDateLabel(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LocalDate fromDate = null;
    LocalDate toDate = null;
    Budget budget = null;
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();

    if (InvoiceLine.class.equals(request.getContext().getContextClass())) {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      fromDate = invoiceLine.getBudgetFromDate();
      toDate = invoiceLine.getBudgetToDate();
      budget = invoiceLine.getBudget();
      budgetDistributionList = invoiceLine.getBudgetDistributionList();
    } else if (MoveLine.class.equals(request.getContext().getContextClass())) {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      fromDate = moveLine.getBudgetFromDate();
      toDate = moveLine.getBudgetToDate();
      budget = moveLine.getBudget();
      budgetDistributionList = moveLine.getBudgetDistributionList();
    } else if (PurchaseOrderLine.class.equals(request.getContext().getContextClass())) {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      fromDate = purchaseOrderLine.getBudgetFromDate();
      toDate = purchaseOrderLine.getBudgetToDate();
      budget = purchaseOrderLine.getBudget();
      budgetDistributionList = purchaseOrderLine.getBudgetDistributionList();
    } else if (SaleOrderLine.class.equals(request.getContext().getContextClass())) {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      fromDate = saleOrderLine.getBudgetFromDate();
      toDate = saleOrderLine.getBudgetToDate();
      budget = saleOrderLine.getBudget();
      budgetDistributionList = saleOrderLine.getBudgetDistributionList();
    }

    String labelError =
        Beans.get(BudgetDateService.class)
            .getBudgetDateError(fromDate, toDate, budget, budgetDistributionList);
    if (StringUtils.notEmpty(labelError)) {
      response.setAttr("budgetDatesLabel", "title", labelError);
      response.setAttr("budgetDatesLabel", "hidden", false);
    } else {
      response.setAttr("budgetDatesLabel", "hidden", true);
    }
  }

  @ErrorException
  public void checkBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String labelError = "";
    BudgetDateService budgetDateService = Beans.get(BudgetDateService.class);
    Context currentContext = request.getContext();
    Class<?> currentClass = currentContext.getContextClass();

    if (Invoice.class.equals(currentClass)) {
      labelError = budgetDateService.checkBudgetDates(currentContext.asType(Invoice.class));
    } else if (Move.class.equals(currentClass)) {
      labelError = budgetDateService.checkBudgetDates(currentContext.asType(Move.class));
    } else if (PurchaseOrder.class.equals(currentClass)) {
      labelError = budgetDateService.checkBudgetDates(currentContext.asType(PurchaseOrder.class));
    } else if (SaleOrder.class.equals(currentClass)) {
      labelError = budgetDateService.checkBudgetDates(currentContext.asType(SaleOrder.class));
    } else {
      return;
    }

    if (!StringUtils.isEmpty(labelError)) {
      response.setError(labelError);
    }
  }
}
