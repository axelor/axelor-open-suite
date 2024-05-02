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
package com.axelor.apps.base.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodUtilsServiceImpl implements PeriodUtilsService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Fetches the active period with the date, company and type in parameter
   *
   * @param date
   * @param company
   * @param typeSelect
   * @return
   * @throws AxelorException
   */
  @Override
  public Period getActivePeriod(LocalDate date, Company company, int typeSelect)
      throws AxelorException {
    Period period = this.getPeriod(date, company, typeSelect);
    if (period == null || this.isClosedPeriod(period)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_1),
          company.getName(),
          L10n.getInstance().format(date));
    }
    LOG.debug("Period : {}", period);
    return period;
  }

  @Override
  public Period getPeriod(LocalDate date, Company company, int typeSelect) {
    return JPA.all(Period.class)
        .filter(
            "self.year.company = ?1 and self.fromDate <= ?2 and self.toDate >= ?2 and self.year.typeSelect = ?3",
            company,
            date,
            typeSelect)
        .fetchOne();
  }

  @Override
  public boolean isClosedPeriod(Period period) throws AxelorException {
    List<Integer> unauthorizedStatus = new ArrayList<>();
    unauthorizedStatus.add(PeriodRepository.STATUS_TEMPORARILY_CLOSED);
    unauthorizedStatus.add(PeriodRepository.STATUS_CLOSED);

    return period != null && unauthorizedStatus.contains(period.getStatusSelect());
  }
}
