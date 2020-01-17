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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.BudgetLineRepository;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BudgetSupplychainService extends BudgetService {

  @Inject
  public BudgetSupplychainService(
      BudgetLineRepository budgetLineRepository, BudgetRepository budgetRepository) {
    super(budgetLineRepository, budgetRepository);
  }

  @Override
  @Transactional
  public List<BudgetLine> updateLines(Budget budget) {
    if (budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        budgetLine.setAmountCommitted(BigDecimal.ZERO);
        budgetLine.setAmountRealized(BigDecimal.ZERO);
      }
      List<BudgetDistribution> budgetDistributionList = null;
      budgetDistributionList =
          Beans.get(BudgetDistributionRepository.class)
              .all()
              .filter(
                  "self.budget.id = ?1 AND self.purchaseOrderLine.purchaseOrder.statusSelect in (?2,?3)",
                  budget.getId(),
                  IPurchaseOrder.STATUS_VALIDATED,
                  IPurchaseOrder.STATUS_FINISHED)
              .fetch();
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        LocalDate orderDate =
            budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getOrderDate();
        if (orderDate != null) {
          for (BudgetLine budgetLine : budget.getBudgetLineList()) {
            LocalDate fromDate = budgetLine.getFromDate();
            LocalDate toDate = budgetLine.getToDate();
            if ((fromDate != null && toDate != null)
                && (fromDate.isBefore(orderDate) || fromDate.isEqual(orderDate))
                && (toDate.isAfter(orderDate) || toDate.isEqual(orderDate))) {
              budgetLine.setAmountCommitted(
                  budgetLine.getAmountCommitted().add(budgetDistribution.getAmount()));
              budgetLineRepository.save(budgetLine);
              break;
            }
          }
        }
      }

      budgetDistributionList =
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
                if ((fromDate.isBefore(date) || fromDate.isEqual(date))
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

  @Override
  protected Optional<LocalDate> getDate(BudgetDistribution budgetDistribution) {
    InvoiceLine invoiceLine = budgetDistribution.getInvoiceLine();

    if (invoiceLine == null) {
      return Optional.empty();
    }

    Invoice invoice = invoiceLine.getInvoice();

    if (invoice.getPurchaseOrder() != null) {
      return Optional.of(invoice.getPurchaseOrder().getOrderDate());
    }

    return super.getDate(budgetDistribution);
  }

  @Transactional
  public BigDecimal computeTotalAmountCommitted(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    if (budgetLineList == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal totalAmountCommitted =
        budgetLineList
            .stream()
            .map(BudgetLine::getAmountCommitted)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    budget.setTotalAmountCommitted(totalAmountCommitted);

    return totalAmountCommitted;
  }

  public void updateBudgetLinesFromPurchaseOrder(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();

    if (purchaseOrderLineList == null) {
      return;
    }

    purchaseOrderLineList
        .stream()
        .flatMap(x -> x.getBudgetDistributionList().stream())
        .forEach(
            budgetDistribution -> {
              Budget budget = budgetDistribution.getBudget();
              updateLines(budget);
              computeTotalAmountCommitted(budget);
            });
  }
}
