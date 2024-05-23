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
 */ package com.axelor.apps.budget.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetUtilsServiceImpl implements SaleOrderBudgetUtilsService {

  protected BudgetService budgetService;

  @Inject
  public SaleOrderBudgetUtilsServiceImpl(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @Override
  public void validateSaleAmountWithBudgetDistribution(SaleOrder saleOrder) throws AxelorException {
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
        String productCode =
            Optional.of(soLine)
                .map(SaleOrderLine::getProduct)
                .map(Product::getCode)
                .orElse(soLine.getProductName());
        if (StringUtils.notEmpty(productCode)) {
          budgetService.validateBudgetDistributionAmounts(
              soLine.getBudgetDistributionList(), soLine.getCompanyExTaxTotal(), productCode);
        }
      }
    }
  }
}
