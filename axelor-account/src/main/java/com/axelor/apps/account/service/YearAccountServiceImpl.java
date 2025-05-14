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
package com.axelor.apps.account.service;

import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.YearService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class YearAccountServiceImpl implements YearAccountService {

  protected YearService yearService;

  @Inject
  public YearAccountServiceImpl(YearService yearService) {
    this.yearService = yearService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Year generateFiscalYear(
      Company company,
      LocalDate fromDate,
      LocalDate toDate,
      String periodDuration,
      LocalDate reportedBalancedDate)
      throws AxelorException {
    String name = I18n.get(ITranslation.FISCAL_YEAR_CODE) + " " + fromDate.getYear();
    String code = I18n.get(ITranslation.FISCAL_YEAR_CODE) + fromDate.getYear();
    Year year =
        createYear(
            company,
            name,
            code,
            fromDate,
            toDate,
            periodDuration,
            YearRepository.TYPE_FISCAL,
            reportedBalancedDate);
    yearService.generatePeriodsForYear(year);
    return year;
  }

  @Override
  public Year createYear(
      Company company,
      String name,
      String code,
      LocalDate fromDate,
      LocalDate toDate,
      String periodDuration,
      int typeSelect,
      LocalDate reportedBalanceDate) {
    Year year =
        yearService.createYear(company, name, code, fromDate, toDate, periodDuration, typeSelect);
    year.setReportedBalanceDate(reportedBalanceDate);
    return year;
  }
}
