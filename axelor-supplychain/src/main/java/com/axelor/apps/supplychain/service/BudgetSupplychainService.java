/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.inject.Beans;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetSupplychainService extends BudgetService {

  @Inject private AppAccountService appAccountService;

  @Override
  public List<BudgetLine> updateLines(Budget budget) {
    if (budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        budgetLine.setAmountCommitted(BigDecimal.ZERO);
        budgetLine.setAmountRealized(BigDecimal.ZERO);
      }
      List<Integer> statusList = new ArrayList<Integer>();
      if (appAccountService.isApp("budget")) {
        String budgetStatus = appAccountService.getAppBudget().getBudgetStatusSelect();
        if (!budgetStatus.isEmpty()) {
          String[] numbers = budgetStatus.split(", ");
          for (int c = 0; c < numbers.length; c++) statusList.add(Integer.parseInt(numbers[c]));
        }
      }
      List<BudgetDistribution> budgetDistributionList = null;
      if (!statusList.isEmpty()) {
        budgetDistributionList =
            Beans.get(BudgetDistributionRepository.class)
                .all()
                .filter(
                    "self.budgetLine.budget.id = ?1 AND (self.purchaseOrderLine.purchaseOrder.statusSelect in (?2) OR ?3 is null)",
                    budget.getId(),
                    statusList,
                    statusList)
                .fetch();
        for (BudgetDistribution budgetDistribution : budgetDistributionList) {
          BudgetLine budgetLine = budgetDistribution.getBudgetLine();
          budgetLine.setAmountCommitted(
              budgetLine.getAmountCommitted().add(budgetDistribution.getAmount()));
        }
      }
      budgetDistributionList =
          Beans.get(BudgetDistributionRepository.class)
              .all()
              .filter(
                  "self.budgetLine.budget.id = ?1 AND (self.invoiceLine.invoice.statusSelect = ?2 OR self.invoiceLine.invoice.statusSelect = ?3)",
                  budget.getId(),
                  InvoiceRepository.STATUS_VALIDATED,
                  InvoiceRepository.STATUS_VENTILATED)
              .fetch();
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        BudgetLine budgetLine = budgetDistribution.getBudgetLine();
        budgetLine.setAmountRealized(
            budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
      }
    }
    return budget.getBudgetLineList();
  }

  public void computeBudgetDistributionSumAmount(BudgetDistribution budgetDistribution) {
    BudgetLine budgetLine = budgetDistribution.getBudgetLine();
    BigDecimal budgetAmountAvailable =
        budgetLine.getAmountExpected().subtract(budgetLine.getAmountCommitted());
    budgetDistribution.setBudgetAmountAvailable(budgetAmountAvailable);
  }

  public String changebudgetLineDomain(List<BudgetLine> budgetLineList) {
    Map<Budget, BudgetLine> map = new HashMap<>();
    ArrayList<Long> idList = new ArrayList<>();

    for (BudgetLine budgetLine : budgetLineList) {

      if (budgetLine.getBudget().getAllBudgetLinesAvailable()) {
        idList.add(budgetLine.getId());
      } else if (!budgetLine.getBudget().getAllBudgetLinesAvailable()
          && map.get(budgetLine.getBudget()) == null) {
        map.put(budgetLine.getBudget(), budgetLine);
        idList.add(budgetLine.getId());
      }
    }

    String idString = Joiner.on(",").join(idList);
    String domain = "self.id IN (" + idString + ")";

    return domain;
  }
}
