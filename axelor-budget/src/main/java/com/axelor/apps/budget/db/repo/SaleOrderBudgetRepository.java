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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCopyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOrderingStatusService;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetRepository extends SaleOrderSupplychainRepository {

  @Inject
  public SaleOrderBudgetRepository(
      SaleOrderCopyService saleOrderCopyService,
      SaleOrderOrderingStatusService saleOrderOrderingStatusService) {
    super(saleOrderCopyService, saleOrderOrderingStatusService);
  }

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    if (!Beans.get(AppBudgetService.class).isApp("budget")) {
      return super.save(saleOrder);
    }

    try {
      if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        SaleOrderLineBudgetService saleOrderBudgetService =
            Beans.get(SaleOrderLineBudgetService.class);
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          saleOrderBudgetService.checkAmountForSaleOrderLine(saleOrderLine);
        }
      }

      saleOrder = super.save(saleOrder);

      Beans.get(SaleOrderBudgetService.class).validateSaleAmountWithBudgetDistribution(saleOrder);

    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    return saleOrder;
  }

  @Override
  public void remove(SaleOrder entity) {
    List<Budget> budgetList = new ArrayList<>();
    if (entity.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      budgetList = cancelSaleOrder(entity);
    }
    super.remove(entity);
    resetBudgets(budgetList);
  }

  public List<Budget> cancelSaleOrder(SaleOrder saleOrder) {
    List<Budget> budgetList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      budgetList =
          saleOrder.getSaleOrderLineList().stream()
              .filter(soLine -> !CollectionUtils.isEmpty(soLine.getBudgetDistributionList()))
              .flatMap(soLine -> soLine.getBudgetDistributionList().stream())
              .map(BudgetDistribution::getBudget)
              .collect(Collectors.toList());
      for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
        soLine.setBudget(null);
        soLine.clearBudgetDistributionList();
      }
    }
    return budgetList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetBudgets(List<Budget> budgetList) {
    BudgetServiceImpl budgetBudgetService = Beans.get(BudgetServiceImpl.class);

    if (!CollectionUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        budgetBudgetService.updateLines(budget);
        budgetBudgetService.computeTotalAmountCommitted(budget);
        budgetBudgetService.computeTotalAmountPaid(budget);
        budgetBudgetService.computeToBeCommittedAmount(budget);
      }
    }
  }
}
