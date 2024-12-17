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
package com.axelor.apps.budget.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderCheckBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetObserver {

  public void budgetConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    SaleOrderBudgetService saleOrderBudgetService = Beans.get(SaleOrderBudgetService.class);

    if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      saleOrderBudgetService.generateBudgetDistribution(saleOrder);
      saleOrderBudgetService.updateBudgetLinesFromSaleOrder(saleOrder);
    }

    Beans.get(SaleOrderCheckBudgetService.class).checkNoComputeBudgetError(saleOrder);
  }
}
