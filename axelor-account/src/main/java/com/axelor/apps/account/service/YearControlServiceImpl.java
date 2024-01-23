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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class YearControlServiceImpl implements YearControlService {

  protected YearRepository yearRepository;
  protected MoveRepository moveRepository;

  @Inject
  public YearControlServiceImpl(YearRepository yearRepository, MoveRepository moveRepository) {
    this.yearRepository = yearRepository;
    this.moveRepository = moveRepository;
  }

  @Override
  public void controlDates(Year year) throws AxelorException {
    Objects.requireNonNull(year);
    if (year.getId() != null) {
      Year savedYear = yearRepository.find(year.getId());
      if (savedYear.getStatusSelect() >= PeriodRepository.STATUS_OPENED
          && haveDifferentDates(year, savedYear)
          && isLinkedToMove(year)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.FISCAL_YEARS_DIFFERENTS_DATE_WHEN_NOT_OPENED));
      }
    }
  }

  @Override
  public boolean isLinkedToMove(Year year) {

    if (CollectionUtils.isEmpty(year.getPeriodList())) {
      return false;
    }
    return moveRepository
            .all()
            .filter("self.period.id in (:periodIdList)")
            .bind(
                "periodIdList",
                year.getPeriodList().stream()
                    .map(period -> period.getId())
                    .collect(Collectors.toList()))
            .count()
        > 0;
  }

  protected boolean haveDifferentDates(Year year, Year savedYear) {
    return (year.getFromDate() != null
            && savedYear.getFromDate() != null
            && !year.getFromDate().isEqual(savedYear.getFromDate()))
        || (year.getToDate() != null
            && savedYear.getToDate() != null
            && !year.getToDate().isEqual(savedYear.getToDate()));
  }
}
