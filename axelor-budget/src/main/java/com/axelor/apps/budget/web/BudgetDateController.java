package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.date.BudgetDateService;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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
    }

    String labelError =
        Beans.get(BudgetDateService.class)
            .getBudgetDateError(fromDate, toDate, budget, budgetDistributionList, null);
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
    if (Invoice.class.equals(request.getContext().getContextClass())) {
      labelError =
          Beans.get(BudgetDateService.class)
              .checkBudgetDates(request.getContext().asType(Invoice.class));
    } else {
      return;
    }

    if (!StringUtils.isEmpty(labelError)) {
      response.setError(labelError);
    }
  }
}
