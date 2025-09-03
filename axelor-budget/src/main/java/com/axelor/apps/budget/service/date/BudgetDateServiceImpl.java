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
package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetLineRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
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
    if (invoice == null || ObjectUtils.isEmpty(invoice.getInvoiceLineList())) {
      return "";
    }

    StringJoiner sj = new StringJoiner("<BR/>");

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      getBudgetDateError(
          invoiceLine.getBudgetFromDate(),
          invoiceLine.getBudgetToDate(),
          invoiceLine.getBudget(),
          invoiceLine.getBudgetDistributionList(),
          invoiceLine.getProductName(),
          sj);
    }

    return sj.toString();
  }

  @Override
  public String checkBudgetDates(Move move) {
    if (move == null || ObjectUtils.isEmpty(move.getMoveLineList())) {
      return "";
    }

    StringJoiner sj = new StringJoiner("<BR/>");

    for (MoveLine moveLine : move.getMoveLineList()) {
      getBudgetDateError(
          moveLine.getBudgetFromDate(),
          moveLine.getBudgetToDate(),
          moveLine.getBudget(),
          moveLine.getBudgetDistributionList(),
          moveLine.getAccountName(),
          sj);
    }

    return sj.toString();
  }

  @Override
  public String checkBudgetDates(PurchaseOrder purchaseOrder) {
    if (purchaseOrder == null || ObjectUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      return "";
    }

    StringJoiner sj = new StringJoiner("<BR/>");

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      getBudgetDateError(
          purchaseOrderLine.getBudgetFromDate(),
          purchaseOrderLine.getBudgetToDate(),
          purchaseOrderLine.getBudget(),
          purchaseOrderLine.getBudgetDistributionList(),
          purchaseOrderLine.getProductName(),
          sj);
    }

    return sj.toString();
  }

  @Override
  public String checkBudgetDates(SaleOrder saleOrder) {
    if (saleOrder == null || ObjectUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return "";
    }

    StringJoiner sj = new StringJoiner("<BR/>");

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      getBudgetDateError(
          saleOrderLine.getBudgetFromDate(),
          saleOrderLine.getBudgetToDate(),
          saleOrderLine.getBudget(),
          saleOrderLine.getBudgetDistributionList(),
          saleOrderLine.getProductName(),
          sj);
    }

    return sj.toString();
  }

  protected void getBudgetDateError(
      LocalDate fromDate,
      LocalDate toDate,
      Budget budget,
      List<BudgetDistribution> budgetDistributionList,
      String errorArgument,
      StringJoiner sj) {
    String error = getBudgetDateError(fromDate, toDate, budget, budgetDistributionList);
    if (StringUtils.notEmpty(error)) {
      sj.add(String.format("%s %s :", I18n.get(BudgetExceptionMessage.ON_LINE), errorArgument));
      sj.add(error);
    }
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

    String coherenceError = checkDateCoherence(fromDate, toDate);
    if (StringUtils.notEmpty(coherenceError)) {
      return coherenceError;
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

  @Override
  public String checkDateCoherence(LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null || toDate == null) {
      return I18n.get(BudgetExceptionMessage.BUDGET_MISSING_DATES);
    }
    if (fromDate.isAfter(toDate)) {
      return I18n.get(BudgetExceptionMessage.BUDGET_WRONG_DATES);
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
