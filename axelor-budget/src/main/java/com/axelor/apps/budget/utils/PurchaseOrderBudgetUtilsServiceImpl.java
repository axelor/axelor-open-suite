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
package com.axelor.apps.budget.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderBudgetUtilsServiceImpl implements PurchaseOrderBudgetUtilsService {

  protected BudgetService budgetService;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;
  protected BudgetDistributionService budgetDistributionService;

  @Inject
  public PurchaseOrderBudgetUtilsServiceImpl(
      BudgetService budgetService,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService,
      BudgetDistributionService budgetDistributionService) {
    this.budgetService = budgetService;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  public void generateBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (CollectionUtils.isNotEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      AppBudget appBudget = appBudgetService.getAppBudget();
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getBudget() != null) {
          BudgetDistribution budgetDistribution = null;

          if (CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {
            budgetDistribution =
                purchaseOrderLine.getBudgetDistributionList().stream()
                    .filter(
                        it ->
                            it.getBudget() != null
                                && it.getBudget().equals(purchaseOrderLine.getBudget()))
                    .findFirst()
                    .orElse(null);
          }

          if (budgetDistribution == null) {
            purchaseOrderLine.clearBudgetDistributionList();

            budgetDistribution = new BudgetDistribution();
            budgetDistribution.setBudget(purchaseOrderLine.getBudget());
            budgetDistribution.setBudgetAmountAvailable(
                budgetToolsService.getAvailableAmountOnBudget(
                    purchaseOrderLine.getBudget(),
                    purchaseOrderLine.getPurchaseOrder() != null
                        ? purchaseOrderLine.getPurchaseOrder().getOrderDate()
                        : null));

            budgetDistributionService.linkBudgetDistributionWithParent(
                budgetDistribution, purchaseOrderLine);
          }

          budgetDistribution.setAmount(
              currencyScaleService.getCompanyScaledValue(
                  budgetDistribution, purchaseOrderLine.getCompanyExTaxTotal()));
        } else if (purchaseOrderLine.getBudget() == null
            && appBudget != null
            && !appBudget.getManageMultiBudget()) {
          purchaseOrderLine.clearBudgetDistributionList();
        }
        purchaseOrderLine.setBudgetRemainingAmountToAllocate(
            budgetToolsService.getBudgetRemainingAmountToAllocate(
                purchaseOrderLine.getBudgetDistributionList(),
                purchaseOrderLine.getCompanyExTaxTotal()));
      }
    }
  }

  @Override
  public void validatePurchaseAmountWithBudgetDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException {
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine poLine : purchaseOrder.getPurchaseOrderLineList()) {
        String productCode =
            Optional.of(poLine)
                .map(PurchaseOrderLine::getProduct)
                .map(Product::getCode)
                .orElse(poLine.getProductName());
        if (StringUtils.notEmpty(productCode)) {
          budgetService.validateBudgetDistributionAmounts(
              poLine.getBudgetDistributionList(), poLine.getCompanyExTaxTotal(), productCode);
        }
      }
    }
  }

  @Override
  public void updateBudgetLinesFromPurchaseOrder(PurchaseOrder purchaseOrder) {

    if (CollectionUtils.isNotEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {
          purchaseOrderLine.getBudgetDistributionList().stream()
              .forEach(
                  budgetDistribution -> {
                    budgetDistribution.setImputationDate(purchaseOrder.getOrderDate());
                    Budget budget = budgetDistribution.getBudget();
                    budgetService.updateLines(budget);
                    budgetService.computeTotalAmountCommitted(budget);
                    budgetService.computeTotalAmountPaid(budget);
                    budgetService.computeToBeCommittedAmount(budget);
                  });
        }
      }
    }
  }
}
