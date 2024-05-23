/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.budget.utils.SaleOrderBudgetUtilsService;
import com.axelor.apps.businessproject.db.repo.SaleOrderProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetRepository extends SaleOrderProjectRepository {

  protected AppBaseService appBaseService;
  protected BudgetService budgetService;
  protected SaleOrderLineBudgetService saleOrderLineBudgetService;
  protected SaleOrderBudgetUtilsService saleOrderBudgetUtilsService;

  @Inject
  public SaleOrderBudgetRepository(
      AppBaseService appBaseService,
      BudgetService budgetService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      SaleOrderBudgetUtilsService saleOrderBudgetUtilsService) {
    this.appBaseService = appBaseService;
    this.budgetService = budgetService;
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
    this.saleOrderBudgetUtilsService = saleOrderBudgetUtilsService;
  }

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    if (!appBaseService.isApp("budget")) {
      return super.save(saleOrder);
    }

    try {
      if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {

        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          saleOrderLineBudgetService.checkAmountForSaleOrderLine(saleOrderLine);
        }
      }

      saleOrder = super.save(saleOrder);

      saleOrderBudgetUtilsService.validateSaleAmountWithBudgetDistribution(saleOrder);

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
