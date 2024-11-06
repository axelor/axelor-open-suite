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
package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineViewBudgetServiceImpl implements SaleOrderLineViewBudgetService {

  protected BudgetToolsService budgetToolsService;

  @Inject
  public SaleOrderLineViewBudgetServiceImpl(BudgetToolsService budgetToolsService) {
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public Map<String, Map<String, Object>> checkBudget(SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (saleOrder != null && saleOrder.getCompany() != null) {
      attrs.put(
          "budgetDistributionPanel",
          Map.of(
              "readonly",
              !budgetToolsService.checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
                  || saleOrder.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED));

      attrs.put(
          "budget",
          Map.of(
              "readonly",
              !budgetToolsService.checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
                  || saleOrder.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED));
    }
    return attrs;
  }
}
