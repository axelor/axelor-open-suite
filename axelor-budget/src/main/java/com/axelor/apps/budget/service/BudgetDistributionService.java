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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.auth.db.AuditableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BudgetDistributionService {

  /**
   * Create a budget distribution object with parameters and save
   *
   * @param budget, amount
   * @return BudgetDistribution
   */
  public BudgetDistribution createDistributionFromBudget(Budget budget, BigDecimal bigDecimal);

  /**
   * Check amount with budget available amount watching config for budget and return an error
   * message if needed
   *
   * @param budget, amount, date
   * @return String
   */
  public String getBudgetExceedAlert(Budget budget, BigDecimal amount, LocalDate date);

  /**
   * For all lines in invoice, compute paid amount field in all related budgets and save them
   *
   * @param invoice, ratio
   */
  public void computePaidAmount(Invoice invoice, BigDecimal ratio);

  String createBudgetDistribution(
      List<AnalyticMoveLine> analyticMoveLineList,
      Account account,
      Company company,
      LocalDate date,
      BigDecimal amount,
      String name,
      AuditableModel object);

  public void computeBudgetDistributionSumAmount(
      BudgetDistribution budgetDistribution, LocalDate computeDate);
}
