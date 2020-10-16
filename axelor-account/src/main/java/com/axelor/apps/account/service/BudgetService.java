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
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.BudgetLineRepository;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetService {

  protected BudgetLineRepository budgetLineRepository;
  protected BudgetRepository budgetRepository;

  @Inject
  public BudgetService(
      BudgetLineRepository budgetLineRepository, BudgetRepository budgetRepository) {
    this.budgetLineRepository = budgetLineRepository;
    this.budgetRepository = budgetRepository;
  }

  public BigDecimal compute(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total = total.add(budgetLine.getAmountExpected());
      }
    }
    return total;
  }

  @Transactional
  public BigDecimal computeTotalAmountRealized(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    if (budgetLineList == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal totalAmountRealized =
        budgetLineList.stream()
            .map(BudgetLine::getAmountRealized)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    budget.setTotalAmountRealized(totalAmountRealized);

    return totalAmountRealized;
  }

  @Transactional
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
        Optional<LocalDate> optionaldate = getDate(budgetDistribution);
        optionaldate.ifPresent(
            date -> {
              for (BudgetLine budgetLine : budget.getBudgetLineList()) {
                LocalDate fromDate = budgetLine.getFromDate();
                LocalDate toDate = budgetLine.getToDate();
                if (fromDate != null
                    && toDate != null
                    && (fromDate.isBefore(date) || fromDate.isEqual(date))
                    && (toDate.isAfter(date) || toDate.isEqual(date))) {
                  budgetLine.setAmountRealized(
                      budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
                  break;
                }
              }
            });
      }
    }
    return budget.getBudgetLineList();
  }

  /**
   * @param budgetDistribution
   * @return returns an {@code Optional} because in some cases the child implementations can return
   *     a null value.
   */
  protected Optional<LocalDate> getDate(BudgetDistribution budgetDistribution) {
    Invoice invoice = budgetDistribution.getInvoiceLine().getInvoice();

    LocalDate invoiceDate = invoice.getInvoiceDate();
    if (invoiceDate != null) {
      return Optional.of(invoiceDate);
    }

    return Optional.of(invoice.getValidatedDate());
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
      if (budgetLineNumber != 1 && duration != 0) fromDate = fromDate.plusMonths(duration);
      if (c >= loopLimit) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.BUDGET_1));
      }
      c += 1;
      budgetLineToDate = duration == 0 ? toDate : fromDate.plusMonths(duration).minusDays(1);
      if (budgetLineToDate.isAfter(toDate)) budgetLineToDate = toDate;
      if (fromDate.isAfter(toDate)) continue;
      BudgetLine budgetLine = new BudgetLine();
      budgetLine.setFromDate(fromDate);
      budgetLine.setToDate(budgetLineToDate);
      budgetLine.setBudget(budget);
      budgetLine.setAmountExpected(budget.getAmountForGeneration());
      budgetLineList.add(budgetLine);
      budgetLineNumber++;
      if (duration == 0) break;
    }
    return budgetLineList;
  }

  public void checkSharedDates(Budget budget) throws AxelorException {

    if (budget.getBudgetLineList() == null) {
      return;
    }

    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    for (int i = 0; i < budgetLineList.size() - 1; i++) {
      BudgetLine budgetLineA = budgetLineList.get(i);
      LocalDate fromDateA = budgetLineA.getFromDate();
      LocalDate toDateA = budgetLineA.getToDate();

      for (int j = i + 1; j < budgetLineList.size(); j++) {
        BudgetLine budgetLineB = budgetLineList.get(j);
        LocalDate fromDateB = budgetLineB.getFromDate();
        LocalDate toDateB = budgetLineB.getToDate();

        if (fromDateA.equals(fromDateB) || toDateA.equals(toDateB)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get("Two or more budget lines share dates"));
        }

        if (fromDateA.isBefore(fromDateB) && (!toDateA.isBefore(fromDateB))) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get("Two or more budget lines share dates"));
        }

        if (fromDateA.isAfter(fromDateB) && (!fromDateA.isAfter(toDateB))) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get("Two or more budget lines share dates"));
        }
      }
    }
  }

  @Transactional
  public void validate(Budget budget) {
    budget.setStatusSelect(BudgetRepository.STATUS_VALIDATED);
  }

  @Transactional
  public void draft(Budget budget) {
    budget.setStatusSelect(BudgetRepository.STATUS_DRAFT);
  }

  public void updateBudgetLinesFromInvoice(Invoice invoice) {
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

    if (invoiceLineList == null) {
      return;
    }

    invoiceLineList.stream()
        .filter(invoiceLine -> invoiceLine.getBudgetDistributionList() != null)
        .flatMap(x -> x.getBudgetDistributionList().stream())
        .forEach(
            budgetDistribution -> {
              Budget budget = budgetDistribution.getBudget();
              updateLines(budget);
              computeTotalAmountRealized(budget);
            });
  }
}
