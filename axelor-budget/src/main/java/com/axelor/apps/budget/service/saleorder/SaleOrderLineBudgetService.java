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
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineBudgetService {

  List<BudgetDistribution> addBudgetDistribution(SaleOrderLine saleOrderLine);

  void fillBudgetStrOnLine(SaleOrderLine saleOrderLine, boolean multiBudget);

  String searchAndFillBudgetStr(SaleOrderLine saleOrderLine, boolean multiBudget);

  String computeBudgetDistribution(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  String getBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  void checkAmountForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException;

  void computeBudgetDistributionSumAmount(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getGroupBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getSectionBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getLineBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean isBudget);
}
