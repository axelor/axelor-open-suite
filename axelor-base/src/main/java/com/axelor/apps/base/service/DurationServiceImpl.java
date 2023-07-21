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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
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

  @Override
  public BigDecimal getFactor(int durationType) throws AxelorException {
    if (durationType == DurationRepository.TYPE_MONTH) {
      return BigDecimal.valueOf(12);
    } else if (durationType == DurationRepository.TYPE_DAY) {
      return BigDecimal.valueOf(365);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(I18n.get(BaseExceptionMessage.UNKNOWN_DURATION), durationType));
    }
  }
}
