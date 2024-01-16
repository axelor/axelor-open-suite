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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.db.JPA;
import com.google.inject.Inject;

public class ExpenseFetchPeriodServiceImpl implements ExpenseFetchPeriodService {

  protected AppBaseService appBaseService;

  @Inject
  public ExpenseFetchPeriodServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Period getPeriod(Expense expense) {

    return JPA.all(Period.class)
        .filter(
            "self.fromDate <= :todayDate AND self.toDate >= :todayDate "
                + "AND self.allowExpenseCreation = true "
                + "AND self.year.company = :company "
                + "AND self.year.typeSelect = :typeSelect")
        .bind("todayDate", appBaseService.getTodayDate(expense.getCompany()))
        .bind("company", expense.getCompany())
        .bind("typeSelect", YearRepository.TYPE_FISCAL)
        .fetchOne();
  }
}
