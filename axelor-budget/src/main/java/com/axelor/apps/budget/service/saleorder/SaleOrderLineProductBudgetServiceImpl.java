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
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductBudgetServiceImpl implements SaleOrderLineProductBudgetService {

  protected SaleOrderLineBudgetService saleOrderLineBudgetService;

  @Inject
  public SaleOrderLineProductBudgetServiceImpl(
      SaleOrderLineBudgetService saleOrderLineBudgetService) {
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
  }

  @Override
  public Map<String, Object> computeProductInformationBudget(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineBudgetService.setProductAccount(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineBudgetService.resetBudget(saleOrderLine));
    return saleOrderLineMap;
  }
}
