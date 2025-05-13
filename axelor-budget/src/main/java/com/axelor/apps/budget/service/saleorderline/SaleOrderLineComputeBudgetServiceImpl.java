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
package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineComputeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineComputeBudgetServiceImpl
    extends SaleOrderLineComputeSupplychainServiceImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderLineComputeBudgetServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      MarginComputeService marginComputeService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineCostPriceComputeService saleOrderLineCostPriceComputeService,
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService,
      AnalyticLineModelService analyticLineModelService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        taxService,
        currencyScaleService,
        productCompanyService,
        marginComputeService,
        currencyService,
        priceListService,
        saleOrderLinePackService,
        saleOrderLineCostPriceComputeService,
        appBaseService,
        appSupplychainService,
        appAccountService,
        analyticLineModelService,
        saleOrderLineServiceSupplyChain);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, Object> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.computeValues(saleOrder, saleOrderLine);

    if (appBudgetService.isApp("budget")) {
      saleOrderLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              saleOrderLine.getBudgetDistributionList(), saleOrderLine.getCompanyExTaxTotal()));
      saleOrderLineMap.put(
          "budgetRemainingAmountToAllocate", saleOrderLine.getBudgetRemainingAmountToAllocate());
    }
    return saleOrderLineMap;
  }
}
