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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    return Optional.ofNullable(invoice.getValidatedDateTime()).map(LocalDateTime::toLocalDate);
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
