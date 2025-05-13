package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetLineRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

public class BudgetDateServiceImpl implements BudgetDateService {

  protected BudgetLineRepository budgetLineRepository;

  @Inject
  public BudgetDateServiceImpl(BudgetLineRepository budgetLineRepository) {
    this.budgetLineRepository = budgetLineRepository;
  }

  @Override
  public String checkBudgetDates(Invoice invoice) {
    String error = "";
    if (invoice == null || ObjectUtils.isEmpty(invoice.getInvoiceLineList())) {
      return error;
    }

    StringJoiner sj = new StringJoiner("<BR/>");

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      error =
          getBudgetDateError(
              invoiceLine.getBudgetFromDate(),
              invoiceLine.getBudgetToDate(),
              invoiceLine.getBudget(),
              invoiceLine.getBudgetDistributionList());
      if (StringUtils.notEmpty(error)) {
        sj.add(
            String.format(
                "%s %s :",
                I18n.get(BudgetExceptionMessage.ON_PRODUCT_LINE), invoiceLine.getProductName()));
        sj.add(error);
      }
    }

    return sj.toString();
  }

  @Override
  public String getBudgetDateError(
      LocalDate fromDate,
      LocalDate toDate,
      Budget budget,
      List<BudgetDistribution> budgetDistributionList) {
    if (fromDate == null && toDate == null) {
      return "";
    }

    if (fromDate == null || toDate == null) {
      return I18n.get(BudgetExceptionMessage.BUDGET_MISSING_DATES);
    }
    if (fromDate.isAfter(toDate)) {
      return I18n.get(BudgetExceptionMessage.BUDGET_WRONG_DATES);
    }

    if (budget == null && ObjectUtils.isEmpty(budgetDistributionList)) {
      return "";
    }

    StringJoiner sj = new StringJoiner("<BR/>");
    if (budget != null) {
      fillErrorWithBudget(sj, fromDate, toDate, budget);
    } else {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        fillErrorWithBudget(sj, fromDate, toDate, budgetDistribution.getBudget());
      }
    }

    if (sj.length() > 0) {
      return I18n.get(sj.toString());
    }

    return "";
  }

  protected void fillErrorWithBudget(
      StringJoiner sj, LocalDate fromDate, LocalDate toDate, Budget budget) {
    if (budget == null) {
      return;
    }

    if (ObjectUtils.isEmpty(budget.getBudgetLineList())) {
      sj.add(
          String.format(
              I18n.get(BudgetExceptionMessage.BUDGET_LINE_MISSING_ON_DATES),
              fromDate.toString(),
              toDate.toString(),
              budget.getCode()));
      return;
    }

    LocalDate date = fromDate;

    while (!date.isAfter(toDate)) {
      BudgetLine budgetLine = budgetLineRepository.findCurrentByDate(budget, date);
      if (budgetLine == null) {
        sj.add(
            String.format(
                I18n.get(BudgetExceptionMessage.BUDGET_LINE_MISSING_ON_DATES),
                date.toString(),
                toDate.toString(),
                budget.getCode()));
        return;
      }

      date = budgetLine.getToDate().plusDays(1);
    }
  }
}
