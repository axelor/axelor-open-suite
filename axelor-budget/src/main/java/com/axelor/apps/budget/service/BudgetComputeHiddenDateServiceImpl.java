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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.interfaces.LocalDateInterval;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import java.util.Objects;

public class BudgetComputeHiddenDateServiceImpl implements BudgetComputeHiddenDateService {

  @Override
  public boolean isHidden(LocalDateInterval budgetDateInterval) {
    boolean isHidden = true;
    if (budgetDateInterval.getId() == null) {
      isHidden = false;
    } else if (budgetDateInterval.getFromDate() != null && budgetDateInterval.getToDate() != null) {
      LocalDateInterval savedGlobalBudget =
          JPA.em()
              .find(EntityHelper.getEntityClass(budgetDateInterval), budgetDateInterval.getId());
      boolean datesMatch =
          Objects.equals(savedGlobalBudget.getFromDate(), budgetDateInterval.getFromDate())
              && Objects.equals(savedGlobalBudget.getToDate(), budgetDateInterval.getToDate());
      if (!datesMatch) {
        isHidden = false;
      }
    }
    return isHidden;
  }
}
