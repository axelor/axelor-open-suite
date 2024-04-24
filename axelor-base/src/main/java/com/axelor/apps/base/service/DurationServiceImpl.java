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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
  public BigDecimal computeRatio(
      LocalDate start, LocalDate end, LocalDate totalStart, LocalDate totalEnd, Duration duration) {
    Preconditions.checkNotNull(
        start, I18n.get("You can't compute a" + " duration ratio without start date."));
    Preconditions.checkNotNull(
        end, I18n.get("You can't compute a" + " duration ratio without end date."));
    totalEnd = totalEnd.plus(1, ChronoUnit.DAYS);

    long totalComputedDays = ChronoUnit.DAYS.between(start, end) + 1;

    if (isFullDuration(start, duration, totalComputedDays)) {
      return BigDecimal.ONE;
    }

    long totalDays = getTotalDays(totalStart, totalEnd, duration);

    return BigDecimal.valueOf(totalComputedDays)
        .divide(
            BigDecimal.valueOf(totalDays),
            AppBaseService.COMPUTATION_SCALING,
            RoundingMode.HALF_UP);
  }

  protected boolean isFullDuration(LocalDate start, Duration duration, long totalComputedDays) {
    if (duration.getTypeSelect() == DurationRepository.TYPE_MONTH) {
      return ChronoUnit.DAYS.between(start, start.plusMonths(duration.getValue()))
          == totalComputedDays;
    } else {
      return duration.getValue() == totalComputedDays;
    }
  }

  protected long getTotalDays(LocalDate totalStart, LocalDate totalEnd, Duration duration) {
    long totalDays = ChronoUnit.DAYS.between(totalStart, totalEnd);
    if (duration == null) {
      return totalDays;
    }
    long durationValue = duration.getValue();
    if (duration.getTypeSelect() == DurationRepository.TYPE_MONTH) {
      long months = ChronoUnit.MONTHS.between(totalStart, totalEnd);
      if (months < durationValue) {
        LocalDate theoryStart = totalStart.minusMonths(duration.getValue() - months);
        totalDays = ChronoUnit.DAYS.between(theoryStart, totalEnd);
      }
    } else {
      if (totalDays < durationValue) {
        LocalDate theoryStart = totalStart.minusDays(duration.getValue() - totalDays);
        totalDays = ChronoUnit.DAYS.between(theoryStart, totalEnd);
      }
    }
    return totalDays;
  }
}
