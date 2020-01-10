/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Singleton
public class DurationServiceImpl implements DurationService {

  @Override
  public LocalDate computeDuration(Duration duration, LocalDate date) {
    if (duration == null) {
      return date;
    }
    switch (duration.getTypeSelect()) {
      case DurationRepository.TYPE_MONTH:
        return date.plusMonths(duration.getValue());
      case DurationRepository.TYPE_DAY:
        return date.plusDays(duration.getValue());
      default:
        return date;
    }
  }

  @Override
  public BigDecimal computeRatio(LocalDate start, LocalDate end, Duration duration) {
    Preconditions.checkNotNull(
        start, I18n.get("You can't compute a" + " duration ratio without start date."));
    Preconditions.checkNotNull(
        end, I18n.get("You can't compute a" + " duration ratio without end date."));
    if (duration == null) {
      return BigDecimal.ONE;
    }
    end = end.plus(1, ChronoUnit.DAYS);

    if (duration.getTypeSelect() == DurationRepository.TYPE_MONTH) {
      long months = ChronoUnit.MONTHS.between(start, end);
      LocalDate theoryEnd = start.plusMonths(months);
      long restDays = ChronoUnit.DAYS.between(theoryEnd, end);
      long daysInMonth = ChronoUnit.DAYS.between(theoryEnd, theoryEnd.plusMonths(1));
      return BigDecimal.valueOf(months)
          .add(
              BigDecimal.valueOf(restDays)
                  .divide(BigDecimal.valueOf(daysInMonth), MathContext.DECIMAL32))
          .divide(BigDecimal.valueOf(duration.getValue()), MathContext.DECIMAL32);
    } else {
      long restDays = ChronoUnit.DAYS.between(start, end);
      return BigDecimal.valueOf(restDays)
          .divide(BigDecimal.valueOf(duration.getValue()), MathContext.DECIMAL32);
    }
  }
}
