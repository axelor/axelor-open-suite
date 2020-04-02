/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {

  public BigDecimal compute(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total = total.add(budgetLine.getAmountExpected());
      }
    }
    return total;
  }

  public List<BudgetLine> updateLines(Budget budget) {
    if (budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        budgetLine.setAmountRealized(BigDecimal.ZERO);
      }
      List<BudgetDistribution> budgetDistributionList =
          Beans.get(BudgetDistributionRepository.class)
              .all()
              .filter(
                  "self.budget.id = ?1 AND (self.invoiceLine.invoice.statusSelect = ?2 OR self.invoiceLine.invoice.statusSelect = ?3)",
                  budget.getId(),
                  InvoiceRepository.STATUS_VALIDATED,
                  InvoiceRepository.STATUS_VENTILATED)
              .fetch();
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        LocalDate orderDate = budgetDistribution.getInvoiceLine().getInvoice().getInvoiceDate();
        if (orderDate != null) {
          for (BudgetLine budgetLine : budget.getBudgetLineList()) {
            LocalDate fromDate = budgetLine.getFromDate();
            LocalDate toDate = budgetLine.getToDate();
            if ((fromDate.isBefore(orderDate) || fromDate.isEqual(orderDate))
                && (toDate.isAfter(orderDate) || toDate.isEqual(orderDate))) {
              budgetLine.setAmountRealized(
                  budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
              break;
            }
          }
        }
      }
    }
    return budget.getBudgetLineList();
  }

  public List<BudgetLine> generatePeriods(Budget budget) throws AxelorException {

    if (budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()) {
      List<BudgetLine> budgetLineList = budget.getBudgetLineList();
      budgetLineList.clear();
    }

    List<BudgetLine> budgetLineList = new ArrayList<BudgetLine>();
    Integer duration = budget.getPeriodDurationSelect();
    LocalDate fromDate = budget.getFromDate();
    LocalDate toDate = budget.getToDate();
    LocalDate budgetLineToDate = fromDate;
    Integer budgetLineNumber = 1;

    int c = 0;
    int loopLimit = 1000;
    while (budgetLineToDate.isBefore(toDate)) {
      if (budgetLineNumber != 1) fromDate = fromDate.plusMonths(duration);
      if (c >= loopLimit) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.BUDGET_1));
      }
      c += 1;
      budgetLineToDate = fromDate.plusMonths(duration).minusDays(1);
      if (budgetLineToDate.isAfter(toDate)) budgetLineToDate = toDate;
      if (fromDate.isAfter(toDate)) continue;
      BudgetLine budgetLine = new BudgetLine();
      budgetLine.setFromDate(fromDate);
      budgetLine.setToDate(budgetLineToDate);
      budgetLine.setBudget(budget);
      budgetLine.setAmountExpected(budget.getAmountForGeneration());
      budgetLineList.add(budgetLine);
      budgetLineNumber++;
    }
    return budgetLineList;
  }
}
