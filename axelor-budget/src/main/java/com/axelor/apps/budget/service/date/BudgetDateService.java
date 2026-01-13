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
package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import java.time.LocalDate;
import java.util.List;

public interface BudgetDateService {
  String checkBudgetDates(Invoice invoice);

  String checkBudgetDates(Move move);

  String checkBudgetDates(PurchaseOrder purchaseOrder);

  String checkBudgetDates(SaleOrder saleOrder);

  String getBudgetDateError(
      LocalDate fromDate,
      LocalDate toDate,
      Budget budget,
      List<BudgetDistribution> budgetDistributionList);

  String checkDateCoherence(LocalDate fromDate, LocalDate toDate);
}
