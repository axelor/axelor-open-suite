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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderManagementBudgetRepository extends PurchaseOrderSupplychainRepository {

  @Override
  public PurchaseOrder save(PurchaseOrder purchaseOrder) {
    try {

      Beans.get(PurchaseOrderBudgetService.class).generateBudgetDistribution(purchaseOrder);

      purchaseOrder = super.save(purchaseOrder);

      Beans.get(PurchaseOrderBudgetService.class)
          .validatePurchaseAmountWithBudgetDistribution(purchaseOrder);

      if (purchaseOrder.getStatusSelect() != null
          && purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_REQUESTED) {
        Beans.get(PurchaseOrderBudgetService.class)
            .updateBudgetLinesFromPurchaseOrder(purchaseOrder);
      }

    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    return purchaseOrder;
  }

  // This remove method is override because when PO gets deleted, budget's committed amount should
  // be calculated again, which is done in cancel method.
  @Override
  public void remove(PurchaseOrder entity) {
    List<Budget> budgetList = new ArrayList<>();
    if (entity.getStatusSelect() >= PurchaseOrderRepository.STATUS_REQUESTED) {
      budgetList = cancelPurchaseOrder(entity);
    }
    super.remove(entity);
    resetBudgets(budgetList);
  }

  public List<Budget> cancelPurchaseOrder(PurchaseOrder purchaseOrder) {
    List<Budget> budgetList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      budgetList =
          purchaseOrder.getPurchaseOrderLineList().stream()
              .filter(poLine -> !CollectionUtils.isEmpty(poLine.getBudgetDistributionList()))
              .flatMap(poLine -> poLine.getBudgetDistributionList().stream())
              .map(BudgetDistribution::getBudget)
              .collect(Collectors.toList());
      for (PurchaseOrderLine poLine : purchaseOrder.getPurchaseOrderLineList()) {
        poLine.setBudget(null);
        poLine.clearBudgetDistributionList();
      }
    }
    return budgetList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetBudgets(List<Budget> budgetList) {
    BudgetService budgetService = Beans.get(BudgetService.class);

    if (!CollectionUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        budgetService.updateLines(budget);
        budgetService.computeTotalAmountCommitted(budget);
        budgetService.computeTotalAmountPaid(budget);
        budgetService.computeToBeCommittedAmount(budget);
      }
    }
  }
}
