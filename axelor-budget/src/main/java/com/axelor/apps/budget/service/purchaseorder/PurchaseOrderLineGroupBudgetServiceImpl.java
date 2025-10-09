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
package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.compute.BudgetDistributionComputeService;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import java.util.Map;

public class PurchaseOrderLineGroupBudgetServiceImpl extends PurchaseOrderLineServiceProjectImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;
  protected BudgetDistributionComputeService budgetDistributionComputeService;

  @Inject
  public PurchaseOrderLineGroupBudgetServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      UnitConversionService unitConversionService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticLineModelService analyticLineModelService,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService,
      BudgetDistributionComputeService budgetDistributionComputeService) {
    super(
        analyticMoveLineService,
        unitConversionService,
        appAccountService,
        accountConfigService,
        analyticLineModelService,
        purchaseOrderLineRepo);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
    this.budgetDistributionComputeService = budgetDistributionComputeService;
  }

  @Override
  public Map<String, Object> compute(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    Map<String, Object> map = super.compute(purchaseOrderLine, purchaseOrder);

    if (appBudgetService.isApp("budget")) {

      if (purchaseOrder != null
          && purchaseOrder.getStatusSelect() <= PurchaseOrderRepository.STATUS_REQUESTED) {
        budgetDistributionComputeService.updateMonoBudgetAmounts(
            purchaseOrderLine.getBudgetDistributionList(),
            purchaseOrderLine.getCompanyExTaxTotal());
        map.put("budgetDistributionList", purchaseOrderLine.getBudgetDistributionList());
      }

      map.put(
          "budgetRemainingAmountToAllocate",
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              purchaseOrderLine.getBudgetDistributionList(),
              purchaseOrderLine.getCompanyExTaxTotal()));
    }
    return map;
  }
}
