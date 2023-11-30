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

import com.axelor.apps.budget.db.BudgetLine;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class BudgetLineServiceImpl implements BudgetLineService {

  @Inject
  public BudgetLineServiceImpl() {}

  @Override
  public Optional<BudgetLine> findBudgetLineAtDate(
      List<BudgetLine> budgetLineList, LocalDate date) {
    if (budgetLineList == null || budgetLineList.isEmpty() || date == null) {
      return Optional.empty();
    }
    return budgetLineList.stream()
        .filter(
            budgetLine ->
                (budgetLine.getFromDate().isBefore(date) || budgetLine.getFromDate().isEqual(date))
                    && (budgetLine.getToDate().isAfter(date)
                        || budgetLine.getToDate().isEqual(date)))
        .findFirst();
  }
}
